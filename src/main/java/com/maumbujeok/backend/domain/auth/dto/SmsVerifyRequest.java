package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "SMS 인증코드 검증 요청 정보")
@Getter
@NoArgsConstructor
public class SmsVerifyRequest {
    @Schema(description = "인증번호를 전송받았던 휴대전화번호 (- 제외)", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "수신한 6자리 인증번호", example = "123456")
    private String code;
}
