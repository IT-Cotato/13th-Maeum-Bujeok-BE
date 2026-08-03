package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "구글 소셜 로그인 요청 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequest {
    @Schema(description = "구글 OAuth2 ID Token 또는 인가 코드", example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...")
    @NotNull(message = "구글 토큰은 필수 입력 값입니다.")
    private String googleToken;
}
