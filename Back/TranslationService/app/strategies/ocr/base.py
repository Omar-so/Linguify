import base64
import aiohttp
import cv2
import numpy as np
from abc import ABC, abstractmethod

from app.config import settings   #  correct import

# return the text in the image given the bounding box of the text in the image
class OCRStrategy(ABC):
    @abstractmethod
    async def extract(self, image: np.ndarray, offset: tuple[int, int]) -> list:
        pass
