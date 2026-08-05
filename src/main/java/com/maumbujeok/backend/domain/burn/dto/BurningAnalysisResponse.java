package com.maumbujeok.backend.domain.burn.dto;

import com.maumbujeok.backend.domain.burn.domain.BurningAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(name = "BurningAnalysisResponse", description = "\uC18C\uAC01 AI \uBD84\uC11D \uC0C1\uD0DC\uC640 \uACB0\uACFC")
public record BurningAnalysisResponse(
        @Schema(description = "\uC18C\uAC01 \uAE30\uB85D ID", example = "3") Long burningId,
        @Schema(description = "\uBD84\uC11D \uCC98\uB9AC \uC0C1\uD0DC", example = "FALLBACK_COMPLETED") BurningAnalysisStatus status,
        @Schema(description = "\uBD84\uC11D \uC785\uB825 revision", example = "1") int inputRevision,
        @Schema(description = "\uAE30\uC5B5\uC744 \uB2E4\uB8E8\uB294 \uCF54\uBA58\uD2B8", example = "\uD798\uB4E4\uC5C8\uB358 \uAE30\uC5B5\uC744 \uC548\uC804\uD558\uAC8C \uB0B4\uB824\uB193\uC544\uC694.", nullable = true) String comment,
        @Schema(description = "\uBD80\uC801 \uC774\uBBF8\uC9C0 \uBC88\uD638(1~13)", example = "2", minimum = "1", maximum = "13", nullable = true) Integer talismanType,
        @Schema(description = "\uD655\uC815 4\uAE00\uC790 \uBD80\uC801 \uBB38\uAD6C", example = "\uD3C9\uC628\uD68C\uBCF5", nullable = true) String talismanText,
        @Schema(description = "\uBD84\uC11D \uC644\uB8CC \uC2DC\uAC01", example = "2026-08-03T18:58:35.031491", nullable = true) LocalDateTime completedAt
) {
}