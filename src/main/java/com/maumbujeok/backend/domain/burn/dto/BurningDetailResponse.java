package com.maumbujeok.backend.domain.burn.dto;

import com.maumbujeok.backend.domain.burn.domain.BurningAnalysisStatus;
import com.maumbujeok.backend.domain.burn.domain.BurningSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "BurningDetailResponse", description = "\uC18C\uAC01 \uC0C1\uC138 \uC751\uB2F5. \uC18C\uAC01 \uC6D0\uBB38\uC740 \uD3EC\uD568\uD558\uC9C0 \uC54A\uC74C")
public record BurningDetailResponse(
        @Schema(description = "\uC18C\uAC01 \uAE30\uB85D ID", example = "3") Long burningId,
                @Schema(description = "소각 제목", nullable = true) String title,
        @Schema(description = "소각 원문", nullable = true) String sourceContent,
        @Schema(description = "AI 개운 지침", nullable = true) String guidance,
@Schema(description = "\uC18C\uAC01 \uCD9C\uCC98", example = "DIRECT") BurningSourceType sourceType,
        @Schema(description = "\uC18C\uAC01 \uC644\uB8CC \uC2DC\uAC01", example = "2026-08-03T18:58:35.031491") LocalDateTime burnedAt,
        @Schema(description = "AI \uBD84\uC11D \uC0C1\uD0DC", example = "FALLBACK_COMPLETED") BurningAnalysisStatus analysisStatus,
        @Schema(description = "\uBD84\uC11D \uCF54\uBA58\uD2B8", nullable = true) String comment,
        @Schema(description = "\uBD80\uC801 \uC774\uBBF8\uC9C0 \uBC88\uD638(1~13)", example = "2", minimum = "1", maximum = "13", nullable = true) Integer talismanType,
        @Schema(description = "4\uAE00\uC790 \uBD80\uC801 \uBB38\uAD6C", example = "\uD3C9\uC628\uD68C\uBCF5", nullable = true) String talismanText,
        @Schema(description = "\uBD80\uC801 \uC0DD\uC131 \uC5EC\uBD80", example = "false") boolean hasTalisman
) {
}