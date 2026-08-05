package com.maumbujeok.backend.domain.member.controller;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberSajuProfileRepository sajuProfileRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        sajuProfileRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    void onboardingStoresBirthDateSajuAndConsent() throws Exception {
        Member member = memberRepository.save(Member.builder()
                .phoneNumber("01012345678")
                .name("홍길동")
                .provider(Member.Provider.LOCAL)
                .role(Member.Role.ROLE_USER)
                .build());

        mockMvc.perform(post("/api/members/onboarding")
                        .header("Authorization", "Bearer " + jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "19990101",
                                  "gender": "FEMALE",
                                  "calendarType": "SOLAR",
                                  "birthTime": "14:30",
                                  "termsAgreed": true,
                                  "privacyAgreed": true,
                                  "sensitiveDataAgreed": true,
                                  "marketingAgreed": false
                                }
                                """))
                .andExpect(status().isOk());

        Member savedMember = memberRepository.findById(member.getPhoneNumber()).orElseThrow();
        assertEquals("19990101", savedMember.getBirthDate());
        assertNotNull(savedMember.getTermsAgreedAt());
        assertNotNull(savedMember.getPrivacyAgreedAt());
        assertNotNull(savedMember.getSensitiveDataAgreedAt());
        assertNull(savedMember.getMarketingAgreedAt());

        var sajuProfile = sajuProfileRepository.findByMember(savedMember).orElseThrow();
        assertEquals(MemberSajuProfile.Gender.FEMALE, sajuProfile.getGender());
        assertEquals(MemberSajuProfile.CalendarType.SOLAR, sajuProfile.getCalendarType());
        assertEquals("14:30", sajuProfile.getBirthTime().toString());
    }
    @Test
    void onboardingRejectsNonexistentBirthDate() throws Exception {
        Member member = memberRepository.save(Member.builder()
                .phoneNumber("01098765432")
                .name("tester")
                .provider(Member.Provider.LOCAL)
                .role(Member.Role.ROLE_USER)
                .build());

        mockMvc.perform(post("/api/members/onboarding")
                        .header("Authorization", "Bearer " + jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "19990230",
                                  "gender": "FEMALE",
                                  "calendarType": "SOLAR",
                                  "termsAgreed": true,
                                  "privacyAgreed": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400"));
    }
}