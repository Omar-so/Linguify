import base64
import aiohttp
import cv2
import numpy as np
from abc import ABC, abstractmethod
from app.strategies.ocr.base import OCRStrategy
from app.config import settings   #  correct import



class PaddleOCRAPIStrategy(OCRStrategy):
    def __init__(self):
        self.api_url = settings.ocr_api_url
        self.token = settings.ocr_api_token

    @staticmethod
    def extract_text_blocks(item: dict) -> dict | None:
        offset_x = item.get('offset_x', 0)
        offset_y = item.get('offset_y', 0)

        parsing_results = (
            item.get('text_data', {})
                .get('result', {})          # ✅ unwrap the result key
                .get('layoutParsingResults', [])
        )

        for res in parsing_results:
            for block in res.get('prunedResult', {}).get('parsing_res_list', []):
                points = block.get('block_polygon_points', [])
                if not points:
                    continue

                global_poly = (
                    np.array(points, dtype=np.float32)
                    + np.array([offset_x, offset_y])
                )

                return {
                    "text": block.get('block_content', '').replace('\n', ' ').strip(),
                    "polygon": global_poly.tolist(),
                    "type": block.get('block_label', 'text')
                }

        return None

    async def extract(self, image: np.ndarray, offset: tuple[int, int]) -> list:
        _, img_encoded = cv2.imencode('.jpg', image)
        file_data = base64.b64encode(img_encoded.tobytes()).decode("ascii")

        payload = {
            "file": file_data,
            "fileType": 1,
            "useDocOrientationClassify": False,
            "useDocUnwarping": False,
            "useChartRecognition": False,
        }

        headers = {
            "Authorization": f"token {self.token}",
            "Content-Type": "application/json"
        }

        async with aiohttp.ClientSession() as session:
            async with session.post(self.api_url, json=payload, headers=headers) as resp:
                resp.raise_for_status()
                data = await resp.json()
        return self.extract_text_blocks({
            "text_data": data,
            "offset_x": offset[0],
            "offset_y": offset[1]
        })