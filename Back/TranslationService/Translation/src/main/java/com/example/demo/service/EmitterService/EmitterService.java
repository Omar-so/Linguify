package com.example.demo.service.EmitterService;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmitterService {

    // userId → their open SSE connection
    // ConcurrentHashMap because SSE thread and Redis listener thread both touch this
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /**
     * Creates emitter keyed by userId.
     * If user already has an open connection (e.g. double tab), close it first.
     * Immediately sends WAITING so client knows the connection is alive.
     */
    public SseEmitter create(Long userId) {
        SseEmitter existing = emitters.get(userId);
        if (existing != null) {
            existing.complete();             // close stale connection
        }

        SseEmitter emitter = new SseEmitter(120_0000L); // 2 min timeout

        emitters.put(userId, emitter);

        // cleanup on any terminal state
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(()   -> emitters.remove(userId));
        emitter.onError(e      -> emitters.remove(userId));

        // tell client the connection is ready and to proceed with upload
        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of(
                            "status",  "WAITING",
                            "message", "Connection ready, proceed with upload"
                    )));
        } catch (IOException e) {
            emitter.completeWithError(e);
            emitters.remove(userId);
        }

        return emitter;
    }

    /**
     * Called by ResultSubscriber when worker finishes.
     * Pushes result to the exact emitter for this userId, then closes the connection.
     */
    public void send(Long userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("result")
                        .data(data));
                emitter.complete();          // connection terminates here
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                emitters.remove(userId);
            }
        }
    }

    /**
     * Called by ResultSubscriber if worker fails.
     * Pushes error event then closes the connection.
     */
    public void sendError(Long userId, String errorMessage) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("status", "FAILED", "message", errorMessage)));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                emitters.remove(userId);
            }
        }
    }
}