package com.maumbujeok.backend.domain.report.ai;

import com.maumbujeok.backend.global.ai.client.AiGateway;
import com.maumbujeok.backend.global.ai.dto.AiExecutionResult;
import com.maumbujeok.backend.global.ai.dto.AiStructuredRequest;
import com.maumbujeok.backend.global.ai.exception.AiClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiWeeklyReportAiProvider implements WeeklyReportAiProvider {
    private final AiGateway aiGateway;
    private final WeeklyReportAiPromptFactory promptFactory;

    @Override
    public WeeklyReportAiCallResult analyzeWeeklyReport(WeeklyReportAiRequest request) {
        try {
            AiExecutionResult<WeeklyReportAiResponse> execution = aiGateway.generateStructured(
                    new AiStructuredRequest(
                            "weekly-report-summary",
                            promptFactory.instructions(),
                            promptFactory.input(request),
                            "weekly_report_summary",
                            WeeklyReportAiResponseSchema.schema(),
                            null,
                            700,
                            WeeklyReportPromptVersion.VALUE
                    ),
                    WeeklyReportAiResponse.class
            );
            return new WeeklyReportAiCallResult(execution.output().toResult(execution.model()), execution.attempts());
        } catch (AiClientException exception) {
            throw new WeeklyReportAiException(
                    exception.getFailureCode().name(),
                    exception.getAttempts(),
                    exception
            );
        }
    }
}
