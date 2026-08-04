package com.maumbujeok.backend.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SmsVerifyRequest {
    private String phoneNumber;
    private String code;
}
