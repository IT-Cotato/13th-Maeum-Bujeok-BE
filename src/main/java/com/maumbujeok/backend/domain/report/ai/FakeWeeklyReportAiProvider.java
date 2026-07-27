package com.maumbujeok.backend.domain.report.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "fake", matchIfMissing = true)
public class FakeWeeklyReportAiProvider implements WeeklyReportAiProvider {
    private final WeeklyReportSummaryComposer summaryComposer;

    @Override
    public WeeklyReportAiCallResult analyzeWeeklyReport(WeeklyReportAiRequest request) {
        return new WeeklyReportAiCallResult(
                summaryComposer.compose(request, "fake-weekly-report-v1"),
                1
        );
    }
}
