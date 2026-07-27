package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.diary.application.DiaryWeeklyReportRefreshRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiaryWeeklyReportRefreshEventListener {
    private final WeeklyReportService weeklyReportService;

    @Async("weeklyReportExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DiaryWeeklyReportRefreshRequestedEvent event) {
        log.info("Weekly report refresh requested by diary memberPhoneSuffix={} diaryDate={}",
                maskPhoneNumber(event.memberPhoneNumber()), event.diaryDate());
        weeklyReportService.refreshForDiaryEntry(event.memberPhoneNumber(), event.diaryDate());
        log.info("Weekly report refresh handled memberPhoneSuffix={} diaryDate={}",
                maskPhoneNumber(event.memberPhoneNumber()), event.diaryDate());
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(phoneNumber.length() - 4);
    }
}
