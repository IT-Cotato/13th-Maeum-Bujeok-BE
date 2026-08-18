package com.maumbujeok.backend.domain.saju.application;

import com.maumbujeok.backend.domain.saju.ai.SajuAiCallResult;
import com.maumbujeok.backend.domain.saju.ai.SajuAiClient;
import com.maumbujeok.backend.domain.saju.ai.SajuAiException;
import com.maumbujeok.backend.domain.saju.ai.SajuAiRequest;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysisStatus;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SajuAnalysisOrchestrator {
    private static final String INVALID_RESPONSE_CODE = "AI_INVALID_RESPONSE";
    private static final String PROVIDER_ERROR_CODE = "AI_PROVIDER_ERROR";

    private final SajuAnalysisRepository repository;
    private final SajuAiClient aiClient;
    private final SajuAnalysisStateService stateService;

    public void analyze(Long analysisId, long requestSequence) {
        try {
            boolean started = stateService.begin(analysisId, requestSequence);
            if (!started) {
                log.info("Saju analysis skipped analysisId={} requestSequence={} reason=missing_or_already_started",
                        analysisId, requestSequence);
                return;
            }

            SajuAnalysis analysis = repository.findById(analysisId).orElse(null);
            if (analysis == null
                    || analysis.getStatus() != SajuAnalysisStatus.PROCESSING
                    || analysis.getRequestSequence() != requestSequence) {
                log.info("Saju analysis skipped analysisId={} requestSequence={} reason=missing_after_begin_or_not_processing",
                        analysisId, requestSequence);
                return;
            }

            SajuAiCallResult call = aiClient.analyze(toRequest(analysis));
            boolean completed = stateService.complete(analysisId, requestSequence, call.result(), call.attempts());
            log.info("Saju analysis completed analysisId={} requestSequence={} attempts={} model={} stored={}",
                    analysisId, requestSequence, call.attempts(), call.result().modelName(), completed);
        } catch (Exception exception) {
            markFailed(analysisId, requestSequence, exception);
        }
    }

    private SajuAiRequest toRequest(SajuAnalysis analysis) {
        return new SajuAiRequest(
                analysis.getBirthDate(),
                analysis.getGender(),
                analysis.getCalendarType(),
                analysis.getBirthTime()
        );
    }

    private void markFailed(Long analysisId, long requestSequence, Exception exception) {
        String failureCode = resolveFailureCode(exception);
        int attempts = resolveAttempts(exception);

        try {
            boolean failed = stateService.fail(analysisId, requestSequence, attempts, failureCode);
            log.warn("Saju analysis failed analysisId={} requestSequence={} failureCode={} attempts={} stored={} exceptionType={}",
                    analysisId, requestSequence, failureCode, attempts, failed, exception.getClass().getSimpleName(), exception);
        } catch (Exception persistenceException) {
            log.error(
                    "Saju analysis failure state could not be stored analysisId={} requestSequence={} failureCode={} attempts={} exceptionType={} persistenceExceptionType={}",
                    analysisId,
                    requestSequence,
                    failureCode,
                    attempts,
                    exception.getClass().getSimpleName(),
                    persistenceException.getClass().getSimpleName(),
                    persistenceException
            );
        }
    }

    private String resolveFailureCode(Exception exception) {
        if (exception instanceof SajuAiException aiException) {
            return aiException.getFailureCode();
        }
        if (exception instanceof IllegalArgumentException || exception instanceof IllegalStateException) {
            return INVALID_RESPONSE_CODE;
        }
        return PROVIDER_ERROR_CODE;
    }

    private int resolveAttempts(Exception exception) {
        if (exception instanceof SajuAiException aiException) {
            return Math.max(aiException.getAttempts(), 1);
        }
        return 1;
    }
}
