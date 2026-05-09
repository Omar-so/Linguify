from abc import ABC, abstractmethod
from typing import Callable, Awaitable


class MessageBroker(ABC):
    """
    Abstract base class for all message broker implementations.
    Every broker (Redis, SQS, Kafka, etc.) must implement this contract.
    """

    @abstractmethod
    async def connect(self) -> None:
        """Establish connection and initialize any required infrastructure."""
        ...

    @abstractmethod
    async def consume(self, process_job_coro: Callable[[dict], Awaitable[dict]]) -> None:
        """
        Start consuming messages and dispatch each one to process_job_coro.
        This is a long-running loop — run it as a background task.
        """
        ...

    @abstractmethod
    async def publish_result(self, result: dict) -> None:
        """Publish a processed job result to the result stream/queue/topic."""
        ...

    @abstractmethod
    async def close(self) -> None:
        """Gracefully shut down the broker connection."""
        ...