package com.maumbujeok.backend.domain.burn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.burn.domain.BurningAnalysisStatus;
import com.maumbujeok.backend.domain.burn.domain.BurningSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "BurningDetailResponse", description = "소각 상세 응답. 소각 원문은 포함하지 않음")
public record BurningDetailResponse(
        @Schema(description = "소각 기록 ID", example = "3") Long burningId,
        @Schema(description = "소각 제목", nullable = true) String title,
        @Schema(description = "소각 원문", nullable = true) String sourceContent,
        @Schema(description = "AI 개운 지침", nullable = true) String guidance,
        @Schema(description = "소각 출처", example = "DIRECT") BurningSourceType sourceType,
        @Schema(description = "소각 완료 시각", example = "2026-08-03T18:58:35+09:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime burnedAt,
        @Schema(description = "AI 분석 상태", example = "FALLBACK_COMPLETED") BurningAnalysisStatus analysisStatus,
        @Schema(description = "분석 코멘트", nullable = true) String comment,
        @Schema(description = "부적 이미지 번호(1~13)", example = "2", minimum = "1", maximum = "13", nullable = true) Integer talismanType,
        @Schema(description = "4글자 부적 문구", example = "평온회복", nullable = true) String talismanText,
        @Schema(description = "부적 생성 여부", example = "false") boolean hasTalisman
) {
}