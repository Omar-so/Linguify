import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.responses import JSONResponse

from app.strategies.broker.strategy import get_broker_stratgy
from app.pipeline import ImageTranslationPipeline
from app.cache import ImageCache
from app.config import settings

@asynccontextmanager
async def lifespan(app: FastAPI):
    broker = get_broker_stratgy(settings.broker_strategy)
    await broker.connect()

    cache = ImageCache()
    await cache.connect()

    pipeline = ImageTranslationPipeline()
    task = asyncio.create_task(broker.consume(pipeline.process))

    app.state.broker = broker       # expose for health check
    app.state.cache = cache         # expose for health check

    yield

    task.cancel()
    await broker.close()
    await cache.close()


app = FastAPI(lifespan=lifespan)

@app.get("/health")
async def health():
    results = {}

    try:
        await app.state.broker.redis.ping()
        results["broker"] = "ok"
    except Exception as e:
        results["broker"] = f"failed: {repr(e)}"

    try:
        await app.state.cache.redis.ping()
        results["cache"] = "ok"
    except Exception as e:
        results["cache"] = f"failed: {repr(e)}"

    try:
        info = await app.state.broker.redis.xinfo_stream(settings.job_stream)
        results["job_stream"] = f"ok — {info['length']} messages"
    except Exception as e:
        results["job_stream"] = f"failed: {repr(e)}"

    try:
        groups = await app.state.broker.redis.xinfo_groups(settings.job_stream)
        results["consumer_group"] = f"ok — {groups[0]['name']} / {groups[0]['consumers']} consumers"
    except Exception as e:
        results["consumer_group"] = f"failed: {repr(e)}"

    all_ok = all("ok" in str(v) for v in results.values())
    results["status"] = "healthy" if all_ok else "unhealthy"

    return JSONResponse(
        content=results,
        status_code=200 if all_ok else 503
    )