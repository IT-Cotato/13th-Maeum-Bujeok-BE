package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiRequest;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportSummaryComposer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyReportFallbackFactory {
    private final WeeklyReportSummaryComposer summaryComposer;

    public WeeklyReportAiResult create(WeeklyReportGenerationInput input) {
        return summaryComposer.compose(toRequest(input), "fallback-weekly-report-v1");
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
