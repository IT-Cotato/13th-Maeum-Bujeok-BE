package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.ai.AiAnalysisException;
import com.maumbujeok.backend.domain.diary.ai.AiCallResult;
import com.maumbujeok.backend.domain.diary.ai.AiDiaryAnalysisClient;
import com.maumbujeok.backend.domain.diary.ai.DiaryAiRequest;
import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryAnalysisOrchestrator {
    private static final int MAX_OUTPUT_QUALITY_ATTEMPTS = 2;

    private final DiaryAnalysisStateService stateService;
    private final DiaryAnalysisInputLoader inputLoader;
    private final SajuContextProvider sajuContextProvider;
    private final AiDiaryAnalysisClient aiClient;
    private final NegativeIntensityPolicy intensityPolicy;
    private final AiResponseSafetyGuard safetyGuard;
    private final DiaryAnalysisFallbackFactory fallbackFactory;

    public void analyze(Long analysisId, long inputRevision) {
        if (!stateService.begin(analysisId, inputRevision)) {
            log.info(
                    "Diary analysis skipped analysisId={} inputRevision={} reason=not_current_pending",
                    analysisId,
                    inputRevision
            );
            return;
        }

        log.info(
                "Diary analysis started analysisId={} inputRevision={}",
                analysisId,
                inputRevision
        );

        try {
            DiaryAnalysisInput input = inputLoader.load(analysisId, inputRevision);
            AiCallResult call = analyzeWithQualityRetry(analysisId, new DiaryAiRequest(
                    input.content(),
                    input.selectedEmotion(),
                    sajuContextProvider.getContext(input.memberPhoneNumber())
            ));
            DiaryAiResult result = call.result();
            SafetyLevel safety = safetyGuard.resolveSafetyLevel(input.content(), result.safetyLevel());
            int score = intensityPolicy.calculate(
                    result.negativeIntensity(),
                    input.content(),
                    input.selectedEmotion()
            );
            boolean recommended = safety != SafetyLevel.CRISIS
                    && intensityPolicy.recommendsSalpuri(score);
            boolean saved = stateService.complete(
                    analysisId,
                    inputRevision,
                    result,
                    score,
                    recommended,
                    call.attempts(),
                    safety
            );
            if (!saved) {
                log.info(
                        "Diary analysis result ignored analysisId={} inputRevision={} reason=stale",
                        analysisId,
                        inputRevision
                );
                return;
            }
            log.info(
                    "Diary analysis completed analysisId={} inputRevision={} model={} attempts={} safetyLevel={} negativeIntensity={} salpuriRecommended={}",
                    analysisId,
                    inputRevision,
                    result.modelName(),
                    call.attempts(),
                    safety,
                    score,
                    recommended
            );
        } catch (StaleDiaryAnalysisException exception) {
            log.info(
                    "Diary analysis stopped analysisId={} inputRevision={} reason=stale_input",
                    analysisId,
                    inputRevision
            );
        } catch (RuntimeException exception) {
            handleFailure(analysisId, inputRevision, exception);
        }
    }

    private void handleFailure(Long analysisId, long inputRevision, RuntimeException exception) {
        String failureCode = exception instanceof AiAnalysisException aiException
                ? aiException.getFailureCode()
                : "AI_OUTPUT_REJECTED";
        int attempts = exception instanceof AiAnalysisException aiException
                ? aiException.getAttempts()
                : 1;
        log.warn(
                "Diary analysis switching to fallback analysisId={} inputRevision={} failureCode={} attempts={} exceptionType={}",
                analysisId,
                inputRevision,
                failureCode,
                attempts,
                exception.getClass().getSimpleName()
        );

        DiaryAnalysisInput input;
        try {
            input = inputLoader.load(analysisId, inputRevision);
        } catch (StaleDiaryAnalysisException stale) {
            log.info(
                    "Diary analysis fallback skipped analysisId={} inputRevision={} reason=stale_input",
                    analysisId,
                    inputRevision
            );
            return;
        } catch (RuntimeException inputFailure) {
            stateService.fail(analysisId, inputRevision, 1, "ANALYSIS_INPUT_ERROR");
            log.error(
                    "Diary analysis failed analysisId={} inputRevision={} failureCode=ANALYSIS_INPUT_ERROR exceptionType={}",
                    analysisId,
                    inputRevision,
                    inputFailure.getClass().getSimpleName()
            );
            return;
        }
        completeWithFallback(analysisId, inputRevision, input, exception);
    }

    private AiCallResult analyzeWithQualityRetry(Long analysisId, DiaryAiRequest request) {
        int totalAttempts = 0;
        IllegalArgumentException lastQualityFailure = null;

        for (int qualityAttempt = 1; qualityAttempt <= MAX_OUTPUT_QUALITY_ATTEMPTS; qualityAttempt++) {
            AiCallResult call;
            try {
                call = aiClient.analyze(request);
            } catch (AiAnalysisException exception) {
                throw new AiAnalysisException(
                        exception.getFailureCode(),
                        totalAttempts + exception.getAttempts(),
                        exception
                );
            }
            totalAttempts += call.attempts();

            try {
                safetyGuard.validate(call.result());
                return new AiCallResult(call.result(), totalAttempts);
            } catch (IllegalArgumentException qualityFailure) {
                lastQualityFailure = qualityFailure;
                boolean retrying = qualityAttempt < MAX_OUTPUT_QUALITY_ATTEMPTS;
                log.warn(
                        "Diary AI output quality rejected analysisId={} qualityAttempt={}/{} totalAttempts={} reason={} retrying={}",
                        analysisId,
                        qualityAttempt,
                        MAX_OUTPUT_QUALITY_ATTEMPTS,
                        totalAttempts,
                        qualityFailure.getMessage(),
                        retrying
                );
            }
        }

        throw new AiAnalysisException("AI_OUTPUT_REJECTED", totalAttempts, lastQualityFailure);
    }

    private void completeWithFallback(
            Long analysisId,
            long inputRevision,
            DiaryAnalysisInput input,
            RuntimeException cause
    ) {
        int attempts = cause instanceof AiAnalysisException aiException
                ? aiException.getAttempts()
                : 1;
        String code = cause instanceof AiAnalysisException aiException
                ? aiException.getFailureCode()
                : "AI_OUTPUT_REJECTED";
        try {
            SafetyLevel safety = safetyGuard.resolveSafetyLevel(
                    input.content(),
                    SafetyLevel.NORMAL
            );
            DiaryAiResult fallback = fallbackFactory.create(input, safety);
            safetyGuard.validate(fallback);
            int score = intensityPolicy.calculate(
                    fallback.negativeIntensity(),
                    input.content(),
                    input.selectedEmotion()
            );
            boolean recommended = safety != SafetyLevel.CRISIS
                    && intensityPolicy.recommendsSalpuri(score);
            boolean saved = stateService.fallback(
                    analysisId,
                    inputRevision,
                    fallback,
                    score,
                    recommended,
                    attempts,
                    code,
                    safety
            );
            if (!saved) {
                log.info(
                        "Diary analysis fallback ignored analysisId={} inputRevision={} reason=stale",
                        analysisId,
                        inputRevision
                );
                return;
            }
            log.warn(
                    "Diary analysis fallback completed analysisId={} inputRevision={} failureCode={} attempts={} safetyLevel={} negativeIntensity={}",
                    analysisId,
                    inputRevision,
                    code,
                    attempts,
                    safety,
                    score
            );
        } catch (RuntimeException fallbackFailure) {
            stateService.fail(
                    analysisId,
                    inputRevision,
                    attempts,
                    "FALLBACK_FAILED"
            );
            log.error(
                    "Diary analysis fallback failed analysisId={} inputRevision={} failureCode=FALLBACK_FAILED exceptionType={}",
                    analysisId,
                    inputRevision,
                    fallbackFailure.getClass().getSimpleName()
            );
        }
    }
}
