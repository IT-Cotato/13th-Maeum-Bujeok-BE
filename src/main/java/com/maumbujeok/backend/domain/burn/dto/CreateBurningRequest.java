package com.maumbujeok.backend.domain.burn.dto;
import com.maumbujeok.backend.domain.burn.domain.BurningSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(name="CreateBurningRequest", description="\uC18C\uAC01 \uC2DC\uC791 \uC694\uCCAD")
public record CreateBurningRequest(
 @Schema(description="\uC18C\uAC01 \uCD9C\uCC98. DIRECT \uB610\uB294 DIARY", example="DIRECT", requiredMode=Schema.RequiredMode.REQUIRED) BurningSourceType sourceType,
 @Schema(description="DIRECT\uC5D0\uC11C\uB9CC \uC0AC\uC6A9\uD558\uB294 \uC9C1\uC811 \uC785\uB825 \uC6D0\uBB38", example="\uC78A\uACE0 \uC2F6\uC740 \uAE30\uC5B5", nullable=true) String content,
 @Schema(description="DIARY\uC5D0\uC11C\uB9CC \uC0AC\uC6A9\uD558\uB294 \uC77C\uAE30 ID", example="42", nullable=true) Long diaryId
) {}