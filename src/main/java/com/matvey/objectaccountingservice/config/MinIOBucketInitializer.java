package com.matvey.objectaccountingservice.config;

import com.matvey.objectaccountingservice.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinIOBucketInitializer {

    private final StorageService storageService;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeBuckets() {
        log.info("Initializing MinIO buckets...");
        try {
            storageService.ensureBucketsExist();
            log.info("MinIO buckets initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize MinIO buckets", e);
        }
    }
}
