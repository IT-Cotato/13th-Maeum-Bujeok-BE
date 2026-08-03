package com.maumbujeok.backend.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import java.time.LocalDate;

@Schema(description = "다음 주 흐름 분석 시작 응답 정보")
public record NextWeekFlowStartResponse(
        @Schema(description = "생성된 다음 주 흐름 분석 리포트 ID", example = "10")
        Long flowId,

        @Schema(description = "바인딩된 주간 감정 리포트 ID", example = "5")
        Long emotionReportId,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "대상 주차 시작일 (월요일 날짜)", example = "2026-07-13")
        LocalDate weekStart,

        @Schema(description = "분석 리포트 초기 생성 상태 [경우의 수 및 프론트엔드 대응 가이드] -> "
                + "1) PROCESSING: 분석 작업 시작됨 (화면에 대기/로딩 상태 노출)", example = "PROCESSING")
        NextWeekFlowGenerationStatus generationStatus,

        @Schema(description = "처리 메시지", example = "다음 주 흐름 생성을 비동기로 시작했습니다.")
        String message
) {
}
