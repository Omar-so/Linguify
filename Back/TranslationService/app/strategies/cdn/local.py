import aiofiles
import os
from abc import ABC, abstractmethod

from app.config import settings

import cloudinary
from cloudinary import CloudinaryImage
import cloudinary.uploader
import cloudinary.api

# Import to format the JSON responses
# ==============================
import json


import aiofiles
import os
import asyncio
from abc import ABC, abstractmethod
from io import BytesIO

import cloudinary
import cloudinary.uploader


from app.strategies.cdn.base import CDNStrategy

class LocalCDNStrategy(CDNStrategy):
    def __init__(self, base_dir: str = "/tmp/cdn"):
        self.base_dir = base_dir
        os.makedirs(base_dir, exist_ok=True)

    async def upload(self, data: bytes, filename: str) -> str:
        path = os.path.join(self.base_dir, filename)
        async with aiofiles.open(path, "wb") as f:
            await f.write(data)
        return f"file://{path}"