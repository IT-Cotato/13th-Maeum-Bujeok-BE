package com.maumbujeok.backend.domain.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SmsSendRequest {
    @NotNull(message = "전화번호는 필수 입력 값입니다.")
    private String phoneNumber;

    @NotNull(message = "SMS 요청 목적은 필수 입력 값입니다.")
    private SmsPurpose purpose;
}
