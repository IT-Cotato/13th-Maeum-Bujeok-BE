package com.maumbujeok.backend.domain.member.dto;

import com.maumbujeok.backend.domain.member.domain.MemberNotificationSetting;

public record NotificationDaysResponse(
        boolean mondayEnabled,
        boolean tuesdayEnabled,
        boolean wednesdayEnabled,
        boolean thursdayEnabled,
        boolean fridayEnabled,
        boolean saturdayEnabled,
        boolean sundayEnabled
) {
    public static NotificationDaysResponse from(MemberNotificationSetting setting) {
        return new NotificationDaysResponse(
                setting.isMondayEnabled(),
                setting.isTuesdayEnabled(),
                setting.isWednesdayEnabled(),
                setting.isThursdayEnabled(),
                setting.isFridayEnabled(),
                setting.isSaturdayEnabled(),
                setting.isSundayEnabled()
        );
    }
}
