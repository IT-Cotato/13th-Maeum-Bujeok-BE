package com.maumbujeok.backend.domain.saju.ai;

import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import java.time.LocalTime;

public record SajuAiRequest(
        String birthDate,
        MemberSajuProfile.Gender gender,
        MemberSajuProfile.CalendarType calendarType,
        LocalTime birthTime
) {
}
