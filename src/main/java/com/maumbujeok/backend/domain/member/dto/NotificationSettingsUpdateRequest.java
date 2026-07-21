package com.maumbujeok.backend.domain.member.dto;

public record NotificationSettingsUpdateRequest(
        Boolean diaryReminderEnabled,
        Boolean fortuneActionEnabled
) {
}
