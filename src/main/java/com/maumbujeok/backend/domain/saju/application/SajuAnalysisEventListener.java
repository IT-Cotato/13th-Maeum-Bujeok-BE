package com.maumbujeok.backend.domain.saju.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class SajuAnalysisEventListener {
    private final SajuAnalysisOrchestrator orchestrator;

    @Async("sajuAnalysisExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SajuAnalysisRequestedEvent event) {
        log.info("Saju analysis event received analysisId={} requestSequence={}",
                event.analysisId(), event.requestSequence());
        orchestrator.analyze(event.analysisId(), event.requestSequence());
        log.info("Saju analysis event handled analysisId={} requestSequence={}",
                event.analysisId(), event.requestSequence());
    }
}
