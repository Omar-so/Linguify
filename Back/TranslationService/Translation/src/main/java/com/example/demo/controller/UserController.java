package com.example.demo.controller;

import com.example.demo.Entitty.Images;
import com.example.demo.Entitty.Reports;
import com.example.demo.Entitty.UserPrinciple;
import com.example.demo.service.EmitterService.EmitterService;
import com.example.demo.service.UsersService.UsersService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;

@RestController
@RequestMapping("/Api/User")
@SecurityRequirement(name = "bearerAuth")  // ADD THIS

public class UserController {

    @Autowired private UsersService usersService;
    @Autowired private EmitterService emitterService;

    // ─── IMAGE ────────────────────────────────────────────────────────────────

    /**
     * STEP 1 — Client opens SSE connection first.
     * Emitter stored by userId extracted from JWT.
     * Immediately sends WAITING event so client knows connection is alive.
     */
    @GetMapping(value = "/images/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter openStream(Authentication auth) {
        Long userId = ((UserPrinciple) auth.getPrincipal()).getId();
        return emitterService.create(userId);
    }

    /**
     * STEP 2 — Client uploads image after SSE is open.
     * Saves to CDN + DB, pushes to Redis queue.
     * Worker will process and publish back to "process:{userId}".
     */
    @PostMapping(value = "/images/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @RequestParam("targetLang") String targetLang,
            @RequestPart("file") MultipartFile file
    )  throws Exception {

        Images saved = usersService.uploadAndQueue(file , targetLang);

        return ResponseEntity.status(202).body(Map.of(
                "message", "Image uploaded, processing started",
                "imageId", saved.getId()
        ));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) throws Exception {
        usersService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    // ─── HISTORY ──────────────────────────────────────────────────────────────

    @GetMapping("/images/history")
    public ResponseEntity<Page<Images>> getAllHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(usersService.getAllHistory(page, size));
    }

    @DeleteMapping("/images/history/{imageId}")
    public ResponseEntity<Void> deleteHistory(@PathVariable Long imageId) throws Exception {
        usersService.deleteHistory(imageId);
        return ResponseEntity.noContent().build();
    }

    // ─── REPORT ───────────────────────────────────────────────────────────────

    @PostMapping("/images/{imageId}/reports")
    public ResponseEntity<Reports> createReport(
            @PathVariable Long imageId,
            @RequestBody Reports report) {
        return ResponseEntity.status(201).body(usersService.createReport(imageId, report));
    }

    @GetMapping("/reports")
    public ResponseEntity<Page<Reports>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(usersService.getAllReports(page, size));
    }

    @PatchMapping("/reports/{reportId}")
    public ResponseEntity<Reports> updateReport(
            @PathVariable Long reportId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(usersService.updateReportDescription(reportId, body.get("description")));
    }

    @DeleteMapping("/reports/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long reportId) {
        usersService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }
}