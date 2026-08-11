package com.maumbujeok.backend.domain.burn.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
@Schema(name="BurningListResponse", description="\uC18C\uAC01 \uAE30\uB85D \uCEE4\uC11C \uBAA9\uB85D")
public record BurningListResponse(
 @Schema(description="\uC18C\uAC01 \uAE30\uB85D \uBAA9\uB85D") List<BurningListItemResponse> items,
 @Schema(description="\uB2E4\uC74C \uD398\uC774\uC9C0 \uCEE4\uC11C. \uB2E4\uC74C \uD398\uC774\uC9C0\uAC00 \uC5C6\uC73C\uBA74 null", example="12", nullable=true) Long nextCursor,
 @Schema(description="\uB2E4\uC74C \uD398\uC774\uC9C0 \uC874\uC7AC \uC5EC\uBD80", example="true") boolean hasNext
) {}