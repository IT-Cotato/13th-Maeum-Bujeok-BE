package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "인증 토큰 응답 정보")
@Getter
@AllArgsConstructor
public class TokenResponse {
    @Schema(description = "Access Token (유효시간: 1시간)", example = "eyJhbGciOiJSUzI1NiIs...")
    private String accessToken;

    @Schema(description = "Refresh Token (유효시간: 2주)", example = "eyJhbGciOiJSUzI1NiIs...")
    private String refreshToken;
}
