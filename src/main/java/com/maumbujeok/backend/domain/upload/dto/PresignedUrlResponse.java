package com.maumbujeok.backend.domain.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "PresignedUrlResponse", description = "\uBC1C\uAE09\uB41C \uC5C5\uB85C\uB4DC ID\uC640 PUT URL")
public record PresignedUrlResponse(
        @Schema(description = "\uC77C\uAE30 \uC0DD\uC131\u00B7\uC218\uC815 \uC694\uCCAD\uC758 imageUploadIds\uC5D0 \uC0AC\uC6A9\uD560 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID uploadId,
        @Schema(description = "\uD30C\uC77C \uBC14\uC774\uD2B8\uB97C PUT\uD560 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/diaries/example", requiredMode = Schema.RequiredMode.REQUIRED)
        String uploadUrl,
        @Schema(description = "\uC5C5\uB85C\uB4DC URL \uB9CC\uB8CC \uC2DC\uAC01(\uBC1C\uAE09 \uD6C4 10\uBD84)", example = "2026-08-03T12:10:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
        Instant expiresAt
) {
}