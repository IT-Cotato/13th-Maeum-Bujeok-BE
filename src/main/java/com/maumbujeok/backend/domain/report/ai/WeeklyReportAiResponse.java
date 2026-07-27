package com.maumbujeok.backend.domain.report.ai;

public record WeeklyReportAiResponse(String insightSummary) {
    public WeeklyReportAiResult toResult(String model) {
        return new WeeklyReportAiResult(insightSummary, model);
    }
}
