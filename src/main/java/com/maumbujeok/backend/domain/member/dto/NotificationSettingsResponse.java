package com.maumbujeok.backend.domain.member.dto;

import com.maumbujeok.backend.domain.member.domain.MemberNotificationSetting;

public record NotificationSettingsResponse(
        boolean diaryReminderEnabled,
        boolean fortuneActionEnabled
) {
    public static NotificationSettingsResponse from(MemberNotificationSetting setting) {
        return new NotificationSettingsResponse(
                setting.isDiaryReminderEnabled(),
                setting.isFortuneActionEnabled()
        );
    }
}
