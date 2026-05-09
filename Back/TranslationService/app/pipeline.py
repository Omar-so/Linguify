import cv2
import numpy as np
from app.config import settings
from app.cache import ImageCache
from app.strategies.cdn.strategy  import get_cdn_strategy
from app.processor.renderer import render_translated_image

from app.utils.image_utils import download_image, compute_image_hash

from app.strategies.detection.strategy import get_detection_stratgy

from app.strategies.ocr.strategy import get_ocr_strategy

from app.utils.image_utils import crop_image_with_offset

from app.strategies.translation.strategy import get_translation_stratgy

class ImageTranslationPipeline:
    def __init__(self):
        self.cache = ImageCache()
        self.detector = get_detection_stratgy(settings.detection_strategy)
        self.ocr = get_ocr_strategy(settings.ocr_strategy)  
        self.cdn = get_cdn_strategy(settings.cdn_strategy)
        self.translation = get_translation_stratgy(settings.translation_strategy)

    async def process(self, job: dict) -> dict:
        job_id = job["job_id"]
        image_url = job["image_url"]
        user_id = job["user_id"]
        target_lang = job["target_language"]
        server_id = job["server_id"]
      
        # 1. Download & hash
        img = await download_image(image_url)
        img_hash = compute_image_hash(img)
        
        cached = await self.cache.get(img_hash)

        print(job)
        if cached:
            return {
                "user_id": user_id,
                "image_id": job_id,
                "status": "success",
                "server_id": server_id,
                "status": "success",
                "image_url": cached["cdn_url"],
                "cached": True
            }
 
        # 3. Run pipeline
        #    a. Detection
        boxes = await self.detector.detect(img)
        if not boxes:
            raise ValueError("No text regions detected")

        #    b. Crop and OCR with offsets
        img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        text_blocks = []
        for box in boxes:
            crop, (x1, y1) = crop_image_with_offset(img_rgb, box)

            if crop is None or crop.size == 0:
                continue
        
            ocr_result = await self.ocr.extract(crop , offset=(x1, y1))

            if ocr_result is None:  
                continue

            text_blocks.append( ocr_result )

        
        #    d. Translate text (placeholder – you would integrate a translation service)
        #       For now, we keep original text (or add a dummy translation)
        Translation = self.translation.translate_blocks(text_blocks, target_lang)
        
        #    e. Render final image (inpainting + Pango text overlay)
        final_image = render_translated_image(img, text_blocks)

        # 4. Upload to CDN
        _, img_encoded = cv2.imencode('.png', final_image)
        img_bytes = img_encoded.tobytes()
        cdn_url = await self.cdn.upload(img_bytes, f"{job_id}.png")

        print(f"[INFO] Job {job_id} processed successfully. CDN URL: {cdn_url}")
         

        # 5. Cache result
        await self.cache.set(img_hash, {"cdn_url": cdn_url, "job_id": job_id})

        return {
            "user_id": user_id,
            "image_id": job_id,
            "status": "success",
            "image_url": cdn_url,
            "server_id": server_id,
            "cached": False
        }