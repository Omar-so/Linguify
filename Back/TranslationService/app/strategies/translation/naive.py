from abc import ABC, abstractmethod
from typing import List, Optional
from translate import Translator
from app.strategies.translation.base import TranslationStrategy


class NaiveTranslation(TranslationStrategy):
    def __init__(self , to_lang = "en"):
        self.target_lang = to_lang

    def translate_blocks(
        self,
        text_blocks: List[dict],
        target_lang: str = None
    ) -> List[dict]:

        if not text_blocks:
            return text_blocks

        lang = target_lang or self.target_lang
        
        translator = Translator( to_lang=lang)

        for block in text_blocks:
            text = block.get("text", "").strip()

            if not text:
                continue

            try:
                translated = translator.translate(text)
                block["text"] = translated
            except Exception as e:
                print(f"[ERROR] Translation failed: {e}")
                block["text"] = text

        return text_blocks
