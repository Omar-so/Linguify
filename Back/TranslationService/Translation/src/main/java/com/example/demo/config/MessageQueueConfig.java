// config/MessageQueueConfig.java
package com.example.demo.config;

import com.example.demo.strategy.MessageQueue.BaseQueue;
import com.example.demo.strategy.MessageQueue.RedisStreams;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageQueueConfig {

    // switch between "redisStream", "kafka", "rabbitMQ" here
    @Bean
    @ConditionalOnProperty(name = "queue.type", havingValue = "redis", matchIfMissing = true)
    public BaseQueue redisQueue() {
        return new RedisStreams();
    }
}