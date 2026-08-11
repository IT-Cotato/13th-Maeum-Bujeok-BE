package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "토큰 재발급 요청")
public class TokenReissueRequest {
    @Schema(description = "유효한 Refresh Token", example = "d9f8a7e6-...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String refreshToken;
}
