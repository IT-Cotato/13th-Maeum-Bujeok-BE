package com.maumbujeok.backend.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class SajuProfileRequest {
    @Schema(description = "성별")
    private MemberSajuProfile.Gender gender;

    @Schema(description = "달력 유형 (SOLAR/LUNAR/LUNAR_LEAP)")
    private MemberSajuProfile.CalendarType calendarType;

    @Schema(description = "태어난 시간 (HH:mm 형식, 모를 경우 null 허용)", nullable = true, example = "14:30")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime birthTime;
}
