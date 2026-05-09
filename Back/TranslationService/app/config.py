from pydantic_settings import BaseSettings
from typing import Optional

class Settings(BaseSettings):
    redis_url: str
    job_stream: str
    result_stream: str
    dead_letter_stream: str

    roboflow_api_key: str
    roboflow_workspace: str
    roboflow_workflow_id: str

    ocr_api_url: str
    ocr_api_token: str

    cdn_strategy: str = "cloudinary"
    detection_strategy: str = "RoboflowDetection"
    ocr_strategy: str = "PaddleOCR"
    translation_strategy: str = "Naive"
    broker_strategy: str = "redis"

    cloudinary_cloud_name: str
    cloudinary_api_key: str
    cloudinary_api_secret: str

    cdn_bucket: Optional[str] = None
    cdn_region: Optional[str] = None

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"

settings = Settings()