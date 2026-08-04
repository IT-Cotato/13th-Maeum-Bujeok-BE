package com.maumbujeok.backend.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

@Schema(description = "다음 주 흐름 분석 요청 정보")
public record NextWeekFlowRequest(
        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "분석 대상 주차 시작일 (월요일 날짜)", example = "2026-07-13")
        LocalDate weekStart
) {
}
