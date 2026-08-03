package com.maumbujeok.backend.domain.burn.dto;
import com.maumbujeok.backend.domain.burn.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
@Schema(name="CreateBurningResponse", description="\uC18C\uAC01 \uC2DC\uC791 \uC751\uB2F5")
public record CreateBurningResponse(
 @Schema(description="\uC18C\uAC01 \uAE30\uB85D ID", example="17") Long burningId,
 @Schema(description="\uC18C\uAC01 \uCD9C\uCC98", example="DIARY") BurningSourceType sourceType,
 @Schema(description="\uC18C\uAC01 \uC644\uB8CC \uC2DC\uAC01", example="2026-08-03T12:00:00") LocalDateTime burnedAt,
 @Schema(description="AI \uBD84\uC11D \uCD08\uAE30 \uC0C1\uD0DC", example="PENDING") BurningAnalysisStatus analysisStatus
) {}