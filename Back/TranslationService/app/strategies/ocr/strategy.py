from app.strategies.ocr.paddleocr import PaddleOCRAPIStrategy
from app.strategies.ocr.base import OCRStrategy

def get_ocr_strategy(name: str) -> OCRStrategy:
    if name == "PaddleOCR":
        return PaddleOCRAPIStrategy()
    else:
        raise ValueError(f"Unknown OCR strategy: {name}")