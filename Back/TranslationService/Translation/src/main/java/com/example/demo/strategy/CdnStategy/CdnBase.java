// strategy/CdnStrategy.java
package com.example.demo.strategy.CdnStategy;

import org.springframework.web.multipart.MultipartFile;

public interface CdnBase {
    String upload(MultipartFile file) throws Exception;
    void delete(String url) throws Exception;
}