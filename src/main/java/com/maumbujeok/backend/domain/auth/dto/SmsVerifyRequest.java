package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "SMS 인증번호 검증 요청")
public class SmsVerifyRequest {
    @Schema(description = "전화번호", example = "01012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @Schema(description = "발송받은 6자리 인증번호", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;
}
