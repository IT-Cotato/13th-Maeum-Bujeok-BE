package com.maumbujeok.backend.domain.diary.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class DiaryAnalysisEventListener {
    private final DiaryAnalysisOrchestrator orchestrator;

    @Async("diaryAnalysisExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DiaryAnalysisRequestedEvent event) {
        log.info(
                "Diary analysis event received analysisId={} inputRevision={}",
                event.analysisId(),
                event.inputRevision()
        );
        orchestrator.analyze(event.analysisId(), event.inputRevision());
        log.info(
                "Diary analysis event handled analysisId={} inputRevision={}",
                event.analysisId(),
                event.inputRevision()
        );
    }
}
