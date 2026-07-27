package com.maumbujeok.backend.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "GenerateWeeklyReportRequest", description = "주간 감정 리포트 생성 요청")
public record GenerateWeeklyReportRequest(
        @Schema(description = "주 시작일(월요일 기준)", example = "2026-07-13", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate weekStart
) {
}
