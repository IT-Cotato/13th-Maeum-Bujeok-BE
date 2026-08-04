package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "로그인 요청 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @Schema(description = "로그인할 회원의 휴대전화번호 (- 제외)", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "로그인 비밀번호", example = "Password123!")
    private String password;
}
