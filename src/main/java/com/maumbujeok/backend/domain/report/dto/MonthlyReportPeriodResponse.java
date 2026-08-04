package com.maumbujeok.backend.domain.report.dto;

import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportGenerationStatus;
import java.time.LocalDate;

public record MonthlyReportPeriodResponse(Long reportId, LocalDate periodStart, LocalDate periodEnd, EmotionReportGenerationStatus generationStatus) {
    public static MonthlyReportPeriodResponse from(EmotionReport report) {
        return new MonthlyReportPeriodResponse(report.getId(), report.getPeriodStart(), report.getPeriodEnd(), report.getGenerationStatus());
    }
}