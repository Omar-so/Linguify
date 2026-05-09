from abc import ABC, abstractmethod
from inference_sdk import InferenceHTTPClient
import numpy as np
import tempfile
import cv2
import os
from app.config import settings
from app.strategies.detection.base import DetectionStrategy


class RoboflowDetection(DetectionStrategy):
    def __init__(self):
        self.client = InferenceHTTPClient(
            api_url="https://serverless.roboflow.com",
            api_key= settings.roboflow_api_key
        )
        self.workspace = settings.roboflow_workspace
        self.workflow_id = settings.roboflow_workflow_id

    async def detect(self, image: np.ndarray) -> list:
        # Write image to temp file (Roboflow SDK expects file path)
        with tempfile.NamedTemporaryFile(suffix=".png", delete=False) as tmp:
            cv2.imwrite(tmp.name, image)
            tmp_path = tmp.name

        try:
            # Run workflow (synchronous call, run in thread to not block)
            import asyncio
            loop = asyncio.get_event_loop()
            result = await loop.run_in_executor(
                None,
                lambda: self.client.run_workflow(
                    workspace_name=self.workspace,
                    workflow_id=self.workflow_id,
                    images={"image": tmp_path},
                    use_cache=True
                )
            )
        finally:
            os.unlink(tmp_path)

        # Extract bounding boxes from result
        predictions = result[0]['detection_predictions']['predictions']
        boxes = []
        for pred in predictions:
            boxes.append({
                "x": pred['x'],
                "y": pred['y'],
                "width": pred['width'],
                "height": pred['height']
            })
        return boxes

