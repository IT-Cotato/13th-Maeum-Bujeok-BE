package com.maumbujeok.backend.domain.burn.application;

import com.maumbujeok.backend.domain.burn.ai.*;
import com.maumbujeok.backend.domain.burn.domain.BurningAnalysis;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BurningAnalysisOrchestrator {
    private final BurningAnalysisStateService stateService;
    private final BurningAiClient client;
    private final BurningFallbackFactory fallbackFactory;

    public void analyze(Long analysisId, int revision) {
        if (!stateService.begin(analysisId, revision)) return;
        try {
            BurningAnalysis analysis = stateService.find(analysisId);
            BurningAiResult result = client.analyze(new BurningAiRequest(analysis.getBurning().getSourceContent()));
            stateService.complete(analysisId, revision, result);
        } catch (RuntimeException exception) {
            String failureCode = exception instanceof BurningAiAnalysisException
                    ? exception.getMessage() : exception.getClass().getSimpleName();
            BurningAiResult fallback = fallbackFactory.create(failureCode);
            stateService.completeWithFallback(analysisId, revision, fallback, failureCode);
            log.warn("Burning analysis used local fallback analysisId={} revision={} failureCode={}", analysisId, revision, failureCode);
        }
    }
}