package com.maumbujeok.backend.domain.upload.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(name = "DiaryImageResponse", description = "일기 첨부 이미지")
public record DiaryImageResponse(
        @Schema(description = "업로드 ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID uploadId,
        @Schema(description = "10분간 유효한 비공개 이미지 조회 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/diaries/example", requiredMode = Schema.RequiredMode.REQUIRED)
        String url,
        @Schema(description = "0부터 시작하는 표시 순서", example = "0", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int sortOrder
) {}
