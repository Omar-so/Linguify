import asyncio
import json
import os

import aioredis

from app.strategies.broker.base import MessageBroker
from app.config import settings


class RedisBroker(MessageBroker):
    def __init__(self, max_concurrency: int = 10):
        self.redis = None
        self.consumer_group = "fastapi-workers"
        self.consumer_name = f"worker-{os.getpid()}"
        self.sem = asyncio.Semaphore(max_concurrency)
        self.running = True

    # -------------------------------------------------------------------------
    # Abstract method implementations
    # -------------------------------------------------------------------------

    async def connect(self) -> None:
        self.redis = await aioredis.from_url(settings.redis_url, decode_responses=True)
        try:
            await self.redis.xgroup_create(
                settings.job_stream,
                self.consumer_group,
                id="0",
                mkstream=True,
            )
        except aioredis.ResponseError as e:
            if "BUSYGROUP" not in str(e):
                raise

    async def consume(self, process_job_coro) -> None:
        asyncio.create_task(self._recover_pending_loop(process_job_coro))

        while self.running:
            try:
                messages = await self.redis.xreadgroup(
                    groupname=self.consumer_group,
                    consumername=self.consumer_name,
                    streams={settings.job_stream: ">"},
                    count=10,
                    block=5000,
                )

                if not messages:
                    continue

                for stream_data in messages:
                    entries = stream_data[1]
                    for entry in entries:
                        msg_id, fields = entry[0], entry[1]
                        await self.sem.acquire()
                        asyncio.create_task(
                            self._handle_job(msg_id, fields, process_job_coro)
                        )

            except Exception as e:
                print(f"[ERROR] Stream read: {e}")
                await asyncio.sleep(1)

    import json

    async def publish_result(self, result: dict) -> None:
        """
        Publishes the processing result to a specific Redis Pub/Sub channel.
        The Java ResultSubscriber is listening for 'process:{serverId}'.
        """
        # 1. Determine the channel name
        # Ensure 'server_id' is passed in the result dict or available via settings
        print(f"[DEBUG] publish_result received: {result}")
        server_id = result.get("server_id") 
        channel_name = server_id

        # 2. Construct the payload to match the Java Subscriber's expected schema
        # Keys must match: result.get("user_id"), result.get("image_url"), result.get("image_id")
        payload = {
            "user_id": result.get("user_id"),
            "image_url": result.get("image_url"), # Assuming your worker uses 'processed_url'
            "image_id": result.get("image_id")
        }

        # 3. Publish to Redis Pub/Sub (NOT a stream)
        # This matches the PatternTopic in your Java container
        print(f"[INFO] Publishing result to channel '{channel_name}': {payload}")
        await self.redis.publish("serverId", json.dumps(payload))

    async def close(self) -> None:
        self.running = False
        if self.redis:
            await self.redis.close()

    # -------------------------------------------------------------------------
    # Redis-specific internals
    # -------------------------------------------------------------------------

    async def _handle_job(self, msg_id, fields, process_job_coro):
        job_data = {}
        try:
            
            job_data = fields
            job_id = job_data.get("job_id")
            print(f"[INFO] Handling job {job_id} (message ID: {msg_id})")
            # if await self._is_processed(job_id):
            #     await self._ack(msg_id)
            #     return
            print(f"[INFO] reached here ?? ")
            result = await process_job_coro(job_data)
            print(f"[INFO] Job {job_id} processed. Result: {result}")
            await self.publish_result(result)
            await self._mark_processed(job_id)
            await self._ack(msg_id)

        except Exception as e:
            print(f"[ERROR] Job failed {msg_id}: {e}")
            retry_count = int(fields.get("retry", 0))

            if retry_count < 3:
                await self.redis.xadd(
                    settings.job_stream,
                    {"data": json.dumps(job_data), "retry": retry_count + 1},
                )
            else:
                await self.redis.xadd(
                    settings.dead_letter_stream,
                    {"data": json.dumps(job_data), "error": str(e)},
                )

            await self._ack(msg_id)

        finally:
            self.sem.release()

    async def _ack(self, msg_id: str) -> None:
        await self.redis.xack(settings.job_stream, self.consumer_group, msg_id)

    async def _is_processed(self, job_id: str) -> bool:
        if not job_id:
            return False
        return await self.redis.sismember("processed_jobs", job_id)

    async def _mark_processed(self, job_id: str) -> None:
        if job_id:
            await self.redis.sadd("processed_jobs", job_id)

    async def _recover_pending_loop(self, process_job_coro):
        while self.running:
            try:
                pending = await self.redis.xpending_range(
                    settings.job_stream,
                    self.consumer_group,
                    min="-",
                    max="+",
                    count=10,
                )

                if not pending:
                    await asyncio.sleep(10)
                    continue

                for msg in pending:
                    try:
                        msg_id, idle = msg[0], msg[2]
                        if idle is None or int(idle) <= 60_000:
                            continue

                        claimed = await self.redis.xclaim(
                            settings.job_stream,
                            self.consumer_group,
                            self.consumer_name,
                            min_idle_time=60_000,
                            message_ids=[msg_id],
                        )

                        for _, entries in claimed:
                            for entry in entries:
                                inner_id, fields = entry[0], entry[1]
                                await self.sem.acquire()
                                asyncio.create_task(
                                    self._handle_job(inner_id, fields, process_job_coro)
                                )

                    except Exception as inner:
                        print(f"[ERROR] Recovery item failed: {inner}")

            except Exception as e:
                print(f"[ERROR] Recovery loop: {repr(e)}")

            await asyncio.sleep(10)