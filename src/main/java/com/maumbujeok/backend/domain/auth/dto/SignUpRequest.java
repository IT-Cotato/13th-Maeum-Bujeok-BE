package com.maumbujeok.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Schema(description = "회원가입 요청 정보")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpRequest {
    @Schema(description = "가입할 회원의 휴대전화번호 (본인인증 완료된 번호, - 제외)", example = "01012345678")
    private String phoneNumber;

    @Schema(description = "회원 비밀번호", example = "Password123!")
    private String password;

    @Schema(description = "회원 이름 (실명)", example = "홍길동")
    private String name;

    @Schema(description = "회원 생년월일 (YYYYMMDD 형식)", example = "19990101")
    private String birthDate;

    @Schema(description = "필수 서비스 이용약관 동의 여부", example = "true")
    @NotNull
    @AssertTrue(message = "필수 서비스 이용약관에 동의해야 합니다.")
    private Boolean termsAgreed;

    @Schema(description = "필수 개인정보 처리방침 동의 여부", example = "true")
    @NotNull
    @AssertTrue(message = "필수 개인정보 처리방침에 동의해야 합니다.")
    private Boolean privacyAgreed;

    @Schema(description = "선택 민감정보 처리 동의 여부", example = "true")
    private Boolean sensitiveDataAgreed;

    @Schema(description = "선택 마케팅 수신 동의 여부", example = "false")
    private Boolean marketingAgreed;
}
