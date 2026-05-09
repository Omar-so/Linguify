from abc import ABC, abstractmethod
from inference_sdk import InferenceHTTPClient
import numpy as np
import tempfile
import cv2
import os
from app.config import settings


#this take and image and return x and y and width and height of the bounding box of the text in the image

class DetectionStrategy(ABC):
    @abstractmethod
    async def detect(self, image: np.ndarray) -> list:
        pass
