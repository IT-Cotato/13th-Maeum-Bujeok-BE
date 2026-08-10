package com.maumbujeok.backend.domain.saju.dto;

import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "사주 분석 상태 및 결과 조회 응답")
public record SajuAnalysisResponse(
        @Schema(description = "사주 분석 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long analysisId,
        @Schema(description = "분석 상태", example = "COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
        SajuAnalysisStatus status,
        @Schema(description = "현재 표시 가능한 오행 비율 결과. 아직 성공 결과가 없으면 null", nullable = true)
        FiveElementsBalanceResponse elements,
        @Schema(description = "분석에 사용한 AI 모델명", example = "gpt-5.5", nullable = true)
        String modelName,
        @Schema(description = "최근 분석 실패 코드. 실패하지 않았다면 null", example = "AI_INVALID_RESPONSE", nullable = true)
        String failureCode,
        @Schema(description = "현재 표시 중인 결과가 생성되었거나 실패가 기록된 시각", nullable = true)
        LocalDateTime analyzedAt
) {
    public static SajuAnalysisResponse from(SajuAnalysis analysis) {
        return new SajuAnalysisResponse(
                analysis.getId(),
                analysis.getStatus(),
                FiveElementsBalanceResponse.from(analysis),
                analysis.getModelName(),
                analysis.getFailureCode(),
                analysis.getAnalyzedAt()
        );
    }
}
