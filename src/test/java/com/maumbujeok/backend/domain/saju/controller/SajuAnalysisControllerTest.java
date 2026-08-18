package com.maumbujeok.backend.domain.saju.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.saju.ai.SajuAiPromptVersion;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysisStatus;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SajuAnalysisControllerTest {

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
    void rejectsUnauthenticatedCreateAndQuery() throws Exception {
        mockMvc.perform(post("/api/saju/analyses"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/saju/analyses/{analysisId}", 1L))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/saju/analyses/latest"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createsPendingAnalysis() throws Exception {
        Member member = saveMember("분석 사용자", "01020000001", "19900101");
        saveSajuProfile(member, MemberSajuProfile.Gender.FEMALE, LocalTime.of(14, 30));

        mockMvc.perform(post("/api/saju/analyses")
                        .header("Authorization", authorization(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analysisId").isNumber())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.message").value("사주 분석 요청이 생성되었습니다."));

        SajuAnalysis analysis = sajuAnalysisRepository.findAll().get(0);
        assertEquals("19900101", analysis.getBirthDate());
        assertEquals(MemberSajuProfile.Gender.FEMALE, analysis.getGender());
        assertEquals(LocalTime.of(14, 30), analysis.getBirthTime());
        assertEquals(SajuAiPromptVersion.VALUE, analysis.getPromptVersion());
    }

    @Test
    void postStartsAnalysisImmediatelyAndGetEventuallyReturnsCompletedResult() throws Exception {
        Member member = saveMember("완료 사용자", "01020000002", "19930516");
        saveSajuProfile(member, MemberSajuProfile.Gender.MALE, LocalTime.of(9, 15));

        mockMvc.perform(post("/api/saju/analyses")
                        .header("Authorization", authorization(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        SajuAnalysis analysis = sajuAnalysisRepository.findAll().get(0);
        SajuAnalysis completed = waitUntilFinished(analysis.getId());
        assertEquals(SajuAnalysisStatus.COMPLETED, completed.getStatus());
        assertNotNull(completed.getAnalyzedAt());
        assertNotNull(completed.getModelName());
        assertEquals(100, completed.getWoodPercentage()
                + completed.getFirePercentage()
                + completed.getEarthPercentage()
                + completed.getMetalPercentage()
                + completed.getWaterPercentage());

        mockMvc.perform(get("/api/saju/analyses/{analysisId}", analysis.getId())
                        .header("Authorization", authorization(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.elements.wood").isNumber())
                .andExpect(jsonPath("$.data.elements.fire").isNumber())
                .andExpect(jsonPath("$.data.elements.earth").isNumber())
                .andExpect(jsonPath("$.data.elements.metal").isNumber())
                .andExpect(jsonPath("$.data.elements.water").isNumber())
                .andExpect(jsonPath("$.data.modelName").isString())
                .andExpect(jsonPath("$.data.failureCode").value(nullValue()))
                .andExpect(jsonPath("$.data.analyzedAt").isNotEmpty());
    }

    @Test
    void getDoesNotStartPendingAnalysis() throws Exception {
        Member member = saveMember("대기 사용자", "01020000008", "19940203");
        SajuAnalysis analysis = sajuAnalysisRepository.save(new SajuAnalysis(
                member,
                member.getBirthDate(),
                MemberSajuProfile.Gender.NONE,
                MemberSajuProfile.CalendarType.SOLAR,
                null,
                SajuAiPromptVersion.VALUE
        ));

        mockMvc.perform(get("/api/saju/analyses/{analysisId}", analysis.getId())
                        .header("Authorization", authorization(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analysisId").value(analysis.getId()))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.elements").value(nullValue()))
                .andExpect(jsonPath("$.data.modelName").value(nullValue()))
                .andExpect(jsonPath("$.data.failureCode").value(nullValue()))
                .andExpect(jsonPath("$.data.analyzedAt").value(nullValue()));

        SajuAnalysis pending = sajuAnalysisRepository.findById(analysis.getId()).orElseThrow();
        assertEquals(SajuAnalysisStatus.PENDING, pending.getStatus());
    }

    @Test
    void getLatestReturnsLatestAnalysis() throws Exception {
        Member member = saveMember("최신 사용자", "01020000009", "19930722");
        saveSajuProfile(member, MemberSajuProfile.Gender.FEMALE, LocalTime.of(6, 45));

        mockMvc.perform(post("/api/saju/analyses")
                        .header("Authorization", authorization(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        SajuAnalysis completed = waitUntilFinished(sajuAnalysisRepository.findAll().get(0).getId());

        mockMvc.perform(get("/api/saju/analyses/latest")
                        .header("Authorization", authorization(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.analysisId").value(completed.getId()))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.elements.wood").isNumber());
    }

    @Test
    void getLatestReturnsNotFoundWhenNoAnalysisExists() throws Exception {
        Member member = saveMember("미분석 사용자", "01020000010", "19930722");

        mockMvc.perform(get("/api/saju/analyses/latest")
                        .header("Authorization", authorization(member)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SAJU_404"));
    }

    @Test
    void enforcesSingleAnalysisPerMemberAtDatabaseLevel() {
        Member member = saveMember("유니크 사용자", "01020000011", "19930722");

        sajuAnalysisRepository.saveAndFlush(new SajuAnalysis(
                member,
                member.getBirthDate(),
                MemberSajuProfile.Gender.FEMALE,
                MemberSajuProfile.CalendarType.SOLAR,
                LocalTime.of(9, 0),
                SajuAiPromptVersion.VALUE
        ));

        assertThrows(DataIntegrityViolationException.class, () ->
                sajuAnalysisRepository.saveAndFlush(new SajuAnalysis(
                        member,
                        member.getBirthDate(),
                        MemberSajuProfile.Gender.FEMALE,
                        MemberSajuProfile.CalendarType.SOLAR,
                        LocalTime.of(9, 0),
                        SajuAiPromptVersion.VALUE
                ))
        );
    }

    @Test
    void returnsFailedAnalysisState() throws Exception {
        Member member = saveMember("실패 사용자", "01020000003", "19881224");
        SajuAnalysis analysis = sajuAnalysisRepository.save(new SajuAnalysis(
                member,
                member.getBirthDate(),
                MemberSajuProfile.Gender.NONE,
                MemberSajuProfile.CalendarType.SOLAR,
                null,
                SajuAiPromptVersion.VALUE
        ));
        analysis.markProcessing(1L);
        analysis.fail(1L, 2, "AI_TIMEOUT");
        sajuAnalysisRepository.save(analysis);

        mockMvc.perform(get("/api/saju/analyses/{analysisId}", analysis.getId())
                        .header("Authorization", authorization(member)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("FAILED"))
                .andExpect(jsonPath("$.data.elements").value(nullValue()))
                .andExpect(jsonPath("$.data.modelName").value(nullValue()))
                .andExpect(jsonPath("$.data.failureCode").value("AI_TIMEOUT"))
                .andExpect(jsonPath("$.data.analyzedAt").isNotEmpty());
    }

    @Test
    void createFailsWhenBirthDateIsMissing() throws Exception {
        Member member = saveMember("생일 없음", "01020000004", null);
        saveSajuProfile(member, MemberSajuProfile.Gender.FEMALE, LocalTime.of(7, 0));

        mockMvc.perform(post("/api/saju/analyses")
                        .header("Authorization", authorization(member)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SAJU_400"))
                .andExpect(jsonPath("$.message").value("사주 분석을 위해 yyyyMMdd 형식의 생년월일이 필요합니다."));
    }

    @Test
    void createFailsWhenSajuProfileIsMissing() throws Exception {
        Member member = saveMember("프로필 없음", "01020000005", "19990101");

        mockMvc.perform(post("/api/saju/analyses")
                        .header("Authorization", authorization(member)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEMBER_003"));
    }

    @Test
    void hidesAnotherMembersAnalysis() throws Exception {
        Member owner = saveMember("주인", "01020000006", "19900101");
        Member stranger = saveMember("타인", "01020000007", "19920202");
        SajuAnalysis analysis = sajuAnalysisRepository.save(new SajuAnalysis(
                owner,
                owner.getBirthDate(),
                MemberSajuProfile.Gender.MALE,
                MemberSajuProfile.CalendarType.SOLAR,
                LocalTime.of(8, 0),
                SajuAiPromptVersion.VALUE
        ));

        mockMvc.perform(get("/api/saju/analyses/{analysisId}", analysis.getId())
                        .header("Authorization", authorization(stranger)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SAJU_404"))
                .andExpect(jsonPath("$.message").value("사주 분석 요청을 찾을 수 없습니다."));
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

    private MemberSajuProfile saveSajuProfile(
            Member member,
            MemberSajuProfile.Gender gender,
            LocalTime birthTime
    ) {
        return sajuProfileRepository.save(MemberSajuProfile.builder()
                .member(member)
                .gender(gender)
                .calendarType(MemberSajuProfile.CalendarType.SOLAR)
                .birthTime(birthTime)
                .build());
    }

    private String authorization(Member member) {
        return "Bearer " + jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());
    }
}
