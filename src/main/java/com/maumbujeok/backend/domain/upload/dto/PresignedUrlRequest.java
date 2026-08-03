package com.maumbujeok.backend.domain.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PresignedUrlRequest", description = "\uC77C\uAE30 \uC774\uBBF8\uC9C0 \uC5C5\uB85C\uB4DC URL \uBC1C\uAE09 \uC694\uCCAD")
public record PresignedUrlRequest(
        @Schema(description = "\uC774\uBBF8\uC9C0 MIME \uD0C0\uC785", example = "image/png", allowableValues = {"image/jpeg", "image/png", "image/webp"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String contentType,
        @Schema(description = "\uC5C5\uB85C\uB4DC\uD560 \uD30C\uC77C \uD06C\uAE30(byte). \uCD5C\uB300 10MB", example = "1024", minimum = "1", maximum = "10485760", requiredMode = Schema.RequiredMode.REQUIRED)
        long fileSize
) {
}