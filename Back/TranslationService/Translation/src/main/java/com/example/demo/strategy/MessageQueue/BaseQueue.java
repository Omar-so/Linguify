package com.example.demo.strategy.MessageQueue;

import com.example.demo.DTO.WorkerSchema;

public interface BaseQueue {
    void sendMessage(WorkerSchema workerSchema); // ← fix naming convention — lowercase
    void subscribe(); // ← fix naming convention — lowercase
}