package com.maumbujeok.backend.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "다음 주 흐름 AI 생성 요청 DTO")
public record NextWeekFlowRequest(
        @Schema(description = "분석 대상 주의 시작일 (월요일)", example = "2026-08-10")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate weekStart
) {
}
