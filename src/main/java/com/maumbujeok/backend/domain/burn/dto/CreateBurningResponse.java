package com.maumbujeok.backend.domain.burn.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.burn.domain.BurningAnalysisStatus;
import com.maumbujeok.backend.domain.burn.domain.BurningSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(name = "CreateBurningResponse", description = "소각 시작 응답")
public record CreateBurningResponse(
        @Schema(description = "소각 기록 ID", example = "17") Long burningId,
        @Schema(description = "소각 출처", example = "DIARY") BurningSourceType sourceType,
        @Schema(description = "소각 완료 시각", example = "2026-08-03T12:00:00+09:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime burnedAt,
        @Schema(description = "AI 분석 초기 상태", example = "PENDING") BurningAnalysisStatus analysisStatus
) {}