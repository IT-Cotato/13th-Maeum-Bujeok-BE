package com.maumbujeok.backend.domain.report.dto;

import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportGenerationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "WeeklyReportPeriodResponse", description = "주간 리포트 기간 목록 항목")
public record WeeklyReportPeriodResponse(
        @Schema(description = "주간 리포트 ID", example = "12") Long reportId,
        @Schema(description = "리포트 기간 시작일(월요일)", example = "2026-07-13") LocalDate periodStart,
        @Schema(description = "리포트 기간 종료일(일요일)", example = "2026-07-19") LocalDate periodEnd,
        @Schema(description = "생성 상태", example = "COMPLETED") EmotionReportGenerationStatus generationStatus
) {
    public static WeeklyReportPeriodResponse from(EmotionReport report) {
        return new WeeklyReportPeriodResponse(
                report.getId(), report.getPeriodStart(), report.getPeriodEnd(), report.getGenerationStatus()
        );
    }
}