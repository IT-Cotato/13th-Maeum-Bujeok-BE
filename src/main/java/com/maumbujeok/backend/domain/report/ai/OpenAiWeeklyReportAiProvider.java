package com.maumbujeok.backend.domain.report.ai;

import com.maumbujeok.backend.global.ai.client.AiGateway;
import com.maumbujeok.backend.global.ai.dto.AiExecutionResult;
import com.maumbujeok.backend.global.ai.dto.AiStructuredRequest;
import com.maumbujeok.backend.global.ai.exception.AiClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiWeeklyReportAiProvider implements WeeklyReportAiProvider {
    private final AiGateway aiGateway;
    private final WeeklyReportAiPromptFactory promptFactory;

    @Override
    public WeeklyReportAiCallResult analyzeWeeklyReport(WeeklyReportAiRequest request) {
        if (isRequestEmpty(request)) {
            log.info("Weekly report AI call skipped because diary count is 0 for member={}", request != null ? request.memberDisplayName() : "null");
            return new WeeklyReportAiCallResult(
                    new WeeklyReportAiResult(buildEmptyDiarySummary(request != null ? request.memberDisplayName() : null), "empty-fallback"),
                    1
            );
        }

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

    private boolean isRequestEmpty(WeeklyReportAiRequest request) {
        if (request == null) {
            return true;
        }
        if (request.diaryEntries() == null || request.diaryEntries().isEmpty()) {
            return true;
        }
        if (request.emotionSnapshot() != null && request.emotionSnapshot().totalDiaryCount() == 0) {
            return true;
        }
        return false;
    }

    private String buildEmptyDiarySummary(String memberDisplayName) {
        String name = (memberDisplayName != null && !memberDisplayName.isBlank()) ? memberDisplayName : "마음님";
        return "이번 주는 " + name + "의 기록이 아직 없어요.\n\n"
                + "이번 주에는 작성된 일기가 없어 감정 분석을 진행하지 않았어요.\n\n"
                + "다음 주에는 작은 감정이라도 남겨볼까요? 당신의 소중한 마음 기록을 언제나 기다릴게요.";
    }
}
