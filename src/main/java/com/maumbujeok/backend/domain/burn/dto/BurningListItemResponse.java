package com.maumbujeok.backend.domain.burn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.burn.domain.BurningAnalysisStatus;
import com.maumbujeok.backend.domain.burn.domain.BurningSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "BurningListItemResponse", description = "소각 기록 목록 항목")
public record BurningListItemResponse(
        @Schema(description = "소각 기록 ID", example = "17") Long burningId,
        @Schema(description = "소각 출처", example = "DIRECT") BurningSourceType sourceType,
        @Schema(description = "소각 완료 시각", example = "2026-08-03T12:00:00+09:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime burnedAt,
        @Schema(description = "AI 분석 상태", example = "COMPLETED") BurningAnalysisStatus analysisStatus,
        @Schema(description = "부적 생성 여부", example = "false") boolean hasTalisman
) {}