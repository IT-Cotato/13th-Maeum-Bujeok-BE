package com.maumbujeok.backend.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "다음 주 흐름 생성 시작 응답 DTO")
public record NextWeekFlowStartResponse(
        @Schema(description = "생성된 다음 주 흐름 ID", example = "1")
        Long flowId,

        @Schema(description = "연관된 주간 감정 리포트 ID", example = "10")
        Long emotionReportId,

        @Schema(description = "분석 대상 주의 시작일", example = "2026-08-10")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate weekStart,

        @Schema(description = "생성 진행 상태 (PENDING, IN_PROGRESS, COMPLETED, FAILED)", example = "IN_PROGRESS")
        NextWeekFlowGenerationStatus generationStatus,

        @Schema(description = "안내 메시지", example = "다음 주 흐름 분석 생성이 시작되었습니다.")
        String message
) {
}
