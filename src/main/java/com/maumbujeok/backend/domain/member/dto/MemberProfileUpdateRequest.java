package com.maumbujeok.backend.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

@Getter
@NoArgsConstructor
@Schema(description = "회원 프로필 수정 요청")
public class MemberProfileUpdateRequest {
    private static final DateTimeFormatter BIRTH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

    @NotBlank(message = "이름은 필수 입력 값입니다.")
    @Schema(description = "이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "생년월일은 필수 입력 값입니다.")
    @Schema(description = "생년월일 (yyyyMMdd, 실제 달력상 존재하는 날짜)", example = "19990101", requiredMode = Schema.RequiredMode.REQUIRED)
    private String birthDate;

    @Schema(description = "태어난 시간 (HH:mm 형식, 모르는 경우 null)", nullable = true, example = "14:30")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime birthTime;

    @NotBlank(message = "전화번호는 필수 입력 값입니다.")
    @Schema(description = "전화번호 (하이픈 포함/제외 가능)", example = "01012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;

    @NotNull(message = "성별은 필수 입력 값입니다.")
    @Schema(
            description = "성별 (여성/남성/선택안함 또는 FEMALE/MALE/NONE)",
            allowableValues = {"여성", "남성", "선택안함", "FEMALE", "MALE", "NONE"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private MemberSajuProfile.Gender gender;

    public String normalizedPhoneNumber() {
        return phoneNumber == null ? null : phoneNumber.replaceAll("[^0-9]", "");
    }

    public String trimmedName() {
        return name == null ? null : name.trim();
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

    @AssertTrue(message = "전화번호는 하이픈 포함 또는 제외한 10~11자리 숫자여야 합니다.")
    public boolean isPhoneNumberValid() {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return true;
        }

        String normalized = normalizedPhoneNumber();
        return normalized.matches("\\d{10,11}");
    }
}
