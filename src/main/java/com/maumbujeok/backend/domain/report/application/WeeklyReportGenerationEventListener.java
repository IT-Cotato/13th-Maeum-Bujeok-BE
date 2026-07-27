package com.maumbujeok.backend.domain.report.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportGenerationEventListener {
    private final WeeklyReportOrchestrator orchestrator;

    @Async("weeklyReportExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(WeeklyReportGenerationRequestedEvent event) {
        log.info("Weekly report generation event received reportId={} generationSequence={}",
                event.reportId(), event.generationSequence());
        orchestrator.generate(event.reportId(), event.generationSequence());
        log.info("Weekly report generation event handled reportId={} generationSequence={}",
                event.reportId(), event.generationSequence());
    }
}
