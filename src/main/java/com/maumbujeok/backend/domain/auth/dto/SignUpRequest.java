package com.maumbujeok.backend.domain.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {
    private String phoneNumber;
    private String password;
    private String name;
    private String birthDate;

    @NotNull
    @AssertTrue(message = "필수 서비스 이용약관에 동의해야 합니다.")
    private Boolean termsAgreed;

    @NotNull
    @AssertTrue(message = "필수 개인정보 처리방침에 동의해야 합니다.")
    private Boolean privacyAgreed;

    private Boolean sensitiveDataAgreed;
    private Boolean marketingAgreed;
}
