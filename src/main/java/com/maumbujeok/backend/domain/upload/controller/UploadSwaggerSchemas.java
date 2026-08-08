package com.maumbujeok.backend.domain.upload.controller;

import com.maumbujeok.backend.domain.upload.dto.PresignedUrlResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

final class UploadSwaggerSchemas {
    private UploadSwaggerSchemas() {
    }

    @Schema(name = "PresignedUrlApiResponse", description = "\uC77C\uAE30 \uC774\uBBF8\uC9C0 \uC5C5\uB85C\uB4DC URL \uBC1C\uAE09 \uACF5\uD1B5 \uC751\uB2F5")
    static final class PresignedUrlApiResponse extends ApiResponse<PresignedUrlResponse> {
        private PresignedUrlApiResponse() {
            super(true, "200", "\uC694\uCCAD\uC774 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.", null);
        }
    }

    @Schema(name = "UploadErrorApiResponse", description = "\uC774\uBBF8\uC9C0 \uC5C5\uB85C\uB4DC API \uC624\uB958 \uACF5\uD1B5 \uC751\uB2F5")
    static final class UploadErrorApiResponse extends ApiResponse<Void> {
        private UploadErrorApiResponse() {
            super(false, "UPLOAD_400", "\uC774\uBBF8\uC9C0 \uC5C5\uB85C\uB4DC \uC694\uCCAD\uC774 \uC798\uBABB\uB418\uC5C8\uC2B5\uB2C8\uB2E4.", null);
        }
    }
}