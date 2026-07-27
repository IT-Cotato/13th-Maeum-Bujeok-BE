package com.maumbujeok.backend.domain.report.controller;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maumbujeok.backend.domain.diary.application.DiaryService;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportPromptVersion;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
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
class ReportControllerSecurityTest {
    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired DiaryService diaryService;
    @Autowired EmotionReportRepository emotionReportRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @Test
    void rejectsUnauthenticatedWeeklyReportGeneration() throws Exception {
        mockMvc.perform(post("/api/reports/weekly/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"2026-07-13\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsUnauthenticatedWeeklySummaryQuery() throws Exception {
        mockMvc.perform(get("/api/reports/weekly-summary/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedMemberStartsWeeklyReportGeneration() throws Exception {
        Member member = saveMember("report-owner", "01010000001");
        diaryService.create(member.getPhoneNumber(), new CreateDiaryRequest("오늘은 기분이 한결 가벼웠다", "HAPPY"));
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());
        LocalDate weekStart = currentWeekStart();

        mockMvc.perform(post("/api/reports/weekly/generate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"" + weekStart + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.emotionReportId", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.data.reportType").value("WEEKLY"))
                .andExpect(jsonPath("$.data.periodStart").value(weekStart.toString()))
                .andExpect(jsonPath("$.data.periodEnd").value(weekStart.plusDays(6).toString()))
                .andExpect(jsonPath("$.data.generationStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.message").value("주간 감정 리포트 생성을 시작했습니다."));
    }

    @Test
    void rejectsWeeklyReportGenerationWithoutDiarySource() throws Exception {
        Member member = saveMember("report-empty", "01010000002");
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());
        LocalDate weekStart = currentWeekStart();

        mockMvc.perform(post("/api/reports/weekly/generate")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weekStart\":\"" + weekStart + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REPORT_400"))
                .andExpect(jsonPath("$.message").value("리포트를 생성할 일기 기록이 없습니다."));
    }

    @Test
    void returnsCompletedWeeklySummaryForOwner() throws Exception {
        Member member = saveMember("summary-owner", "01010000003");
        EmotionReport report = saveCompletedReport(member, LocalDate.of(2026, 7, 13));
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(get("/api/reports/weekly-summary/{summaryId}", report.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.summaryId").value(report.getId()))
                .andExpect(jsonPath("$.data.emotionReportId").value(report.getId()))
                .andExpect(jsonPath("$.data.periodStart").value("2026-07-13"))
                .andExpect(jsonPath("$.data.periodEnd").value("2026-07-19"))
                .andExpect(jsonPath("$.data.generationStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.insightSummary").value("이번 주는 마음님에게 꽤 괜찮은 한 주였어요!"))
                .andExpect(jsonPath("$.data.modelName").value("gpt-4o-mini"))
                .andExpect(jsonPath("$.data.reportVersion").value("v1.0"))
                .andExpect(jsonPath("$.data.generatedAt").isNotEmpty());
    }

    @Test
    void returnsPendingWeeklySummaryWithNullableResultFields() throws Exception {
        Member member = saveMember("summary-pending", "01010000004");
        EmotionReport report = emotionReportRepository.save(new EmotionReport(
                member,
                EmotionReportType.WEEKLY,
                LocalDate.of(2026, 7, 13),
                LocalDate.of(2026, 7, 19),
                WeeklyReportPromptVersion.VALUE
        ));
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(get("/api/reports/weekly-summary/{summaryId}", report.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generationStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.insightSummary").value(nullValue()))
                .andExpect(jsonPath("$.data.modelName").value(nullValue()))
                .andExpect(jsonPath("$.data.reportVersion").value("v1.0"))
                .andExpect(jsonPath("$.data.generatedAt").value(nullValue()));
    }

    @Test
    void returnsFailedWeeklySummaryState() throws Exception {
        Member member = saveMember("summary-failed", "01010000005");
        EmotionReport report = new EmotionReport(
                member,
                EmotionReportType.WEEKLY,
                LocalDate.of(2026, 7, 13),
                LocalDate.of(2026, 7, 19),
                WeeklyReportPromptVersion.VALUE
        );
        report.markProcessing();
        report.fail(2, "AI_TIMEOUT");
        emotionReportRepository.save(report);
        String token = jwtTokenProvider.createToken(member.getPhoneNumber(), member.getRole().name());

        mockMvc.perform(get("/api/reports/weekly-summary/{summaryId}", report.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.generationStatus").value("FAILED"))
                .andExpect(jsonPath("$.data.insightSummary").value(nullValue()))
                .andExpect(jsonPath("$.data.modelName").value(nullValue()))
                .andExpect(jsonPath("$.data.reportVersion").value("v1.0"))
                .andExpect(jsonPath("$.data.generatedAt").isNotEmpty());
    }

    @Test
    void hidesAnotherMembersWeeklySummary() throws Exception {
        Member owner = saveMember("summary-owner2", "01010000006");
        Member stranger = saveMember("summary-stranger", "01010000007");
        EmotionReport report = saveCompletedReport(owner, LocalDate.of(2026, 7, 13));
        String strangerToken = jwtTokenProvider.createToken(stranger.getPhoneNumber(), stranger.getRole().name());

        mockMvc.perform(get("/api/reports/weekly-summary/{summaryId}", report.getId())
                        .header("Authorization", "Bearer " + strangerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("REPORT_404"))
                .andExpect(jsonPath("$.message").value("주간 리포트 요약을 찾을 수 없습니다."));
    }

    private LocalDate currentWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private Member saveMember(String name, String phoneNumber) {
        return memberRepository.save(Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .passwordHash("encoded-password")
                .role(Member.Role.ROLE_USER)
                .build());
    }

    private EmotionReport saveCompletedReport(Member member, LocalDate periodStart) {
        EmotionReport report = new EmotionReport(
                member,
                EmotionReportType.WEEKLY,
                periodStart,
                periodStart.plusDays(6),
                WeeklyReportPromptVersion.VALUE
        );
        report.markProcessing();
        report.complete(new WeeklyReportAiResult("이번 주는 마음님에게 꽤 괜찮은 한 주였어요!", "gpt-4o-mini"), 1);
        return emotionReportRepository.save(report);
    }
}
