package com.maumbujeok.backend.domain.member.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MypageTermsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @Test
    void rejectsUnauthenticatedTermsLookup() throws Exception {
        mockMvc.perform(get("/api/mypage/terms"))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsServiceTermsForAuthenticatedMember() throws Exception {
        String token = tokenFor("terms-user", "01000000201");

        mockMvc.perform(get("/api/mypage/terms").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].termType").value("SERVICE_TERMS"))
                .andExpect(jsonPath("$.data[0].title").value("서비스 이용약관"))
                .andExpect(jsonPath("$.data[0].required").value(true))
                .andExpect(jsonPath("$.data[0].version").value("v1.0"));
    }

    @Test
    void returnsServiceTermsDetailForAuthenticatedMember() throws Exception {
        String token = tokenFor("terms-detail-user", "01000000202");

        mockMvc.perform(get("/api/mypage/terms/SERVICE_TERMS").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.termType").value("SERVICE_TERMS"))
                .andExpect(jsonPath("$.data.content").value(org.hamcrest.Matchers.containsString("제 1 조 (목적)")));
    }

    @Test
    void returnsNotFoundForUnsupportedTermType() throws Exception {
        String token = tokenFor("terms-invalid-user", "01000000203");

        mockMvc.perform(get("/api/mypage/terms/PRIVACY_POLICY").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEMBER_004"));
    }

    private String tokenFor(String name, String phoneNumber) {
        Member member = memberRepository.save(Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .passwordHash("encoded-password")
                .role(Member.Role.ROLE_USER)
                .build());
        return jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());
    }
}
