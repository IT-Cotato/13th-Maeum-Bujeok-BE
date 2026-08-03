package com.maumbujeok.backend.domain.talisman.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "TalismanListResponse", description = "\uCEE4\uC11C\uD615 \uBD80\uC801 \uBAA9\uB85D")
public record TalismanListResponse(
        @Schema(description = "\uBD80\uC801 \uBAA9\uB85D") List<TalismanItemResponse> items,
        @Schema(description = "\uD604\uC7AC \uD398\uC774\uC9C0 \uACB0\uACFC \uAC1C\uC218", example = "3") int count,
        @Schema(description = "\uB2E4\uC74C \uD398\uC774\uC9C0 \uC874\uC7AC \uC5EC\uBD80", example = "true") boolean hasNext,
        @Schema(description = "\uB2E4\uC74C \uD398\uC774\uC9C0\uC758 \uBD80\uC801 ID \uCEE4\uC11C. \uB2E4\uC74C \uD398\uC774\uC9C0\uAC00 \uC5C6\uC73C\uBA74 null", example = "17", nullable = true) Long nextCursor
) {
}