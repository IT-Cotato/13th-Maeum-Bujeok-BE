package com.maumbujeok.backend.domain.burn.dto;
import com.maumbujeok.backend.domain.burn.domain.BurningAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
@Schema(name="BurningAnalysisResponse", description="\uC18C\uAC01 AI \uBD84\uC11D \uC0C1\uD0DC\uC640 \uACB0\uACFC")
public record BurningAnalysisResponse(
 Long burningId,
 @Schema(description="\uBD84\uC11D \uC0C1\uD0DC", example="PROCESSING") BurningAnalysisStatus status,
 @Schema(description="\uBD84\uC11D \uC785\uB825 revision", example="1") int inputRevision,
 String comment, String talismanType, String talismanText, LocalDateTime completedAt
) {}