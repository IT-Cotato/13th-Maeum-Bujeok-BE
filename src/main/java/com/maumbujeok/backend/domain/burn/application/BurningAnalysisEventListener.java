package com.maumbujeok.backend.domain.burn.application;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;
@Component @RequiredArgsConstructor @Slf4j
public class BurningAnalysisEventListener { private final BurningAnalysisOrchestrator orchestrator; @Async("diaryAnalysisExecutor") @TransactionalEventListener(phase=TransactionPhase.AFTER_COMMIT) public void handle(BurningAnalysisRequestedEvent event){orchestrator.analyze(event.analysisId(),event.inputRevision());} }