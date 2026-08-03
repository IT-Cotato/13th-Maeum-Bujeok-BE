package com.maumbujeok.backend.domain.burn.dto;
import com.maumbujeok.backend.domain.burn.domain.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
@Schema(name="BurningDetailResponse", description="\uC18C\uAC01 \uC0C1\uC138 \uC751\uB2F5. \uC18C\uAC01 \uC6D0\uBB38\uC740 \uD3EC\uD568\uD558\uC9C0 \uC54A\uC74C")
public record BurningDetailResponse(
 Long burningId, BurningSourceType sourceType, LocalDateTime burnedAt, BurningAnalysisStatus analysisStatus,
 String comment, String talismanType, String talismanText, boolean hasTalisman
) {}