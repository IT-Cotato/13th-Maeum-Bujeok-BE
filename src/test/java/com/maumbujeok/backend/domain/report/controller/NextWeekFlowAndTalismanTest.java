package com.maumbujeok.backend.domain.report.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlow;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.domain.report.repository.NextWeekFlowRepository;
import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import com.maumbujeok.backend.domain.talisman.domain.TalismanGenerationStatus;
import com.maumbujeok.backend.domain.talisman.repository.TalismanRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import java.time.LocalDate;
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
class NextWeekFlowAndTalismanTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired EmotionReportRepository emotionReportRepository;
    @Autowired NextWeekFlowRepository nextWeekFlowRepository;
    @Autowired TalismanRepository talismanRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @Test
    void rejectsUnauthenticatedAccessToNextWeekFlow() throws Exception {
        mockMvc.perform(post("/api/reports/next-week-flow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"2026-07-13\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/reports/next-week-flow/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsUnauthenticatedAccessToTalismans() throws Exception {
        mockMvc.perform(get("/api/talismans"))
                .andExpect(status().isForbidden());
    }

    @Test
    void generateNextWeekFlowFailsIfReportNotFound() throws Exception {
        Member member = saveMember("user1", "01099990001");
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(post("/api/reports/next-week-flow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"2026-07-13\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REPORT_404"));
    }

    @Test
    void generateNextWeekFlowStartsSuccessfully() throws Exception {
        Member member = saveMember("user2", "01099990002");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 13));
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(post("/api/reports/next-week-flow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"2026-07-13\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flowId").exists())
                .andExpect(jsonPath("$.data.emotionReportId").value(report.getId()))
                .andExpect(jsonPath("$.data.weekStart").value("2026-07-13"))
                .andExpect(jsonPath("$.data.generationStatus").value("PROCESSING"));
    }

    @Test
    void getNextWeekFlowReturnsValue() throws Exception {
        Member member = saveMember("user3", "01099990003");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 13));
        NextWeekFlow flow = NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(LocalDate.of(2026, 7, 13))
                .periodStart(LocalDate.of(2026, 7, 20))
                .periodEnd(LocalDate.of(2026, 7, 26))
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build();
        nextWeekFlowRepository.save(flow);

        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(get("/api/reports/next-week-flow/{flowId}", flow.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flowId").value(flow.getId()))
                .andExpect(jsonPath("$.data.generationStatus").value("PROCESSING"));
    }

    @Test
    void getNextWeekFlowRejectsOtherUser() throws Exception {
        Member owner = saveMember("owner", "01099990004");
        Member guest = saveMember("guest", "01099990005");
        EmotionReport report = saveCompletedReport(owner, LocalDate.of(2026, 7, 13));
        NextWeekFlow flow = NextWeekFlow.builder()
                .member(owner)
                .emotionReport(report)
                .weekStart(LocalDate.of(2026, 7, 13))
                .periodStart(LocalDate.of(2026, 7, 20))
                .periodEnd(LocalDate.of(2026, 7, 26))
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build();
        nextWeekFlowRepository.save(flow);

        String guestToken = jwtTokenProvider.createToken(guest.getPhoneNumber(), guest.getRole().name());

        mockMvc.perform(get("/api/reports/next-week-flow/{flowId}", flow.getId())
                        .header("Authorization", "Bearer " + guestToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("COMMON_403"));
    }

    @Test
    void getNextWeekFlowReturnsDefaultAdviceTextOnFailure() throws Exception {
        Member member = saveMember("user4", "01099990006");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 13));
        NextWeekFlow flow = NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(LocalDate.of(2026, 7, 13))
                .periodStart(LocalDate.of(2026, 7, 20))
                .periodEnd(LocalDate.of(2026, 7, 26))
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build();
        flow.fail();
        nextWeekFlowRepository.save(flow);

        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(get("/api/reports/next-week-flow/{flowId}", flow.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generationStatus").value("FAILED"))
                .andExpect(jsonPath("$.data.advice.content").value("AI 조언 생성에 실패했습니다. 다음 주 흐름 분석을 다시 요청해 주세요."));
    }

    @Autowired jakarta.persistence.EntityManager entityManager;

    @Test
    void getTalismanListReturnsValuesWithPaginationAndWeeklyFiltering() throws Exception {
        java.time.LocalDate today = java.time.LocalDate.now();
        Member member = saveMember("user5", "01099990007");

        // 1. Create a talisman from 2 weeks ago (should NOT be returned in this week's query)
        Talisman oldTalisman = Talisman.builder()
                .member(member)
                .title("과거부적")
                .message("건강을 기원합니다.")
                .designType("A")
                .generationStatus(TalismanGenerationStatus.COMPLETED)
                .recordedAt(today.minusWeeks(2))
                .build();
        talismanRepository.saveAndFlush(oldTalisman);

        // 2. Create 4 talismans for this week
        Talisman t1 = talismanRepository.save(Talisman.builder().member(member).title("부적1").designType("A").generationStatus(TalismanGenerationStatus.COMPLETED).recordedAt(today).build());
        Talisman t2 = talismanRepository.save(Talisman.builder().member(member).title("부적2").designType("A").generationStatus(TalismanGenerationStatus.COMPLETED).recordedAt(today).build());
        Talisman t3 = talismanRepository.save(Talisman.builder().member(member).title("부적3").designType("A").generationStatus(TalismanGenerationStatus.COMPLETED).recordedAt(today).build());
        Talisman t4 = talismanRepository.save(Talisman.builder().member(member).title("부적4").designType("A").generationStatus(TalismanGenerationStatus.COMPLETED).recordedAt(today).build());
        talismanRepository.flush();

        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        // 3. Request page 1 with size 3 (should return 부적4, 부적3, 부적2 and hasNext = true)
        mockMvc.perform(get("/api/talismans")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(3))
                .andExpect(jsonPath("$.data.hasNext").value(true))
                .andExpect(jsonPath("$.data.nextCursor").value(t2.getId()))
                .andExpect(jsonPath("$.data.items", hasSize(3)))
                .andExpect(jsonPath("$.data.items[0].title").value("부적4"))
                .andExpect(jsonPath("$.data.items[1].title").value("부적3"))
                .andExpect(jsonPath("$.data.items[2].title").value("부적2"));

        // 4. Request page 2 with cursor pointing to t2 (should return 부적1, and hasNext = false, nextCursor = null)
        mockMvc.perform(get("/api/talismans")
                        .header("Authorization", "Bearer " + token)
                        .param("cursor", String.valueOf(t2.getId()))
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.count").value(1))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andExpect(jsonPath("$.data.nextCursor").value(nullValue()))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.data.items[0].title").value("부적1"));
    }

    private Member saveMember(String name, String phone) {
        return memberRepository.save(Member.builder()
                .name(name)
                .phoneNumber(phone)
                .email(phone + "@test.com")
                .passwordHash("password")
                .role(Member.Role.ROLE_USER)
                .build());
    }

    private EmotionReport saveCompletedReport(Member member, LocalDate periodStart) {
        EmotionReport report = new EmotionReport(
                member,
                EmotionReportType.WEEKLY,
                periodStart,
                periodStart.plusDays(6),
                "weekly-report-summary-v1"
        );
        report.markProcessing();
        com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult result =
                new com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult("완료된 주간 요약", "gpt-4o-mini");
        report.complete(result, 1);
        return emotionReportRepository.save(report);
    }
}
