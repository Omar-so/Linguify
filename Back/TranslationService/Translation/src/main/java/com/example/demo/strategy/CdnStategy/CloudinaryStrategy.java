package com.example.demo.strategy.CdnStategy;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.example.demo.Exceptions.NoDataFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

 public class CloudinaryStrategy implements CdnBase {

    @Autowired
    private Cloudinary cloudinaryClient;

    @Override
    public String upload(MultipartFile file) throws Exception {
        // validate file
        if (file.isEmpty()) {
            throw new NoDataFoundException("File is empty");
        }

        // upload to cloudinary

        Map uploadResult = cloudinaryClient.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "demo/images",
                        "resource_type", "image",
                        "allowed_formats", "jpg,png,jpeg",
                        "transformation", new Transformation()
                                .quality("auto")
                                .fetchFormat("auto")   // ✅ correct method name
                )
        );
        // return the secure URL
        return uploadResult.get("secure_url").toString();
    }

    @Override
    public void delete(String url) throws Exception {
        // extract public_id from URL
        // URL format: https://res.cloudinary.com/{cloud}/image/upload/v123/{folder}/{publicId}.jpg
        if (url == null || url.isEmpty()) {
        return;
    }
        String publicId = extractPublicId(url);

        Map deleteResult = cloudinaryClient.uploader().destroy(
                publicId,
                ObjectUtils.asMap("resource_type", "image")
        );

        // check if delete was successful
        if (!"ok".equals(deleteResult.get("result"))) {
            throw new NoDataFoundException("Failed to delete image from Cloudinary: " + publicId);
        }
    }

    // extract public_id from cloudinary URL
    private String extractPublicId(String url) {
        // remove everything before /upload/
        String afterUpload = url.substring(url.indexOf("/upload/") + 8);

        // remove version if present (v1234567/)
        if (afterUpload.startsWith("v") && afterUpload.contains("/")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }

        // remove file extension
        int dotIndex = afterUpload.lastIndexOf(".");
        if (dotIndex != -1) {
            afterUpload = afterUpload.substring(0, dotIndex);
        }

        return afterUpload; // returns "demo/images/filename"
    }
}