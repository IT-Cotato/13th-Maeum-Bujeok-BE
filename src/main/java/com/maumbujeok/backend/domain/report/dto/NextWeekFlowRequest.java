package com.maumbujeok.backend.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

@Schema(description = "다음 주 흐름 AI 생성 요청 DTO")
public record NextWeekFlowRequest(
        @Schema(description = "분석 대상 주의 시작일 (월요일)", example = "2026-08-10", type = "string")
        @NotBlank(message = "시작일은 필수입니다.")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "날짜 형식은 yyyy-MM-dd 이어야 합니다.")
        String weekStart
) {
    public LocalDate parsedWeekStart() {
        return LocalDate.parse(weekStart);
    }
}
