package com.maumbujeok.backend.domain.saju.application;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class SajuAnalysisEventListener {
    @Qualifier("sajuAnalysisExecutor")
    private final TaskExecutor sajuAnalysisExecutor;
    private final SajuAnalysisOrchestrator orchestrator;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SajuAnalysisRequestedEvent event) {
        try {
            sajuAnalysisExecutor.execute(() -> analyze(event));
        } catch (TaskRejectedException exception) {
            log.warn("Saju analysis executor saturated analysisId={} requestSequence={} retrying_in_caller_thread=true",
                    event.analysisId(), event.requestSequence(), exception);
            analyze(event);
        }
    }

    private void analyze(SajuAnalysisRequestedEvent event) {
        log.info("Saju analysis event received analysisId={} requestSequence={}",
                event.analysisId(), event.requestSequence());
        orchestrator.analyze(event.analysisId(), event.requestSequence());
        log.info("Saju analysis event handled analysisId={} requestSequence={}",
                event.analysisId(), event.requestSequence());
    }
}
