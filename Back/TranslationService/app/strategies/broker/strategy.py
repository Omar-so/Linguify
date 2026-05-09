
from app.strategies.broker.redis import RedisBroker
from app.strategies.broker.base import MessageBroker

def get_broker_stratgy(name: str) -> MessageBroker:
    if name == "redis":
        return RedisBroker()  
    else:
        raise ValueError(f"Unknown CDN strategy: {name}")                    