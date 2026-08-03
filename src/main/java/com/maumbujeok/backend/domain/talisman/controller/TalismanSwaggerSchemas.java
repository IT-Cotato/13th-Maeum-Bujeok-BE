package com.maumbujeok.backend.domain.talisman.controller;

import com.maumbujeok.backend.domain.talisman.dto.TalismanListResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

final class TalismanSwaggerSchemas {
    private TalismanSwaggerSchemas() {
    }

    @Schema(name = "TalismanListApiResponse", description = "\uBD80\uC801 \uBAA9\uB85D \uACF5\uD1B5 \uC751\uB2F5")
    static final class TalismanListApiResponse extends ApiResponse<TalismanListResponse> {
        private TalismanListApiResponse() {
            super(true, "200", "\uC694\uCCAD\uC774 \uC131\uACF5\uD588\uC2B5\uB2C8\uB2E4.", null);
        }
    }

    @Schema(name = "TalismanErrorApiResponse", description = "\uBD80\uC801 API \uC624\uB958 \uACF5\uD1B5 \uC751\uB2F5")
    static final class TalismanErrorApiResponse extends ApiResponse<Void> {
        private TalismanErrorApiResponse() {
            super(false, "TALISMAN_400", "\uBD80\uC801 \uC870\uD68C \uC694\uCCAD\uC774 \uC798\uBABB\uB418\uC5C8\uC2B5\uB2C8\uB2E4.", null);
        }
    }
}