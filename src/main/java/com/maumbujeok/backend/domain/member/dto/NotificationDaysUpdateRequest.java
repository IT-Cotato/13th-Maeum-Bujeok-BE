package com.maumbujeok.backend.domain.member.dto;

public record NotificationDaysUpdateRequest(
        Boolean mondayEnabled,
        Boolean tuesdayEnabled,
        Boolean wednesdayEnabled,
        Boolean thursdayEnabled,
        Boolean fridayEnabled,
        Boolean saturdayEnabled,
        Boolean sundayEnabled
) {
}
