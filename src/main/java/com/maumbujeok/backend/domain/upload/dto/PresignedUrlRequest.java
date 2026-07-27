package com.maumbujeok.backend.domain.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PresignedUrlRequest", description = "일기 이미지 업로드 URL 발급 요청")
public record PresignedUrlRequest(
        @Schema(description = "이미지 MIME 타입", example = "image/png", allowableValues = {"image/jpeg", "image/png", "image/webp"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String contentType,
        @Schema(description = "업로드할 파일 크기(byte). 최대 10MB", example = "1024", minimum = "1", maximum = "10485760", requiredMode = Schema.RequiredMode.REQUIRED)
        long fileSize
) {}
