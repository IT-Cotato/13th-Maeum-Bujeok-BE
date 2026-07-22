package com.maumbujeok.backend.domain.member.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberNotificationSetting;
import com.maumbujeok.backend.domain.member.repository.MemberNotificationSettingRepository;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MypageNotificationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberNotificationSettingRepository notificationSettingRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @Test
    void rejectsUnauthenticatedNotificationUpdate() throws Exception {
        mockMvc.perform(patch("/api/mypage/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"diaryReminderEnabled\":true,\"fortuneActionEnabled\":false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatesNotificationSettingsForAuthenticatedMember() throws Exception {
        Member member = saveMember("notification-user", "01000000111");
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(patch("/api/mypage/notifications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"diaryReminderEnabled\":true,\"fortuneActionEnabled\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.diaryReminderEnabled").value(true))
                .andExpect(jsonPath("$.data.fortuneActionEnabled").value(false));

        MemberNotificationSetting setting = notificationSettingRepository.findByMemberPhoneNumber(member.getPhoneNumber())
                .orElseThrow();
        assertTrue(setting.isDiaryReminderEnabled());
        assertFalse(setting.isFortuneActionEnabled());
    }

    @Test
    void updatesNotificationDaysForAuthenticatedMember() throws Exception {
        Member member = saveMember("day-user", "01000000112");
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(patch("/api/mypage/notifications/days")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mondayEnabled": true,
                                  "tuesdayEnabled": false,
                                  "wednesdayEnabled": true,
                                  "thursdayEnabled": false,
                                  "fridayEnabled": true,
                                  "saturdayEnabled": false,
                                  "sundayEnabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mondayEnabled").value(true))
                .andExpect(jsonPath("$.data.tuesdayEnabled").value(false))
                .andExpect(jsonPath("$.data.wednesdayEnabled").value(true))
                .andExpect(jsonPath("$.data.thursdayEnabled").value(false))
                .andExpect(jsonPath("$.data.fridayEnabled").value(true))
                .andExpect(jsonPath("$.data.saturdayEnabled").value(false))
                .andExpect(jsonPath("$.data.sundayEnabled").value(false));

        MemberNotificationSetting setting = notificationSettingRepository.findByMemberPhoneNumber(member.getPhoneNumber())
                .orElseThrow();
        assertTrue(setting.isMondayEnabled());
        assertFalse(setting.isTuesdayEnabled());
        assertTrue(setting.isWednesdayEnabled());
        assertFalse(setting.isThursdayEnabled());
        assertTrue(setting.isFridayEnabled());
        assertFalse(setting.isSaturdayEnabled());
        assertFalse(setting.isSundayEnabled());
    }

    @Test
    void returnsBadRequestWhenNotificationSettingsRequestIsIncomplete() throws Exception {
        Member member = saveMember("invalid-notification-user", "01000000113");
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(patch("/api/mypage/notifications")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"diaryReminderEnabled\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MEMBER_001"));
    }

    @Test
    void returnsBadRequestWhenNotificationDaysRequestIsIncomplete() throws Exception {
        Member member = saveMember("invalid-day-user", "01000000114");
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(patch("/api/mypage/notifications/days")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "mondayEnabled": true,
                                  "tuesdayEnabled": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MEMBER_002"));
    }

    private Member saveMember(String name, String phoneNumber) {
        return memberRepository.save(Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .passwordHash("encoded-password")
                .role(Member.Role.ROLE_USER)
                .build());
    }
}
