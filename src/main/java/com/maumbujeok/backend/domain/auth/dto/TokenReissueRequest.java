package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "토큰 재발급 요청 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TokenReissueRequest {
    @Schema(description = "만료되었거나 만료 예정인 Refresh Token", example = "eyJhbGciOiJSUzI1NiIs...")
    private String refreshToken;
}
