package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그아웃 요청")
public class LogoutRequest {
    @Schema(description = "무효화할 Refresh Token", example = "d9f8a7e6-...")
    private String refreshToken;
}
