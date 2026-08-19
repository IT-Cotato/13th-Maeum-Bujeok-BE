package com.maumbujeok.backend.domain.report.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maumbujeok.backend.domain.burn.application.BurningService;
import com.maumbujeok.backend.domain.burn.domain.BurningSourceType;
import com.maumbujeok.backend.domain.burn.dto.CreateBurningRequest;
import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryEmotion;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportGenerationStatus;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlow;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import com.maumbujeok.backend.domain.report.application.NextWeekFlowTestClockConfig;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(NextWeekFlowTestClockConfig.class)
class NextWeekFlowAndTalismanTest {

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired DiaryRepository diaryRepository;
    @Autowired EmotionReportRepository emotionReportRepository;
    @Autowired NextWeekFlowRepository nextWeekFlowRepository;
    @Autowired TalismanRepository talismanRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired com.maumbujeok.backend.domain.report.application.NextWeekFlowAsyncService nextWeekFlowAsyncService;
    @Autowired com.maumbujeok.backend.domain.report.application.NextWeekFlowService nextWeekFlowService;
    @Autowired com.maumbujeok.backend.domain.report.application.NextWeekFlowGenerationStateService nextWeekFlowGenerationStateService;
    @Autowired BurningService burningService;

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
        saveDiaries(member, LocalDate.of(2026, 7, 13), 3);
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(post("/api/reports/next-week-flow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"2026-07-13\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REPORT_404"));
    }

    @Test
    void generatePastNextWeekFlowFailsIfLessThan3Diaries() throws Exception {
        Member member = saveMember("user1_few", "01099990011");
        LocalDate weekStart = LocalDate.of(2026, 7, 6);
        saveCompletedReport(member, weekStart);
        // Only 2 diaries
        saveDiaries(member, weekStart, 2);
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(post("/api/reports/next-week-flow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"2026-07-06\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REPORT_400"));
    }

    @Test
    void generateNextWeekFlowStartsSuccessfullyWith3Diaries() throws Exception {
        Member member = saveMember("user2", "01099990002");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 13));
        saveDiaries(member, LocalDate.of(2026, 7, 13), 3);
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
    void generateNextWeekFlowFailsWhenOneOfThreeDiariesIsBurned() throws Exception {
        Member member = saveMember("user2_burn", "01099990021");
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        saveCompletedReport(member, weekStart);
        saveDiaries(member, weekStart, 3);
        Diary burnedDiary = diaryRepository
                .findAllByMemberPhoneNumberAndRecordedDateOrderByCreatedAtDescIdDesc(
                        member.getPhoneNumber(), weekStart)
                .get(0);
        burningService.create(member.getPhoneNumber(),
                new CreateBurningRequest(BurningSourceType.DIARY, null, burnedDiary.getId()));

        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(post("/api/reports/next-week-flow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"2026-07-13\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REPORT_400"));

        EmotionReport report = emotionReportRepository
                .findByMemberPhoneNumberAndReportTypeAndPeriodStart(
                        member.getPhoneNumber(), EmotionReportType.WEEKLY, weekStart)
                .orElseThrow();
        nextWeekFlowService.refreshIfEligible(report.getId());
        org.junit.jupiter.api.Assertions.assertTrue(nextWeekFlowRepository
                .findByMemberPhoneNumberAndWeekStart(member.getPhoneNumber(), weekStart).isEmpty());
    }

    @Test
    void generateNextWeekFlowFailsWithTwoDiariesAndOneDirectBurning() throws Exception {
        Member member = saveMember("user2_direct", "01099990024");
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        EmotionReport report = saveCompletedReport(member, weekStart);
        saveDiaries(member, weekStart, 2);
        burningService.create(member.getPhoneNumber(),
                new CreateBurningRequest(BurningSourceType.DIRECT, "즉시 소각할 자유 텍스트", null));
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(post("/api/reports/next-week-flow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"2026-07-13\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REPORT_400"));

        nextWeekFlowService.refreshIfEligible(report.getId());
        org.junit.jupiter.api.Assertions.assertTrue(nextWeekFlowRepository
                .findByMemberPhoneNumberAndWeekStart(member.getPhoneNumber(), weekStart).isEmpty());
    }

    @Test
    void generateNextWeekFlowNormalizesNonMondayWeekStart() throws Exception {
        Member member = saveMember("user2_norm", "01099990022");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 13));
        saveDiaries(member, LocalDate.of(2026, 7, 13), 3);
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        // Request with Thursday 2026-07-16
        mockMvc.perform(post("/api/reports/next-week-flow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"2026-07-16\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flowId").exists())
                .andExpect(jsonPath("$.data.emotionReportId").value(report.getId()))
                .andExpect(jsonPath("$.data.weekStart").value("2026-07-13"))
                .andExpect(jsonPath("$.data.generationStatus").value("PROCESSING"));
    }

    @Test
    void generateNextWeekFlowFailsIfReportNotCompleted() throws Exception {
        Member member = saveMember("user2_proc", "01099990023");
        saveDiaries(member, LocalDate.of(2026, 7, 13), 3);
        // Report still in PROCESSING
        EmotionReport report = new EmotionReport(
                member,
                EmotionReportType.WEEKLY,
                LocalDate.of(2026, 7, 13),
                LocalDate.of(2026, 7, 19),
                "weekly-report-summary-v1"
        );
        report.markProcessing();
        emotionReportRepository.save(report);

        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(post("/api/reports/next-week-flow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"2026-07-13\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REPORT_400"));
    }

    @Test
    void getNextWeekFlowReturnsValue() throws Exception {
        Member member = saveMember("user3", "01099990003");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 6));
        NextWeekFlow flow = NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(LocalDate.of(2026, 7, 6))
                .periodStart(LocalDate.of(2026, 7, 13))
                .periodEnd(LocalDate.of(2026, 7, 19))
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
                .andExpect(jsonPath("$.data.adviceText").value("AI 조언 생성에 실패했습니다. 다음 주 흐름 분석을 다시 요청해 주세요."));
    }

    @Test
    void getReportNextWeekFlowAutoGeneratesOnDemandWhen3DiariesExist() throws Exception {
        Member member = saveMember("user_ondemand", "01099990088");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 13));
        saveDiaries(member, LocalDate.of(2026, 7, 13), 3);
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        // GET /api/reports/weekly/{reportId}/next-week-flow without calling POST first
        mockMvc.perform(get("/api/reports/weekly/{reportId}/next-week-flow", report.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flowId").exists())
                .andExpect(jsonPath("$.data.generationStatus").value("PROCESSING"));
    }

    @Test
    void getReportNextWeekFlowFailsWhenLessThan3Diaries() throws Exception {
        Member member = saveMember("user_ondemand_few", "01099990089");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 13));
        saveDiaries(member, LocalDate.of(2026, 7, 13), 2); // only 2 diaries
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(get("/api/reports/weekly/{reportId}/next-week-flow", report.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FLOW_001"));
    }

    @Test
    void getReportNextWeekFlowDoesNotRecreateIfAlreadyCompleted() throws Exception {
        Member member = saveMember("user_completed_flow", "01099990090");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 13));
        saveDiaries(member, LocalDate.of(2026, 7, 13), 3);
        NextWeekFlow flow = NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(LocalDate.of(2026, 7, 13))
                .periodStart(LocalDate.of(2026, 7, 20))
                .periodEnd(LocalDate.of(2026, 7, 26))
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build();
        flow.complete("이미 완료된 조언입니다.", "openai", "v1");
        nextWeekFlowRepository.saveAndFlush(flow);

        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(get("/api/reports/weekly/{reportId}/next-week-flow", report.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flowId").value(flow.getId()))
                .andExpect(jsonPath("$.data.generationStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.adviceText").value("이미 완료된 조언입니다."));
    }

    @Test
    void getReportNextWeekFlowReturnsFlowNotFoundAfterOneOfThreeDiariesIsBurned() throws Exception {
        Member member = saveMember("user_burned_existing_flow", "01099990091");
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        EmotionReport report = saveCompletedReport(member, weekStart);
        saveDiaries(member, weekStart, 3);
        NextWeekFlow flow = nextWeekFlowRepository.saveAndFlush(NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(weekStart)
                .periodStart(weekStart.plusDays(7))
                .periodEnd(weekStart.plusDays(13))
                .generationStatus(NextWeekFlowGenerationStatus.COMPLETED)
                .build());
        Diary burnedDiary = diaryRepository
                .findAllByMemberPhoneNumberAndRecordedDateOrderByCreatedAtDescIdDesc(
                        member.getPhoneNumber(), weekStart)
                .get(0);
        burningService.create(member.getPhoneNumber(),
                new CreateBurningRequest(BurningSourceType.DIARY, null, burnedDiary.getId()));
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(get("/api/reports/weekly/{reportId}/next-week-flow", report.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("FLOW_001"));

        org.junit.jupiter.api.Assertions.assertTrue(nextWeekFlowRepository.findById(flow.getId()).isPresent());
    }

    @Test
    void getReportNextWeekFlowReturnsExistingPastFlowWithoutRegeneration() throws Exception {
        Member member = saveMember("past_existing_flow", "01099990094");
        LocalDate weekStart = LocalDate.of(2026, 7, 6);
        EmotionReport report = saveCompletedReport(member, weekStart);
        saveDiaries(member, weekStart, 3);
        NextWeekFlow flow = NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(weekStart)
                .periodStart(weekStart.plusDays(7))
                .periodEnd(weekStart.plusDays(13))
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build();
        flow.complete("과거 조언", "fake", "v1");
        nextWeekFlowRepository.saveAndFlush(flow);
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(get("/api/reports/weekly/{reportId}/next-week-flow", report.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flowId").value(flow.getId()))
                .andExpect(jsonPath("$.data.adviceText").value("과거 조언"));
    }

    @Test
    void getReportNextWeekFlowAutoGeneratesForPastAndFutureWeeks() throws Exception {
        Member pastMember = saveMember("past_without_flow", "01099990095");
        LocalDate pastWeekStart = LocalDate.of(2026, 7, 6);
        EmotionReport pastReport = saveCompletedReport(pastMember, pastWeekStart);
        saveDiaries(pastMember, pastWeekStart, 3);
        String pastToken = jwtTokenProvider.createToken(pastMember.getPhoneNumber(), pastMember.getRole().name());

        Member futureMember = saveMember("future_without_flow", "01099990096");
        LocalDate futureWeekStart = LocalDate.of(2026, 7, 20);
        EmotionReport futureReport = saveCompletedReport(futureMember, futureWeekStart);
        saveDiaries(futureMember, futureWeekStart, 3);
        String futureToken = jwtTokenProvider.createToken(futureMember.getPhoneNumber(), futureMember.getRole().name());

        mockMvc.perform(get("/api/reports/weekly/{reportId}/next-week-flow", pastReport.getId())
                        .header("Authorization", "Bearer " + pastToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flowId").exists())
                .andExpect(jsonPath("$.data.periodStart").value("2026-07-13"))
                .andExpect(jsonPath("$.data.periodEnd").value("2026-07-19"))
                .andExpect(jsonPath("$.data.generationStatus").value("PROCESSING"));
        mockMvc.perform(get("/api/reports/weekly/{reportId}/next-week-flow", futureReport.getId())
                        .header("Authorization", "Bearer " + futureToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flowId").exists())
                .andExpect(jsonPath("$.data.periodStart").value("2026-07-27"))
                .andExpect(jsonPath("$.data.periodEnd").value("2026-08-02"))
                .andExpect(jsonPath("$.data.generationStatus").value("PROCESSING"));
    }

    @Test
    void postGeneratesForPastAndFutureWeeks() throws Exception {
        assertPostGenerationStartsForAnyWeek(
                "past_post", "01099990097", LocalDate.of(2026, 7, 6));
        assertPostGenerationStartsForAnyWeek(
                "future_post", "01099990098", LocalDate.of(2026, 7, 20));
    }

    @Test
    void refreshIfEligibleReusesCurrentReportGenerationForPastFlow() {
        Member member = saveMember("past_refresh", "01099990099");
        LocalDate weekStart = LocalDate.of(2026, 7, 6);
        EmotionReport report = saveCompletedReport(member, weekStart);
        saveDiaries(member, weekStart, 3);
        NextWeekFlow flow = NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(weekStart)
                .periodStart(weekStart.plusDays(7))
                .periodEnd(weekStart.plusDays(13))
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build();
        flow.complete("보존할 과거 조언", "fake", "v1");
        nextWeekFlowRepository.saveAndFlush(flow);

        nextWeekFlowService.refreshIfEligible(report.getId());

        NextWeekFlow stored = nextWeekFlowRepository.findById(flow.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("보존할 과거 조언", stored.getAdviceText());
        org.junit.jupiter.api.Assertions.assertEquals(1L, nextWeekFlowRepository
                .countByMemberPhoneNumberAndWeekStart(member.getPhoneNumber(), weekStart));
    }

    @Test
    void refreshIfEligibleDoesNotCreateFlowWithOneOrTwoDiaries() {
        Member oneDiaryMember = saveMember("one_diary", "01099990101");
        EmotionReport oneDiaryReport = saveCompletedReport(oneDiaryMember, LocalDate.of(2026, 7, 13));
        saveDiaries(oneDiaryMember, LocalDate.of(2026, 7, 13), 1);

        Member twoDiaryMember = saveMember("two_diary", "01099990102");
        EmotionReport twoDiaryReport = saveCompletedReport(twoDiaryMember, LocalDate.of(2026, 7, 13));
        saveDiaries(twoDiaryMember, LocalDate.of(2026, 7, 13), 2);

        nextWeekFlowService.refreshIfEligible(oneDiaryReport.getId());
        nextWeekFlowService.refreshIfEligible(twoDiaryReport.getId());

        org.junit.jupiter.api.Assertions.assertTrue(nextWeekFlowRepository
                .findByMemberPhoneNumberAndWeekStart(oneDiaryMember.getPhoneNumber(), LocalDate.of(2026, 7, 13))
                .isEmpty());
        org.junit.jupiter.api.Assertions.assertTrue(nextWeekFlowRepository
                .findByMemberPhoneNumberAndWeekStart(twoDiaryMember.getPhoneNumber(), LocalDate.of(2026, 7, 13))
                .isEmpty());
    }

    @Test
    void refreshIfEligibleCreatesFlowWithoutGetWhenThreeDiariesShareSameDate() {
        Member member = saveMember("auto_three_same_day", "01099990103");
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        EmotionReport report = saveCompletedReport(member, weekStart);
        for (int i = 0; i < 3; i++) {
            diaryRepository.save(new Diary(member, "같은 날 일기 " + i, DiaryEmotion.HAPPY, weekStart));
        }
        diaryRepository.flush();

        nextWeekFlowService.refreshIfEligible(report.getId());

        NextWeekFlow flow = nextWeekFlowRepository
                .findByMemberPhoneNumberAndWeekStart(member.getPhoneNumber(), weekStart)
                .orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(report.getId(), flow.getEmotionReport().getId());
        org.junit.jupiter.api.Assertions.assertEquals(NextWeekFlowGenerationStatus.PROCESSING,
                flow.getGenerationStatus());
    }

    @Test
    void refreshIfEligibleCreatesFlowForFallbackCompletedReport() {
        Member member = saveMember("auto_fallback", "01099990104");
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        EmotionReport report = saveReport(member, weekStart, EmotionReportGenerationStatus.FALLBACK_COMPLETED);
        saveDiaries(member, weekStart, 3);

        nextWeekFlowService.refreshIfEligible(report.getId());

        org.junit.jupiter.api.Assertions.assertTrue(nextWeekFlowRepository
                .findByMemberPhoneNumberAndWeekStart(member.getPhoneNumber(), weekStart).isPresent());
    }

    @Test
    void refreshIfEligibleDoesNotCreateFlowForNonTerminalReportStatuses() {
        assertNoFlowForStatus("pending", "01099990105", LocalDate.of(2026, 7, 13),
                EmotionReportGenerationStatus.PENDING);
        assertNoFlowForStatus("processing", "01099990106", LocalDate.of(2026, 7, 13),
                EmotionReportGenerationStatus.PROCESSING);
        assertNoFlowForStatus("failed", "01099990107", LocalDate.of(2026, 7, 13),
                EmotionReportGenerationStatus.FAILED);
    }

    @Test
    void staleNextWeekFlowResultIsIgnoredAfterWeeklySequenceChanges() {
        Member member = saveMember("stale_flow", "01099990108");
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        EmotionReport report = saveCompletedReport(member, weekStart);
        NextWeekFlow flow = nextWeekFlowRepository.saveAndFlush(NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(weekStart)
                .periodStart(weekStart.plusDays(7))
                .periodEnd(weekStart.plusDays(13))
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build());
        int staleSequence = report.getGenerationSequence();
        report.requestGeneration(report.getPeriodEnd(), "weekly-report-summary-v1");
        emotionReportRepository.flush();

        boolean completed = nextWeekFlowGenerationStateService.completeIfCurrent(
                flow.getId(), report.getId(), staleSequence, "오래된 결과");

        org.junit.jupiter.api.Assertions.assertFalse(completed);
        org.junit.jupiter.api.Assertions.assertEquals(NextWeekFlowGenerationStatus.PROCESSING,
                nextWeekFlowRepository.findById(flow.getId()).orElseThrow().getGenerationStatus());
    }

    @Test
    void failFlowOnRejectionMarksProcessingFlowAsFailed() {
        Member member = saveMember("user_rej_1", "01099990092");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 13));
        NextWeekFlow flow = NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(LocalDate.of(2026, 7, 13))
                .periodStart(LocalDate.of(2026, 7, 20))
                .periodEnd(LocalDate.of(2026, 7, 26))
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build();
        nextWeekFlowRepository.saveAndFlush(flow);

        nextWeekFlowAsyncService.failFlowOnRejection(
                flow.getId(), report.getId(), report.getGenerationSequence());

        NextWeekFlow updated = nextWeekFlowRepository.findById(flow.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(NextWeekFlowGenerationStatus.FAILED, updated.getGenerationStatus());
    }

    @Test
    void failFlowOnRejectionDoesNotOverwriteCompletedFlow() {
        Member member = saveMember("user_rej_2", "01099990093");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 13));
        NextWeekFlow flow = NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(LocalDate.of(2026, 7, 13))
                .periodStart(LocalDate.of(2026, 7, 20))
                .periodEnd(LocalDate.of(2026, 7, 26))
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build();
        flow.complete("완료된 조언", "openai", "v1");
        nextWeekFlowRepository.saveAndFlush(flow);

        nextWeekFlowAsyncService.failFlowOnRejection(
                flow.getId(), report.getId(), report.getGenerationSequence());

        NextWeekFlow updated = nextWeekFlowRepository.findById(flow.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(NextWeekFlowGenerationStatus.COMPLETED, updated.getGenerationStatus());
        org.junit.jupiter.api.Assertions.assertEquals("완료된 조언", updated.getAdviceText());
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

    private EmotionReport saveReport(Member member, LocalDate periodStart,
            EmotionReportGenerationStatus status) {
        EmotionReport report = new EmotionReport(
                member,
                EmotionReportType.WEEKLY,
                periodStart,
                periodStart.plusDays(6),
                "weekly-report-summary-v1"
        );
        switch (status) {
            case PENDING -> {
            }
            case PROCESSING -> report.markProcessing();
            case FAILED -> report.fail(1, "TEST_FAILURE");
            case FALLBACK_COMPLETED -> {
                report.markProcessing();
                report.completeWithFallback(
                        new com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult("fallback", "fake"),
                        1,
                        "TEST_FALLBACK"
                );
            }
            default -> throw new IllegalArgumentException("Unsupported test report status: " + status);
        }
        return emotionReportRepository.save(report);
    }

    private void assertNoFlowForStatus(String name, String phone, LocalDate weekStart,
            EmotionReportGenerationStatus status) {
        Member member = saveMember(name, phone);
        EmotionReport report = saveReport(member, weekStart, status);
        saveDiaries(member, weekStart, 3);

        nextWeekFlowService.refreshIfEligible(report.getId());

        org.junit.jupiter.api.Assertions.assertTrue(nextWeekFlowRepository
                .findByMemberPhoneNumberAndWeekStart(phone, weekStart).isEmpty());
    }

    private void assertPostGenerationStartsForAnyWeek(
            String name,
            String phone,
            LocalDate weekStart
    ) throws Exception {
        Member member = saveMember(name, phone);
        saveCompletedReport(member, weekStart);
        saveDiaries(member, weekStart, 3);
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(post("/api/reports/next-week-flow")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"" + weekStart + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.flowId").exists())
                .andExpect(jsonPath("$.data.weekStart").value(weekStart.toString()))
                .andExpect(jsonPath("$.data.generationStatus").value("PROCESSING"));

        NextWeekFlow flow = nextWeekFlowRepository
                .findByMemberPhoneNumberAndWeekStart(phone, weekStart)
                .orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(weekStart.plusDays(7), flow.getPeriodStart());
        org.junit.jupiter.api.Assertions.assertEquals(weekStart.plusDays(13), flow.getPeriodEnd());
    }

    private void saveDiaries(Member member, LocalDate startDate, int count) {
        for (int i = 0; i < count; i++) {
            diaryRepository.save(new Diary(
                    member,
                    "일기 내용 " + (i + 1),
                    DiaryEmotion.HAPPY,
                    startDate.plusDays(i)
            ));
        }
        diaryRepository.flush();
    }
}
