package com.maumbujeok.backend.domain.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignUpRequest {
    private String phoneNumber;
    private String password;
    private String name;
    private String birthDate;
    private Boolean termsAgreed;
    private Boolean privacyAgreed;
    private Boolean sensitiveDataAgreed;
    private Boolean marketingAgreed;
}
