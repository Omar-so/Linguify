from app.strategies.cdn.local import LocalCDNStrategy
from  app.strategies.cdn.cloudinary import CloudinaryCDNStrategy
from  app.strategies.cdn.base import CDNStrategy
def get_cdn_strategy(name: str) -> CDNStrategy:
    if name == "local":
        return LocalCDNStrategy()
    elif name == "s3":
        return S3CDNStrategy()
    elif name == "cloudinary":
        return CloudinaryCDNStrategy()
    else:
        raise ValueError(f"Unknown CDN strategy: {name}")