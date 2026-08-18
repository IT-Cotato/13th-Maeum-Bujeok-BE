package com.maumbujeok.backend.domain.burn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.burn.domain.BurningAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "BurningAnalysisResponse", description = "소각 AI 분석 상태와 결과")
public record BurningAnalysisResponse(
        @Schema(description = "소각 기록 ID", example = "3") Long burningId,
        @Schema(description = "분석 처리 상태", example = "FALLBACK_COMPLETED") BurningAnalysisStatus status,
        @Schema(description = "분석 입력 revision", example = "1") int inputRevision,
        @Schema(description = "기억을 다루는 코멘트", example = "힘들었던 기억을 안전하게 내려놓아요.", nullable = true) String comment,
        @Schema(description = "부적 이미지 번호(1~13)", example = "2", minimum = "1", maximum = "13", nullable = true) Integer talismanType,
        @Schema(description = "확정 4글자 부적 문구", example = "평온회복", nullable = true) String talismanText,
        @Schema(description = "분석 완료 시각", example = "2026-08-03T18:58:35+09:00", nullable = true)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime completedAt
) {
}