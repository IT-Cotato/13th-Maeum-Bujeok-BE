package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "비밀번호 재설정 요청 정보")
@Getter
@NoArgsConstructor
public class PasswordResetRequest {
    @Schema(description = "비밀번호를 재설정할 회원의 휴대전화번호 (- 제외)", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "새로운 비밀번호", example = "NewPassword123!")
    private String newPassword;
}
