package com.maumbujeok.backend.domain.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(name = "PresignedUrlResponse", description = "발급된 업로드 ID와 PUT URL")
public record PresignedUrlResponse(
        @Schema(description = "일기 생성·수정 시 imageUploadIds에 사용할 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID uploadId,
        @Schema(description = "파일 바이트를 PUT할 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/diaries/example", requiredMode = Schema.RequiredMode.REQUIRED)
        String uploadUrl,
        @Schema(description = "업로드 URL 만료 시각(발급 후 10분)", example = "2026-07-27T12:10:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
        Instant expiresAt
) {}
