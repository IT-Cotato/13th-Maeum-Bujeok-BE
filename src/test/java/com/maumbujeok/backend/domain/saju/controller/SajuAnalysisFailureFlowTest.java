package com.maumbujeok.backend.domain.saju.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.saju.ai.SajuAiException;
import com.maumbujeok.backend.domain.saju.ai.SajuAiProvider;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysisStatus;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import com.maumbujeok.backend.domain.home.repository.HomeSummaryRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SajuAnalysisFailureFlowTest.FailingSajuAiProviderConfig.class)
class SajuAnalysisFailureFlowTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberSajuProfileRepository sajuProfileRepository;
    @Autowired SajuAnalysisRepository sajuAnalysisRepository;
    @Autowired HomeSummaryRepository homeSummaryRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        sajuAnalysisRepository.deleteAllInBatch();
        homeSummaryRepository.deleteAllInBatch();
        sajuProfileRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    void transitionsToFailedWhenAiProviderThrows() throws Exception {
        Member member = saveMember("실패 유도", "01040000001");
        sajuProfileRepository.save(MemberSajuProfile.builder()
                .member(member)
                .gender(MemberSajuProfile.Gender.FEMALE)
                .calendarType(MemberSajuProfile.CalendarType.SOLAR)
                .birthTime(LocalTime.of(15, 0))
                .build());

        mockMvc.perform(post("/api/saju/analyses")
                        .header("Authorization", authorization(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        SajuAnalysis analysis = sajuAnalysisRepository.findAll().get(0);

        SajuAnalysis failed = waitUntilFinished(analysis.getId());
        assertEquals(SajuAnalysisStatus.FAILED, failed.getStatus());
        assertEquals("AI_TIMEOUT", failed.getFailureCode());
        assertEquals(2, failed.getAttemptCount());
        assertTrue(failed.getAnalyzedAt() != null);
        assertNull(failed.getModelName());
        assertNull(failed.getWoodPercentage());
        assertNull(failed.getFirePercentage());
        assertNull(failed.getEarthPercentage());
        assertNull(failed.getMetalPercentage());
        assertNull(failed.getWaterPercentage());

        mockMvc.perform(get("/api/saju/analyses/{analysisId}", analysis.getId())
                        .header("Authorization", authorization(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.failureCode").value("AI_TIMEOUT"))
                .andExpect(jsonPath("$.data.modelName").value(nullValue()))
                .andExpect(jsonPath("$.data.elements").value(nullValue()))
                .andExpect(jsonPath("$.data.analyzedAt").isNotEmpty());
    }

    private SajuAnalysis waitUntilFinished(Long analysisId) throws InterruptedException {
        for (int index = 0; index < 40; index++) {
            SajuAnalysis analysis = sajuAnalysisRepository.findById(analysisId).orElseThrow();
            if (analysis.getStatus() == SajuAnalysisStatus.COMPLETED
                    || analysis.getStatus() == SajuAnalysisStatus.FAILED) {
                return analysis;
            }
            Thread.sleep(50);
        }
        throw new AssertionError("Saju analysis did not finish in time");
    }

    private Member saveMember(String name, String phoneNumber) {
        return memberRepository.save(Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .passwordHash("encoded-password")
                .birthDate("19940501")
                .provider(Member.Provider.LOCAL)
                .role(Member.Role.ROLE_USER)
                .build());
    }

    private String authorization(Member member) {
        return "Bearer " + jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());
    }

    @TestConfiguration
    static class FailingSajuAiProviderConfig {
        @Bean
        @Primary
        SajuAiProvider failingSajuAiProvider() {
            return request -> {
                throw new SajuAiException("AI_TIMEOUT", 2, new RuntimeException("timeout"));
            };
        }
    }
}
