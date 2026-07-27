package com.maumbujeok.backend.domain.report.ai;

public record WeeklyReportAiCallResult(
        WeeklyReportAiResult result,
        int attempts
) {
}
