package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.ai.AiAnalysisException;
import com.maumbujeok.backend.domain.diary.ai.AiCallResult;
import com.maumbujeok.backend.domain.diary.ai.AiDiaryAnalysisClient;
import com.maumbujeok.backend.domain.diary.ai.DiaryAiRequest;
import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiaryAnalysisOrchestrator {
    private final DiaryAnalysisStateService stateService;
    private final DiaryAnalysisInputLoader inputLoader;
    private final SajuContextProvider sajuContextProvider;
    private final AiDiaryAnalysisClient aiClient;
    private final NegativeIntensityPolicy intensityPolicy;
    private final AiResponseSafetyGuard safetyGuard;
    private final DiaryAnalysisFallbackFactory fallbackFactory;

    public void analyze(Long analysisId) {
        if (!stateService.begin(analysisId)) return;

        DiaryAnalysisInput input = inputLoader.load(analysisId);
        try {
            AiCallResult call = aiClient.analyze(new DiaryAiRequest(
                    input.content(), input.selectedEmotion(), sajuContextProvider.neutralContext()
            ));
            DiaryAiResult result = call.result();
            safetyGuard.validate(result);
            SafetyLevel safety = safetyGuard.resolveSafetyLevel(input.content(), result.safetyLevel());
            int score = intensityPolicy.calculate(result.negativeIntensity(), input.content(), input.selectedEmotion());
            boolean recommended = safety != SafetyLevel.CRISIS && intensityPolicy.recommendsSalpuri(score);
            stateService.complete(analysisId, result, score, recommended, call.attempts(), safety);
        } catch (RuntimeException exception) {
            completeWithFallback(analysisId, input, exception);
        }
    }

    private void completeWithFallback(Long analysisId, DiaryAnalysisInput input, RuntimeException cause) {
        int attempts = cause instanceof AiAnalysisException aiException ? aiException.getAttempts() : 1;
        String code = cause instanceof AiAnalysisException aiException ? aiException.getFailureCode() : "AI_OUTPUT_REJECTED";
        try {
            SafetyLevel safety = safetyGuard.resolveSafetyLevel(input.content(), SafetyLevel.NORMAL);
            DiaryAiResult fallback = fallbackFactory.create(input, safety);
            safetyGuard.validate(fallback);
            int score = intensityPolicy.calculate(fallback.negativeIntensity(), input.content(), input.selectedEmotion());
            boolean recommended = safety != SafetyLevel.CRISIS && intensityPolicy.recommendsSalpuri(score);
            stateService.fallback(analysisId, fallback, score, recommended, attempts, code, safety);
        } catch (RuntimeException fallbackFailure) {
            stateService.fail(analysisId, attempts, "FALLBACK_FAILED");
        }
    }
}
