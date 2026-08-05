package com.maumbujeok.backend.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "다음 주 흐름 결과 조회 응답 DTO")
public record NextWeekFlowQueryResponse(
        @Schema(description = "다음 주 흐름 ID", example = "1")
        Long flowId,

        @Schema(description = "연관된 주간 감정 리포트 ID", example = "10")
        Long emotionReportId,

        @Schema(description = "대상 주간 시작일", example = "2026-08-17")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate periodStart,

        @Schema(description = "대상 주간 종료일", example = "2026-08-23")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate periodEnd,

        @Schema(description = "생성 진행 상태", example = "COMPLETED")
        NextWeekFlowGenerationStatus generationStatus,

        @Schema(description = "AI 조언 및 흐름 분석 텍스트", example = "다음 주는 목(木) 기운이 다가와 새로운 시도를 시작하기 좋은 시기입니다.")
        String adviceText,

        @Schema(description = "사용된 AI 모델명", example = "gpt-4o-mini")
        String modelName,

        @Schema(description = "프롬프트/리포트 버전", example = "v1")
        String reportVersion,

        @Schema(description = "생성 완료 일시", example = "2026-08-10T00:05:00+09:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime generatedAt
) {
}
