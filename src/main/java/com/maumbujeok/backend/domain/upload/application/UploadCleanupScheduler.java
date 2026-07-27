package com.maumbujeok.backend.domain.upload.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UploadCleanupScheduler {
    private final UploadService uploadService;

    @Scheduled(fixedDelayString = "${storage.cleanup-delay:PT1M}")
    public void cleanup() {
        uploadService.markExpiredOrphans();
        uploadService.retryPendingDeletions();
    }
}
