package com.maumbujeok.backend.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Schema(description = "현재 로그인 회원 정보")
public record MemberProfileResponse(
        @Schema(description = "이름", example = "홍길동") String name,
        @Schema(description = "이메일", nullable = true, example = "user@example.com") String email,
        @Schema(description = "전화번호", example = "01012345678") String phoneNumber,
        @Schema(description = "가입 경로", allowableValues = {"LOCAL", "GOOGLE"}) Member.Provider provider,
        @Schema(description = "생년월일 (yyyyMMdd)", nullable = true, example = "19990101") String birthDate,
        @Schema(description = "성별", nullable = true) MemberSajuProfile.Gender gender,
        @Schema(description = "달력 유형", nullable = true) MemberSajuProfile.CalendarType calendarType,
        @Schema(description = "태어난 시간 (HH:mm)", nullable = true, example = "14:30")
        @JsonFormat(pattern = "HH:mm")
        LocalTime birthTime,
        @Schema(description = "필수 서비스 이용약관 동의 여부") boolean termsAgreed,
        @Schema(description = "필수 개인정보 처리방침 동의 여부") boolean privacyAgreed,
        @Schema(description = "민감정보 처리 동의 여부") boolean sensitiveDataAgreed,
        @Schema(description = "마케팅 정보 수신 동의 여부") boolean marketingAgreed,
        @Schema(description = "온보딩 완료 여부") boolean onboardingCompleted,
        @Schema(description = "최초 온보딩 완료 시각", nullable = true) LocalDateTime onboardingCompletedAt
) {
    public static MemberProfileResponse from(Member member, MemberSajuProfile sajuProfile) {
        return new MemberProfileResponse(
                member.getName(), member.getEmail(), member.getPhoneNumber(), member.getProvider(), member.getBirthDate(),
                sajuProfile != null ? sajuProfile.getGender() : null,
                sajuProfile != null ? sajuProfile.getCalendarType() : null,
                sajuProfile != null ? sajuProfile.getBirthTime() : null,
                member.getTermsAgreedAt() != null,
                member.getPrivacyAgreedAt() != null,
                member.getSensitiveDataAgreedAt() != null,
                member.getMarketingAgreedAt() != null,
                member.getOnboardingCompletedAt() != null,
                member.getOnboardingCompletedAt()
        );
    }
}
