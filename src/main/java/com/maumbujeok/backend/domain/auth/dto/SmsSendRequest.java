package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "SMS 인증번호 발송 요청")
public class SmsSendRequest {
    @NotNull(message = "전화번호는 필수 입력 값입니다.")
    @Schema(description = "인증번호를 발송할 전화번호", example = "01012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @NotNull(message = "SMS 요청 목적은 필수 입력 값입니다.")
    @Schema(description = "SMS 인증 요청 목적 (SIGNUP: 회원가입, PASSWORD_RESET: 비밀번호 재설정)", requiredMode = Schema.RequiredMode.REQUIRED)
    private SmsPurpose purpose;
}
