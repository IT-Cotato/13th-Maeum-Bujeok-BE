package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그아웃 요청 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    @Schema(description = "로그아웃할 사용자의 Refresh Token", example = "eyJhbGciOiJSUzI1NiIs...")
    private String refreshToken;
}
