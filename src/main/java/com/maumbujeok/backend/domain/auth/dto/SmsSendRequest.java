package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "SMS 인증코드 전송 요청 정보")
@Getter
@NoArgsConstructor
public class SmsSendRequest {
    @Schema(description = "수신할 휴대전화번호 (- 제외)", example = "01012345678")
    @NotNull(message = "전화번호는 필수 입력 값입니다.")
    private String phoneNumber;

    @Schema(description = "SMS 전송 목적 (SIGNUP: 회원가입, PASSWORD_RESET: 비밀번호 재설정)", example = "SIGNUP")
    @NotNull(message = "SMS 요청 목적은 필수 입력 값입니다.")
    private SmsPurpose purpose;
}
