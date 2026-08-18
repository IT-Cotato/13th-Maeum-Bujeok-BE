package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiCallResult;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiClient;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiException;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiRequest;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportOrchestrator {
    private static final int MAX_OUTPUT_QUALITY_ATTEMPTS = 2;

    private final WeeklyReportGenerationStateService stateService;
    private final WeeklyReportGenerationInputLoader inputLoader;
    private final WeeklyReportAiClient aiClient;
    private final WeeklyReportResponseSafetyGuard safetyGuard;
    private final WeeklyReportFallbackFactory fallbackFactory;
    private final NextWeekFlowService nextWeekFlowService;

    public void generate(Long reportId, int generationSequence) {
        if (!stateService.begin(reportId, generationSequence)) {
            log.info("Weekly report generation skipped reportId={} generationSequence={} reason=already_started_or_stale",
                    reportId, generationSequence);
            return;
        }

        log.info("Weekly report generation started reportId={} generationSequence={}", reportId, generationSequence);

        try {
            WeeklyReportGenerationInput input = inputLoader.load(reportId);
            WeeklyReportAiCallResult call = generateWithQualityRetry(reportId, generationSequence, toRequest(input));
            boolean completed = stateService.complete(reportId, generationSequence, call.result(), call.attempts());
            log.info("Weekly report generation completed reportId={} generationSequence={} attempts={} model={} stored={}",
                    reportId, generationSequence, call.attempts(), call.result().modelName(), completed);
            if (completed) {
                nextWeekFlowService.refreshIfEligible(reportId);
            }
        } catch (RuntimeException exception) {
            String failureCode = exception instanceof WeeklyReportAiException aiException
                    ? aiException.getFailureCode()
                    : "AI_OUTPUT_REJECTED";
            int attempts = exception instanceof WeeklyReportAiException aiException
                    ? aiException.getAttempts()
                    : 1;
            log.warn("Weekly report generation switching to fallback reportId={} generationSequence={} failureCode={} attempts={} exceptionType={}",
                    reportId, generationSequence, failureCode, attempts, exception.getClass().getSimpleName());
            WeeklyReportGenerationInput input;
            try {
                input = inputLoader.load(reportId);
            } catch (RuntimeException inputFailure) {
                stateService.fail(reportId, generationSequence, 1, "REPORT_INPUT_ERROR");
                log.error("Weekly report generation failed reportId={} generationSequence={} failureCode=REPORT_INPUT_ERROR exceptionType={}",
                        reportId, generationSequence, inputFailure.getClass().getSimpleName());
                return;
            }
            completeWithFallback(reportId, generationSequence, input, exception);
        }
    }

    private WeeklyReportAiCallResult generateWithQualityRetry(Long reportId, int generationSequence, WeeklyReportAiRequest request) {
        int totalAttempts = 0;
        IllegalArgumentException lastQualityFailure = null;

        for (int qualityAttempt = 1; qualityAttempt <= MAX_OUTPUT_QUALITY_ATTEMPTS; qualityAttempt++) {
            WeeklyReportAiCallResult call;
            try {
                call = aiClient.generate(request);
            } catch (WeeklyReportAiException exception) {
                throw new WeeklyReportAiException(
                        exception.getFailureCode(),
                        totalAttempts + exception.getAttempts(),
                        exception
                );
            }
            totalAttempts += call.attempts();

            try {
                safetyGuard.validate(call.result());
                return new WeeklyReportAiCallResult(call.result(), totalAttempts);
            } catch (IllegalArgumentException qualityFailure) {
                lastQualityFailure = qualityFailure;
                boolean retrying = qualityAttempt < MAX_OUTPUT_QUALITY_ATTEMPTS;
                log.warn("Weekly report AI output quality rejected reportId={} generationSequence={} qualityAttempt={}/{} totalAttempts={} reason={} retrying={}",
                        reportId, generationSequence, qualityAttempt, MAX_OUTPUT_QUALITY_ATTEMPTS,
                        totalAttempts, qualityFailure.getMessage(), retrying);
            }
        }

        throw new WeeklyReportAiException("AI_OUTPUT_REJECTED", totalAttempts, lastQualityFailure);
    }

    private void completeWithFallback(Long reportId, int generationSequence, WeeklyReportGenerationInput input, RuntimeException cause) {
        int attempts = cause instanceof WeeklyReportAiException aiException ? aiException.getAttempts() : 1;
        String code = cause instanceof WeeklyReportAiException aiException ? aiException.getFailureCode() : "AI_OUTPUT_REJECTED";
        try {
            WeeklyReportAiResult fallback = fallbackFactory.create(input);
            safetyGuard.validate(fallback);
            boolean stored = stateService.fallback(reportId, generationSequence, fallback, attempts, code);
            log.warn("Weekly report fallback completed reportId={} generationSequence={} failureCode={} attempts={} stored={}",
                    reportId, generationSequence, code, attempts, stored);
            if (stored) {
                nextWeekFlowService.refreshIfEligible(reportId);
            }
        } catch (RuntimeException fallbackFailure) {
            stateService.fail(reportId, generationSequence, attempts, "FALLBACK_FAILED");
            log.error("Weekly report fallback failed reportId={} generationSequence={} failureCode=FALLBACK_FAILED exceptionType={}",
                    reportId, generationSequence, fallbackFailure.getClass().getSimpleName());
        }
    }

    private WeeklyReportAiRequest toRequest(WeeklyReportGenerationInput input) {
        return new WeeklyReportAiRequest(
                input.memberDisplayName(),
                input.periodStart(),
                input.periodEnd(),
                input.diaryEntries(),
                input.emotionSnapshot()
        );
    }
}
