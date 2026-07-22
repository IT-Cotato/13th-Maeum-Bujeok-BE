package com.maumbujeok.backend.domain.member.service;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberNotificationSetting;
import com.maumbujeok.backend.domain.member.dto.NotificationDaysResponse;
import com.maumbujeok.backend.domain.member.dto.NotificationDaysUpdateRequest;
import com.maumbujeok.backend.domain.member.dto.NotificationSettingsResponse;
import com.maumbujeok.backend.domain.member.dto.NotificationSettingsUpdateRequest;
import com.maumbujeok.backend.domain.member.repository.MemberNotificationSettingRepository;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final MemberRepository memberRepository;
    private final MemberNotificationSettingRepository notificationSettingRepository;

    @Transactional
    public NotificationSettingsResponse updateNotificationSettings(
            String memberPhoneNumber,
            NotificationSettingsUpdateRequest request
    ) {
        validateNotificationSettingsRequest(request);

        MemberNotificationSetting setting = getOrCreateSetting(memberPhoneNumber);
        setting.updateNotificationSettings(
                request.diaryReminderEnabled(),
                request.fortuneActionEnabled()
        );

        saveIfNew(setting);
        return NotificationSettingsResponse.from(setting);
    }

    @Transactional
    public NotificationDaysResponse updateNotificationDays(
            String memberPhoneNumber,
            NotificationDaysUpdateRequest request
    ) {
        validateNotificationDaysRequest(request);

        MemberNotificationSetting setting = getOrCreateSetting(memberPhoneNumber);
        setting.updateNotificationDays(
                request.mondayEnabled(),
                request.tuesdayEnabled(),
                request.wednesdayEnabled(),
                request.thursdayEnabled(),
                request.fridayEnabled(),
                request.saturdayEnabled(),
                request.sundayEnabled()
        );

        saveIfNew(setting);
        return NotificationDaysResponse.from(setting);
    }

    private MemberNotificationSetting getOrCreateSetting(String memberPhoneNumber) {
        return notificationSettingRepository.findByMemberPhoneNumber(memberPhoneNumber)
                .orElseGet(() -> MemberNotificationSetting.create(getMemberByPhoneNumber(memberPhoneNumber)));
    }

    private Member getMemberByPhoneNumber(String memberPhoneNumber) {
        return memberRepository.findByPhoneNumber(memberPhoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private void saveIfNew(MemberNotificationSetting setting) {
        if (setting.getId() == null) {
            notificationSettingRepository.save(setting);
        }
    }

    private void validateNotificationSettingsRequest(NotificationSettingsUpdateRequest request) {
        if (request == null
                || request.diaryReminderEnabled() == null
                || request.fortuneActionEnabled() == null) {
            throw new CustomException(ErrorCode.INVALID_NOTIFICATION_SETTINGS);
        }
    }

    private void validateNotificationDaysRequest(NotificationDaysUpdateRequest request) {
        if (request == null
                || request.mondayEnabled() == null
                || request.tuesdayEnabled() == null
                || request.wednesdayEnabled() == null
                || request.thursdayEnabled() == null
                || request.fridayEnabled() == null
                || request.saturdayEnabled() == null
                || request.sundayEnabled() == null) {
            throw new CustomException(ErrorCode.INVALID_NOTIFICATION_DAYS);
        }
    }
}
