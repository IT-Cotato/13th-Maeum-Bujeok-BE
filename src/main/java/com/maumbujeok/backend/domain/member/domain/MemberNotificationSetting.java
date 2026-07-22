package com.maumbujeok.backend.domain.member.domain;

import com.maumbujeok.backend.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_notification_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberNotificationSetting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_setting_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(name = "diary_reminder_enabled", nullable = false)
    private boolean diaryReminderEnabled;

    @Column(name = "diary_reminder_time")
    private LocalTime diaryReminderTime;

    @Column(name = "fortune_action_enabled", nullable = false)
    private boolean fortuneActionEnabled;

    @Column(name = "report_notification_enabled", nullable = false)
    private boolean reportNotificationEnabled;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled;

    @Column(name = "monday_enabled", nullable = false)
    private boolean mondayEnabled;

    @Column(name = "tuesday_enabled", nullable = false)
    private boolean tuesdayEnabled;

    @Column(name = "wednesday_enabled", nullable = false)
    private boolean wednesdayEnabled;

    @Column(name = "thursday_enabled", nullable = false)
    private boolean thursdayEnabled;

    @Column(name = "friday_enabled", nullable = false)
    private boolean fridayEnabled;

    @Column(name = "saturday_enabled", nullable = false)
    private boolean saturdayEnabled;

    @Column(name = "sunday_enabled", nullable = false)
    private boolean sundayEnabled;

    private MemberNotificationSetting(Member member) {
        this.member = member;
        this.diaryReminderEnabled = false;
        this.fortuneActionEnabled = true;
        this.reportNotificationEnabled = true;
        this.pushEnabled = true;
    }

    public static MemberNotificationSetting create(Member member) {
        return new MemberNotificationSetting(member);
    }

    public void updateNotificationSettings(boolean diaryReminderEnabled, boolean fortuneActionEnabled) {
        this.diaryReminderEnabled = diaryReminderEnabled;
        this.fortuneActionEnabled = fortuneActionEnabled;
    }

    public void updateNotificationDays(
            boolean mondayEnabled,
            boolean tuesdayEnabled,
            boolean wednesdayEnabled,
            boolean thursdayEnabled,
            boolean fridayEnabled,
            boolean saturdayEnabled,
            boolean sundayEnabled
    ) {
        this.mondayEnabled = mondayEnabled;
        this.tuesdayEnabled = tuesdayEnabled;
        this.wednesdayEnabled = wednesdayEnabled;
        this.thursdayEnabled = thursdayEnabled;
        this.fridayEnabled = fridayEnabled;
        this.saturdayEnabled = saturdayEnabled;
        this.sundayEnabled = sundayEnabled;
    }
}
