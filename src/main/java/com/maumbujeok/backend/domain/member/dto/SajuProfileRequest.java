package com.maumbujeok.backend.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class SajuProfileRequest {
    private MemberSajuProfile.Gender gender;
    private MemberSajuProfile.CalendarType calendarType;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime birthTime;
}
