from app.strategies.translation.naive import NaiveTranslation
from app.strategies.translation.base import TranslationStrategy


def get_translation_stratgy(name: str) -> TranslationStrategy:
    if name == "Naive":
        return NaiveTranslation()  
    else:
        raise ValueError(f"Unknown CDN strategy: {name}")                    