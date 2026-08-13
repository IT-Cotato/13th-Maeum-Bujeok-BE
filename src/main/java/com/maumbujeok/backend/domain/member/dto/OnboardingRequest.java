package com.maumbujeok.backend.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

@Getter
@NoArgsConstructor
@Schema(description = "회원 온보딩 요청")
public class OnboardingRequest {
    private static final DateTimeFormatter BIRTH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);
    @NotBlank
    @Schema(description = "생년월일 (yyyyMMdd, 실제 달력상 존재하는 날짜)", example = "19990101", requiredMode = Schema.RequiredMode.REQUIRED)
    private String birthDate;

    @NotNull
    @Schema(description = "성별", allowableValues = {"MALE", "FEMALE", "NONE"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private MemberSajuProfile.Gender gender;

    @NotNull
    @Schema(description = "달력 유형", allowableValues = {"SOLAR", "LUNAR", "LUNAR_LEAP"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private MemberSajuProfile.CalendarType calendarType;

    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "출생 시간은 HH:mm 형식(00:00~23:59)이어야 합니다.")
    @Schema(description = "태어난 시간 (HH:mm 형식, 모르는 경우 null)", nullable = true, example = "14:30")
    private String birthTime;

    @NotNull
    @AssertTrue(message = "필수 서비스 이용약관에 동의해야 합니다")
    @Schema(description = "필수 서비스 이용약관 동의", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean termsAgreed;

    @NotNull
    @AssertTrue(message = "필수 개인정보 처리방침 동의가 필요합니다")
    @Schema(description = "필수 개인정보 처리방침 동의", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean privacyAgreed;

    @Schema(description = "민감정보 처리 동의 여부", nullable = true)
    private Boolean sensitiveDataAgreed;

    @Schema(description = "마케팅 정보 수신 동의 여부", nullable = true)
    private Boolean marketingAgreed;
    public LocalTime getBirthTime() {
        if (birthTime == null || birthTime.isBlank()) {
            return null;
        }
        return LocalTime.parse(birthTime);
    }

    @AssertTrue(message = "생년월일은 yyyyMMdd 형식의 실제 날짜여야 합니다.")
    public boolean isBirthDateValid() {
        if (birthDate == null || birthDate.isBlank()) {
            return true;
        }
        if (!birthDate.matches("\\d{8}")) {
            return false;
        }

        try {
            LocalDate.parse(birthDate, BIRTH_DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}