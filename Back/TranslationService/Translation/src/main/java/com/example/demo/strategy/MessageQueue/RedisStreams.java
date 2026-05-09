package com.example.demo.strategy.MessageQueue;

import com.example.demo.DTO.WorkerSchema;
import com.example.demo.Exceptions.NoDataFoundException;
import com.example.demo.Subscriber.ResultSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class RedisStreams implements BaseQueue {

    @Autowired StringRedisTemplate redisTemplate;
    @Autowired RedisConnectionFactory factory;
    @Autowired ResultSubscriber resultSubscriber;

    private static final String STREAM_KEY = "image:processing:stream";
    private final String serverId;

    public RedisStreams() {
        this.serverId = generateServerId();
    }

    private String generateServerId() {
        try {
            return InetAddress.getLocalHost().getHostName() ;
        } catch (UnknownHostException e) {
            return "unknown-server-" + java.util.UUID.randomUUID();
        }
    }

    /**
     * Pushes imageId + userId to Redis stream.
     * Worker reads this, processes the image,
     * then publishes result to channel "process:{userId}".
     */
    @Override
    public void sendMessage(WorkerSchema data) throws NoDataFoundException {
        Map<String, String> message = new HashMap<>();
        message.put("image_url",   data.getImage_url());
        message.put("job_id", data.getImage_id().toString());
        message.put("user_id",  data.getUer_id().toString());
        message.put("target_language", data.getTarget_lang());
        message.put("server_id" , serverId);

        redisTemplate.opsForStream().add(STREAM_KEY, message);
    }

    @Override
    public void subscribe() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        // listens to "process:{userId}" — pattern covers all users
        container.addMessageListener(resultSubscriber, new PatternTopic("serverId"));
        container.start();
    }
}