package com.maumbujeok.backend.domain.report.ai;

public interface WeeklyReportAiProvider {
    WeeklyReportAiCallResult analyzeWeeklyReport(WeeklyReportAiRequest request);
}
