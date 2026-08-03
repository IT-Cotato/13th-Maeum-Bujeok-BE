package com.maumbujeok.backend.domain.burn.application;

import com.maumbujeok.backend.domain.burn.ai.*;
import com.maumbujeok.backend.domain.burn.domain.*;
import com.maumbujeok.backend.domain.burn.repository.BurningAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BurningAnalysisOrchestrator {
    private final BurningAnalysisRepository analyses;
    private final BurningAiClient client;
    private final BurningFallbackFactory fallbackFactory;

    public void analyze(Long analysisId, int revision) {
        if (!begin(analysisId, revision)) return;
        try {
            BurningAnalysis analysis = analyses.findById(analysisId).orElseThrow();
            BurningAiResult result = client.analyze(new BurningAiRequest(analysis.getBurning().getSourceContent()));
            complete(analysisId, revision, result);
        } catch (RuntimeException exception) {
            String failureCode = exception instanceof BurningAiAnalysisException
                    ? exception.getMessage() : exception.getClass().getSimpleName();
            completeWithFallback(analysisId, revision, failureCode);
            log.warn("Burning analysis used local fallback analysisId={} revision={} failureCode={}", analysisId, revision, failureCode);
        }
    }

    @Transactional
    public boolean begin(Long id, int revision) {
        return analyses.findByIdForUpdate(id).map(a -> a.markProcessing(revision)).orElse(false);
    }

    @Transactional
    public boolean complete(Long id, int revision, BurningAiResult result) {
        return analyses.findByIdForUpdate(id).map(a -> a.complete(revision, result)).orElse(false);
    }

    @Transactional
    public boolean completeWithFallback(Long id, int revision, String failureCode) {
        BurningAiResult fallback = fallbackFactory.create(failureCode);
        return analyses.findByIdForUpdate(id).map(a -> a.completeWithFallback(revision, fallback, failureCode)).orElse(false);
    }
}