package com.example.demo.service.UsersService;

import com.example.demo.DTO.WorkerSchema;
import com.example.demo.Entitty.*;
import com.example.demo.Exceptions.*;
import com.example.demo.repoistery.*;
import com.example.demo.strategy.CdnStategy.CdnBase;
import com.example.demo.strategy.MessageQueue.BaseQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsersService {

    @Autowired ImageRepository imageRepository;
    @Autowired ReportRepository reportRepository;
    @Autowired UserRepository usersRepository;
    @Autowired CdnBase cdnStrategy;
    @Autowired BaseQueue messageQueue;


    // ─── HELPER ───────────────────────────────────────────────────────────────

    private Long getCurrentUserId() {
        UserPrinciple userPrinciple = (UserPrinciple) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        return userPrinciple.getId();
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0)
            throw new InvalidPageException("Page number cannot be negative");
        if (size <= 0 || size > 100)
            throw new InvalidPageException("Page size must be between 1 and 100");
    }

    // ─── IMAGE ────────────────────────────────────────────────────────────────

    /**
     * Uploads image to CDN, saves record as QUEUED, pushes to Redis queue.
     * Worker will publish result to channel "process:{userId}".
     */
    public Images uploadAndQueue(MultipartFile file, String target_lang) throws Exception {
        Long userId = getCurrentUserId();

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new NoDataFoundException("User not found with id: " + userId))
                ;

        String url = cdnStrategy.upload(file);

        Images image = Images.builder()
                .image_before(url)
                .user(user)
                .build();

        Images saved = imageRepository.save(image);

        WorkerSchema data = new WorkerSchema(); // ✅ initialize locally, not as class field
        data.setImage_id(saved.getId());
        data.setImage_url(url);
        data.setUer_id(userId);
        data.setTarget_lang(target_lang);


        // push to queue — worker echoes userId back in publish channel
        messageQueue.sendMessage(data);

        return saved;
    }

    public void deleteImage(Long imageId) throws Exception {
        Long userId = getCurrentUserId();

        Images image = imageRepository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new NoDataFoundException("Image not found with id: " + imageId));

        cdnStrategy.delete(image.getImage_after());
        imageRepository.delete(image);
    }

    // ─── HISTORY ──────────────────────────────────────────────────────────────

    public Page<Images> getAllHistory(int page, int size) {
        validatePageRequest(page, size);
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Images> result = imageRepository.findAllHistoryByUserId(userId, pageable);
        if (result.isEmpty())
            throw new NoDataFoundException("No history found");
        return result;
    }

    public void deleteHistory(Long imageId) throws Exception {
        Long userId = getCurrentUserId();

        Images image = imageRepository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new NoDataFoundException("Image not found with id: " + imageId));

        cdnStrategy.delete(image.getImage_after());

        int deleted = imageRepository.deleteImageByIdAndUserId(imageId, userId);
        if (deleted == 0)
            throw new NoDataFoundException("Failed to delete image with id: " + imageId);
    }

    // ─── REPORT ───────────────────────────────────────────────────────────────

    public Reports createReport(Long imageId, Reports report) {
        Long userId = getCurrentUserId();

        Images image = imageRepository.findByIdAndUserId(imageId, userId)
                .orElseThrow(() -> new NoDataFoundException("Image not found with id: " + imageId));

        report.setImage(image);
        report.setResolved(false);
        return reportRepository.save(report);
    }

    public Page<Reports> getAllReports(int page, int size) {
        validatePageRequest(page, size);
        Long userId = getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Reports> result = reportRepository.findAllByUserId(userId, pageable);
        if (result.isEmpty())
            throw new NoDataFoundException("No reports found");
        return result;
    }

    public Reports updateReportDescription(Long reportId, String description) {
        Long userId = getCurrentUserId();
        int updated = reportRepository.updateDescription(reportId, description, userId);
        if (updated == 0)
            throw new NoDataFoundException("Report not found with id: " + reportId);
        return reportRepository.findByIdAndUserId(reportId, userId)
                .orElseThrow(() -> new NoDataFoundException("Report not found"));
    }

    public void deleteReport(Long reportId) {
        Long userId = getCurrentUserId();
        int deleted = reportRepository.deleteByIdAndUserId(reportId, userId);
        if (deleted == 0)
            throw new NoDataFoundException("Report not found with id: " + reportId);
    }
}