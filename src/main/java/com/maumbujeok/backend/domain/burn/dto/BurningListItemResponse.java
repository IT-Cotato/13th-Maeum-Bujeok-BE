package com.maumbujeok.backend.domain.burn.dto;
import com.maumbujeok.backend.domain.burn.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
@Schema(name="BurningListItemResponse", description="\uC18C\uAC01 \uAE30\uB85D \uBAA9\uB85D \uD56D\uBAA9")
public record BurningListItemResponse(
 @Schema(description="\uC18C\uAC01 \uAE30\uB85D ID", example="17") Long burningId,
 @Schema(description="\uC18C\uAC01 \uCD9C\uCC98", example="DIRECT") BurningSourceType sourceType,
 @Schema(description="\uC18C\uAC01 \uC644\uB8CC \uC2DC\uAC01", example="2026-08-03T12:00:00") LocalDateTime burnedAt,
 @Schema(description="AI \uBD84\uC11D \uC0C1\uD0DC", example="COMPLETED") BurningAnalysisStatus analysisStatus,
 @Schema(description="\uBD80\uC801 \uC0DD\uC131 \uC5EC\uBD80", example="false") boolean hasTalisman
) {}