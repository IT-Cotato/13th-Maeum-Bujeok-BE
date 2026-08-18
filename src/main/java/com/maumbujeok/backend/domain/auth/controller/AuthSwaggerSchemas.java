package com.maumbujeok.backend.domain.auth.controller;

import com.maumbujeok.backend.domain.auth.dto.TokenResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

final class AuthSwaggerSchemas {
    private AuthSwaggerSchemas() {
    }

    @Schema(name = "AuthStringApiResponse", description = "인증 성공 메시지 응답")
    static final class AuthStringApiResponse extends ApiResponse<String> {
        private AuthStringApiResponse() {
            super(true, "200", "요청이 성공했습니다.", "성공 메시지");
        }
    }

    @Schema(name = "AuthTokenApiResponse", description = "토큰 발급 성공 응답")
    static final class AuthTokenApiResponse extends ApiResponse<TokenResponse> {
        private AuthTokenApiResponse() {
            super(true, "200", "요청이 성공했습니다.", null);
        }
    }

    @Schema(name = "AuthErrorApiResponse", description = "인증 관련 실패 공통 응답")
    static final class AuthErrorApiResponse extends ApiResponse<Void> {
        private AuthErrorApiResponse() {
            super(false, "AUTH_400", "요청 처리 중 오류가 발생했습니다.", null);
        }
    }
}
