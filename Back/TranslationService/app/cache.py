import json
import aioredis
from app.config import settings


class ImageCache:
    _instance: "ImageCache | None" = None          # singleton holder

    def __new__(cls) -> "ImageCache":
        if cls._instance is None:
            cls._instance = super().__new__(cls)
            cls._instance.redis = None
            cls._instance.prefix = "cache:image"
        return cls._instance

    async def connect(self) -> None:
        if self.redis is None:                      # connect only once
            self.redis = await aioredis.from_url(
                settings.redis_url,
                decode_responses=True,
            )

    async def close(self) -> None:
        if self.redis:
            await self.redis.close()
            self.redis = None
            ImageCache._instance = None             # reset singleton on close

    def _key(self, image_hash: str) -> str:
        return f"{self.prefix}:{image_hash}"

    async def get(self, image_hash: str):
        data = await self.redis.get(self._key(image_hash))
        return json.loads(data) if data else None

    async def set(self, image_hash: str, result: dict, ttl: int = 86400):
        await self.redis.setex(self._key(image_hash), ttl, json.dumps(result))