package com.maumbujeok.backend.domain.saju.dto;

import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사주 분석 요청 생성 응답")
public record CreateSajuAnalysisResponse(
        @Schema(description = "사주 분석 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long analysisId,
        @Schema(description = "분석 상태", example = "PENDING", requiredMode = Schema.RequiredMode.REQUIRED)
        SajuAnalysisStatus status,
        @Schema(description = "요청 안내 메시지", example = "사주 분석 요청이 생성되었습니다.", requiredMode = Schema.RequiredMode.REQUIRED)
        String message
) {
    public static CreateSajuAnalysisResponse from(SajuAnalysis analysis, String message) {
        return new CreateSajuAnalysisResponse(analysis.getId(), analysis.getStatus(), message);
    }
}
