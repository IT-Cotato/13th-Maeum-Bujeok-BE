package com.maumbujeok.backend.domain.diary.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class DiaryAnalysisEventListener {
    private final DiaryAnalysisOrchestrator orchestrator;

    @Async("diaryAnalysisExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(DiaryCreatedEvent event) {
        orchestrator.analyze(event.analysisId());
    }
}
