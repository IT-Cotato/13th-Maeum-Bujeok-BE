package com.maumbujeok.backend.domain.upload.controller;

import com.maumbujeok.backend.domain.upload.dto.PresignedUrlResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

final class UploadSwaggerSchemas {
    private UploadSwaggerSchemas() {
    }

    @Schema(name = "PresignedUrlApiResponse", description = "이미지 업로드 URL 발급 공통 응답")
    static final class PresignedUrlApiResponse extends ApiResponse<PresignedUrlResponse> {
        private PresignedUrlApiResponse() { super(true, "200", "요청에 성공하였습니다.", null); }
    }

    @Schema(name = "UploadErrorApiResponse", description = "업로드 API 오류 공통 응답")
    static final class UploadErrorApiResponse extends ApiResponse<Void> {
        private UploadErrorApiResponse() { super(false, "UPLOAD_400", "Invalid upload request.", null); }
    }
}
