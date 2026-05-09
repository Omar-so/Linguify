from app.strategies.detection.roboflowdetection import RoboflowDetection
from app.strategies.detection.base import DetectionStrategy

def get_detection_stratgy(name: str) -> DetectionStrategy:
    if name == "RoboflowDetection":
        return RoboflowDetection()  
    else:
        raise ValueError(f"Unknown CDN strategy: {name}")