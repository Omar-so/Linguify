package com.example.demo.Subscriber;

import com.example.demo.Entitty.Images;
import com.example.demo.Exceptions.NoDataFoundException;
import com.example.demo.repoistery.ImageRepository;
import com.example.demo.service.EmitterService.EmitterService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class ResultSubscriber implements MessageListener {

    @Autowired ImageRepository imageRepository;
    @Autowired EmitterService emitterService;
    @Autowired ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {

        System.out.println("\n================ REDIS MESSAGE RECEIVED ================");
        System.out.println("[DEBUG] Thread: " + Thread.currentThread().getName());
        System.out.println("[DEBUG] Raw channel: " + new String(message.getChannel()));
        System.out.println("[DEBUG] Raw body: " + new String(message.getBody()));

        Long userId = null;

        try {
            System.out.println("[DEBUG] Step 1: Parsing JSON...");

            Map<String, Object> result = objectMapper.readValue(
                    message.getBody(), new TypeReference<>() {}
            );

            System.out.println("[DEBUG] Parsed result: " + result);

            userId = Long.valueOf(result.get("user_id").toString());
            String processedUrl = result.get("image_url").toString();
            Long imageId = Long.valueOf(result.get("image_id").toString());

            System.out.println("[DEBUG] Extracted userId=" + userId);
            System.out.println("[DEBUG] imageId=" + imageId);
            System.out.println("[DEBUG] processedUrl=" + processedUrl);

            System.out.println("[DEBUG] Step 2: DB lookup imageId=" + imageId);

            Images image = imageRepository.findById(imageId)
                    .orElseThrow(() -> {
                        System.out.println("[ERROR] Image not found in DB: " + imageId);
                        return new NoDataFoundException("Image not found");
                    });

            System.out.println("[DEBUG] Image found: " + image.getId());

            image.setImage_after(processedUrl);

            System.out.println("[DEBUG] Step 3: saving DB update...");
            imageRepository.save(image);

            System.out.println("[DEBUG] Step 4: sending SSE event to user " + userId);

            emitterService.send(userId, Map.of(
                    "imageId", imageId,
                    "processedUrl", processedUrl,
                    "status", "DONE"
            ));

            System.out.println("[SUCCESS] Event sent to SSE for userId=" + userId);

        } catch (Exception e) {
            System.out.println("[FATAL] Exception inside ResultSubscriber:");
            e.printStackTrace();

            if (userId != null) {
                System.out.println("[DEBUG] Sending error SSE to userId=" + userId);
                emitterService.sendError(userId, "Processing failed: " + e.getMessage());
            }
        }

        System.out.println("================ END REDIS HANDLER ================\n");
    }
}