// config/CdnConfig.java
package com.example.demo.config;

import com.example.demo.strategy.CdnStategy.CdnBase;
import com.example.demo.strategy.CdnStategy.CloudinaryStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CdnConfig {

    @Bean
    @ConditionalOnProperty(name = "cdn.provider", havingValue = "cloudinary")
    public CdnBase cloudinaryCdn() {
        return new CloudinaryStrategy();
    }
}