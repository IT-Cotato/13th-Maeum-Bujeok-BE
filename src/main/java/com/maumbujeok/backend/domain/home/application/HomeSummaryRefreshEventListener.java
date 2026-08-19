package com.maumbujeok.backend.domain.home.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class HomeSummaryRefreshEventListener {

    private final HomeSummaryService homeSummaryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(HomeSummaryRefreshRequestedEvent event) {
        log.info("Handling synchronous AFTER_COMMIT home summary refresh for member={}", maskPhoneNumber(event.memberPhoneNumber()));
        try {
            homeSummaryService.refreshForToday(event.memberPhoneNumber());
            log.info("Successfully refreshed home summary for member={}", maskPhoneNumber(event.memberPhoneNumber()));
        } catch (Exception e) {
            log.error("Failed to refresh home summary after commit for member={}. Error: {}",
                    maskPhoneNumber(event.memberPhoneNumber()), e.getMessage(), e);
        }
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(phoneNumber.length() - 4);
    }
}
