import json


import aiofiles
import os
import asyncio
from abc import ABC, abstractmethod
from io import BytesIO



class CDNStrategy(ABC):
    @abstractmethod
    async def upload(self, data: bytes, filename: str) -> str:
        pass
