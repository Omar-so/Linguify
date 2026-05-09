import hashlib
import aiohttp
import cv2
import numpy as np

async def download_image(url: str) -> np.ndarray:
    async with aiohttp.ClientSession() as session:
        async with session.get(url) as resp:
            resp.raise_for_status()
            content = await resp.read()
    np_arr = np.frombuffer(content, np.uint8)
    img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)
    if img is None:
        raise ValueError("Could not decode image")
    return img

def compute_image_hash(img: np.ndarray) -> str:
    _, buf = cv2.imencode('.png', img)
    return hashlib.sha256(buf.tobytes()).hexdigest()

def crop_image_with_offset(
    img: np.ndarray,
    box: dict,
    offset: int = 10
) -> tuple[np.ndarray, tuple[int, int]]:
    h, w = img.shape[:2]

    cx, cy = box["x"], box["y"]
    bw, bh = box["width"], box["height"]

    x1 = int(cx - bw / 2) - offset
    y1 = int(cy - bh / 2) - offset
    x2 = int(cx + bw / 2) + offset
    y2 = int(cy + bh / 2) + offset

    # Clamp to image boundaries
    x1 = max(0, x1)
    y1 = max(0, y1)
    x2 = min(w, x2)
    y2 = min(h, y2)

    # Validate crop
    if x2 <= x1 or y2 <= y1:
        return None, (x1, y1)

    crop = img[y1:y2, x1:x2]
    return crop, (x1, y1)