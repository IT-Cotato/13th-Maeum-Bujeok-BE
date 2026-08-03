package com.maumbujeok.backend.domain.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(description = "다음 주 흐름 분석 상세 조회 응답 정보")
public record NextWeekFlowQueryResponse(
        @Schema(description = "다음 주 흐름 분석 리포트 ID", example = "10")
        Long flowId,

        @Schema(description = "바인딩된 주간 감정 리포트 ID", example = "5")
        Long emotionReportId,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "분석이 조언하는 다음 주 시작일 (월요일 날짜)", example = "2026-07-20")
        LocalDate periodStart,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @Schema(description = "분석이 조언하는 다음 주 종료일 (일요일 날짜)", example = "2026-07-26")
        LocalDate periodEnd,

        @Schema(description = "분석 리포트 생성 상태 [경우의 수 및 프론트엔드 대응 가이드] -> "
                + "1) PROCESSING: AI 분석이 진행 중인 상태 (화면에 로딩 스피너 및 대기 상태 노출) "
                + "2) COMPLETED: AI 분석이 성공적으로 생성 완료된 상태 (화면에 결과 조언 카드뷰 노출) "
                + "3) FAILED: AI 분석 생성에 실패한 상태 (화면에 '재시도' 버튼 및 에러 안내 노출)", example = "COMPLETED")
        NextWeekFlowGenerationStatus generationStatus,

        @Schema(description = "구조화된 AI 조언 데이터 (생성 진행 중이거나 실패 시 빈 데이터 혹은 에러 문구가 포함되어 반환될 수 있음)")
        NextWeekFlowAdviceResponse advice,

        @Schema(description = "분석에 사용된 AI 모델 명칭", example = "openai/gpt-4o")
        String modelName,

        @Schema(description = "리포트 포맷 버전", example = "v1")
        String reportVersion,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        @Schema(description = "분석 완료 시각 (생성 중이거나 실패 시 null)", example = "2026-08-04T00:25:21+09:00")
        OffsetDateTime generatedAt
) {
}
