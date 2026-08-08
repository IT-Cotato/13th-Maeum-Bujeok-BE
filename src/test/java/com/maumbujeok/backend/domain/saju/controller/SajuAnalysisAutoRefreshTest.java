package com.maumbujeok.backend.domain.saju.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysisStatus;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SajuAnalysisAutoRefreshTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberSajuProfileRepository sajuProfileRepository;
    @Autowired SajuAnalysisRepository sajuAnalysisRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        sajuAnalysisRepository.deleteAllInBatch();
        sajuProfileRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    void onboardingAutomaticallyCreatesLatestSajuAnalysis() throws Exception {
        Member member = saveMember("온보딩 사용자", "01050000001", null);

        mockMvc.perform(post("/api/members/onboarding")
                        .header("Authorization", authorization(member))
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

        SajuAnalysis analysis = waitUntilFinished(member.getPhoneNumber());
        assertEquals(SajuAnalysisStatus.COMPLETED, analysis.getStatus());
        assertEquals("19990101", analysis.getBirthDate());
        assertEquals(MemberSajuProfile.Gender.FEMALE, analysis.getGender());
        assertEquals("14:30", analysis.getBirthTime().toString());

        mockMvc.perform(get("/api/saju/analyses/latest")
                        .header("Authorization", authorization(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analysisId").value(analysis.getId()))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.elements.wood").isNumber());
    }

    @Test
    void profileUpdateRefreshesLatestAnalysisWhenSajuInputChanges() throws Exception {
        Member member = saveMember("수정 사용자", "01050000002", null);

        mockMvc.perform(post("/api/members/onboarding")
                        .header("Authorization", authorization(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "19900315",
                                  "gender": "MALE",
                                  "calendarType": "SOLAR",
                                  "birthTime": "08:00",
                                  "termsAgreed": true,
                                  "privacyAgreed": true,
                                  "sensitiveDataAgreed": true,
                                  "marketingAgreed": false
                                }
                                """))
                .andExpect(status().isOk());

        SajuAnalysis initial = waitUntilFinished(member.getPhoneNumber());
        long initialSequence = initial.getRequestSequence();

        mockMvc.perform(patch("/api/mypage/profile")
                        .header("Authorization", authorization(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "수정 사용자",
                                  "birthDate": "19910420",
                                  "birthTime": "18:45",
                                  "phoneNumber": "01050000002",
                                  "gender": "FEMALE"
                                }
                                """))
                .andExpect(status().isOk());

        SajuAnalysis refreshed = waitUntilRevisionFinished(member.getPhoneNumber(), initialSequence + 1);
        assertEquals(initial.getId(), refreshed.getId());
        assertEquals(initialSequence + 1, refreshed.getRequestSequence());
        assertEquals(SajuAnalysisStatus.COMPLETED, refreshed.getStatus());
        assertEquals("19910420", refreshed.getBirthDate());
        assertEquals(MemberSajuProfile.Gender.FEMALE, refreshed.getGender());
        assertEquals("18:45", refreshed.getBirthTime().toString());
        assertEquals(1, latestAnalyses(member.getPhoneNumber()).size());
    }

    @Test
    void profileUpdateDoesNotRefreshWhenOnlyNameChanges() throws Exception {
        Member member = saveMember("이전 이름", "01050000003", null);

        mockMvc.perform(post("/api/members/onboarding")
                        .header("Authorization", authorization(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "19920510",
                                  "gender": "FEMALE",
                                  "calendarType": "SOLAR",
                                  "birthTime": "10:30",
                                  "termsAgreed": true,
                                  "privacyAgreed": true,
                                  "sensitiveDataAgreed": true,
                                  "marketingAgreed": false
                                }
                                """))
                .andExpect(status().isOk());

        SajuAnalysis initial = waitUntilFinished(member.getPhoneNumber());
        long initialSequence = initial.getRequestSequence();

        mockMvc.perform(patch("/api/mypage/profile")
                        .header("Authorization", authorization(member))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 이름",
                                  "birthDate": "19920510",
                                  "birthTime": "10:30",
                                  "phoneNumber": "01050000003",
                                  "gender": "FEMALE"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profile.name").value("새 이름"));

        Thread.sleep(150);

        SajuAnalysis unchanged = latestAnalyses(member.getPhoneNumber()).get(0);
        assertEquals(initial.getId(), unchanged.getId());
        assertEquals(initialSequence, unchanged.getRequestSequence());
        assertEquals(SajuAnalysisStatus.COMPLETED, unchanged.getStatus());
        assertTrue(unchanged.getFailureCode() == null);
    }

    private SajuAnalysis waitUntilFinished(String phoneNumber) throws InterruptedException {
        for (int index = 0; index < 40; index++) {
            List<SajuAnalysis> analyses = latestAnalyses(phoneNumber);
            if (!analyses.isEmpty()) {
                SajuAnalysis analysis = analyses.get(0);
                if (analysis.getStatus() == SajuAnalysisStatus.COMPLETED
                        || analysis.getStatus() == SajuAnalysisStatus.FAILED) {
                    return analysis;
                }
            }
            Thread.sleep(50);
        }
        throw new AssertionError("Saju analysis did not finish in time");
    }

    private SajuAnalysis waitUntilRevisionFinished(String phoneNumber, long requestSequence) throws InterruptedException {
        for (int index = 0; index < 40; index++) {
            List<SajuAnalysis> analyses = latestAnalyses(phoneNumber);
            if (!analyses.isEmpty()) {
                SajuAnalysis analysis = analyses.get(0);
                if (analysis.getRequestSequence() == requestSequence
                        && (analysis.getStatus() == SajuAnalysisStatus.COMPLETED
                        || analysis.getStatus() == SajuAnalysisStatus.FAILED)) {
                    return analysis;
                }
            }
            Thread.sleep(50);
        }
        throw new AssertionError("Saju analysis refresh did not finish in time");
    }

    private List<SajuAnalysis> latestAnalyses(String phoneNumber) {
        return sajuAnalysisRepository.findAllByMemberPhoneNumberOrderByCreatedAtDescIdDesc(phoneNumber);
    }

    private Member saveMember(String name, String phoneNumber, String birthDate) {
        return memberRepository.save(Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .passwordHash("encoded-password")
                .birthDate(birthDate)
                .provider(Member.Provider.LOCAL)
                .role(Member.Role.ROLE_USER)
                .build());
    }

    private String authorization(Member member) {
        return "Bearer " + jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());
    }
}
