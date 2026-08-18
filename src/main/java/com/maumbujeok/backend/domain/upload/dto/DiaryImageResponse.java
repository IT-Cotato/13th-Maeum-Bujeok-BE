package com.maumbujeok.backend.domain.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "DiaryImageResponse", description = "\uC77C\uAE30 \uCCA8\uBD80 \uC774\uBBF8\uC9C0")
public record DiaryImageResponse(
        @Schema(description = "\uC5C5\uB85C\uB4DC ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID uploadId,
        @Schema(description = "10\uBD84\uAC04 \uC720\uD6A8\uD55C \uBE44\uACF5\uAC1C \uC774\uBBF8\uC9C0 \uC870\uD68C URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/diaries/example", requiredMode = Schema.RequiredMode.REQUIRED)
        String url,
        @Schema(description = "0\uBD80\uD130 \uC2DC\uC791\uD558\uB294 \uD654\uBA74 \uD45C\uC2DC \uC21C\uC11C", example = "0", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int sortOrder
) {
}