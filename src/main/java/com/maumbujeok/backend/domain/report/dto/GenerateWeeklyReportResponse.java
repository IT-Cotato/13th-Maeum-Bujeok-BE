package com.maumbujeok.backend.domain.report.dto;

import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportGenerationStatus;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "GenerateWeeklyReportResponse", description = "주간 감정 리포트 생성 시작 응답")
public record GenerateWeeklyReportResponse(
        @Schema(description = "감정 리포트 ID", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
        Long emotionReportId,
        @Schema(description = "리포트 타입", example = "WEEKLY", requiredMode = Schema.RequiredMode.REQUIRED)
        EmotionReportType reportType,
        @Schema(description = "리포트 시작일", example = "2026-07-13", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate periodStart,
        @Schema(description = "리포트 종료일", example = "2026-07-19", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate periodEnd,
        @Schema(description = "비동기 생성 상태", example = "PENDING", requiredMode = Schema.RequiredMode.REQUIRED)
        EmotionReportGenerationStatus generationStatus,
        @Schema(description = "생성 시작 안내 메시지", example = "주간 감정 리포트 생성을 시작했습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        String message
) {
    public static GenerateWeeklyReportResponse from(EmotionReport report, String message) {
        return new GenerateWeeklyReportResponse(
                report.getId(),
                report.getReportType(),
                report.getPeriodStart(),
                report.getPeriodEnd(),
                report.getGenerationStatus(),
                message
        );
    }
}
