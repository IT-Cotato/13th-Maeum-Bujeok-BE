package com.maumbujeok.backend.domain.report.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryEmotion;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlow;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportRequest;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportResponse;
import com.maumbujeok.backend.domain.report.dto.NextWeekFlowQueryResponse;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.domain.report.repository.NextWeekFlowRepository;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

@SpringBootTest
@ActiveProfiles("test")
@Import(NextWeekFlowTestClockConfig.class)
class NextWeekFlowConcurrencyTest {
    private static final String[] FIXTURE_PHONE_NUMBERS = {
            "01099990201", "01099990202", "01099990203", "01099990204"
    };

    @Autowired MemberRepository memberRepository;
    @Autowired DiaryRepository diaryRepository;
    @Autowired EmotionReportRepository emotionReportRepository;
    @Autowired NextWeekFlowRepository nextWeekFlowRepository;
    @Autowired NextWeekFlowService nextWeekFlowService;
    @Autowired WeeklyReportService weeklyReportService;
    @Autowired JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUpCommittedConcurrencyFixtures() {
        for (String phoneNumber : FIXTURE_PHONE_NUMBERS) {
            jdbcTemplate.update("delete from next_week_flows where member_phone_number = ?", phoneNumber);
            jdbcTemplate.update("delete from emotion_reports where member_phone_number = ?", phoneNumber);
            jdbcTemplate.update("delete from diary_analysis where diary_id in "
                    + "(select id from diaries where member_phone_number = ?)", phoneNumber);
            jdbcTemplate.update("delete from diaries where member_phone_number = ?", phoneNumber);
            jdbcTemplate.update("delete from members where phone_number = ?", phoneNumber);
        }
    }

    @Test
    void concurrentOnDemandRequestsReturnOneFlowForTheSameReport() throws Exception {
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        Member member = memberRepository.save(Member.builder()
                .name("concurrent-flow-user")
                .phoneNumber("01099990201")
                .passwordHash("password")
                .role(Member.Role.ROLE_USER)
                .build());
        for (int i = 0; i < 3; i++) {
            diaryRepository.save(new Diary(member, "동시 요청 일기 " + i, DiaryEmotion.HAPPY, weekStart));
        }
        EmotionReport report = new EmotionReport(
                member,
                EmotionReportType.WEEKLY,
                weekStart,
                weekStart.plusDays(6),
                "weekly-report-summary-v1"
        );
        report.markProcessing();
        report.complete(new WeeklyReportAiResult("완료된 주간 요약", "fake"), 1);
        report = emotionReportRepository.save(report);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        EmotionReport detachedReport = report;
        try {
            CompletableFuture<NextWeekFlowQueryResponse> first = CompletableFuture.supplyAsync(
                    () -> requestAfterBarrier(member.getPhoneNumber(), detachedReport, ready, start), executor);
            CompletableFuture<NextWeekFlowQueryResponse> second = CompletableFuture.supplyAsync(
                    () -> requestAfterBarrier(member.getPhoneNumber(), detachedReport, ready, start), executor);

            org.junit.jupiter.api.Assertions.assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            NextWeekFlowQueryResponse firstResponse = first.get(10, TimeUnit.SECONDS);
            NextWeekFlowQueryResponse secondResponse = second.get(10, TimeUnit.SECONDS);

            assertEquals(firstResponse.flowId(), secondResponse.flowId());
            long flowCount = nextWeekFlowRepository
                    .countByMemberPhoneNumberAndWeekStart(member.getPhoneNumber(), weekStart);
            assertEquals(1L, flowCount);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void automaticRefreshAndOnDemandRequestKeepTheSameCurrentFlow() throws Exception {
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        Member member = memberRepository.save(Member.builder()
                .name("concurrent-auto-flow-user")
                .phoneNumber("01099990202")
                .passwordHash("password")
                .role(Member.Role.ROLE_USER)
                .build());
        for (int i = 0; i < 3; i++) {
            diaryRepository.save(new Diary(member, "자동 생성 동시 요청 일기 " + i, DiaryEmotion.HAPPY, weekStart));
        }
        EmotionReport report = new EmotionReport(
                member,
                EmotionReportType.WEEKLY,
                weekStart,
                weekStart.plusDays(6),
                "weekly-report-summary-v1"
        );
        report.markProcessing();
        report.complete(new WeeklyReportAiResult("완료된 주간 요약", "fake"), 1);
        report = emotionReportRepository.save(report);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        EmotionReport detachedReport = report;
        try {
            CompletableFuture<Void> automaticRefresh = CompletableFuture.runAsync(() -> {
                awaitBarrier(ready, start);
                nextWeekFlowService.refreshIfEligible(detachedReport.getId());
            }, executor);
            CompletableFuture<NextWeekFlowQueryResponse> onDemand = CompletableFuture.supplyAsync(() ->
                    requestAfterBarrier(member.getPhoneNumber(), detachedReport, ready, start), executor);

            org.junit.jupiter.api.Assertions.assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            automaticRefresh.get(10, TimeUnit.SECONDS);
            NextWeekFlowQueryResponse response = onDemand.get(10, TimeUnit.SECONDS);
            Long storedFlowId = nextWeekFlowRepository
                    .findByMemberPhoneNumberAndWeekStart(member.getPhoneNumber(), weekStart)
                    .orElseThrow()
                    .getId();

            assertEquals(response.flowId(), storedFlowId);
            assertEquals(1L, nextWeekFlowRepository
                    .countByMemberPhoneNumberAndWeekStart(member.getPhoneNumber(), weekStart));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void automaticRefreshAndOnDemandRequestReplaceOneStaleFlowUnderTheSameReportLock() throws Exception {
        LocalDate weekStart = LocalDate.of(2026, 7, 13);
        Member member = memberRepository.save(Member.builder()
                .name("concurrent-stale-flow-user")
                .phoneNumber("01099990203")
                .passwordHash("password")
                .role(Member.Role.ROLE_USER)
                .build());
        for (int i = 0; i < 3; i++) {
            diaryRepository.save(new Diary(member, "stale flow 교체 일기 " + i, DiaryEmotion.HAPPY, weekStart));
        }
        EmotionReport report = new EmotionReport(
                member,
                EmotionReportType.WEEKLY,
                weekStart,
                weekStart.plusDays(6),
                "weekly-report-summary-v1"
        );
        report.markProcessing();
        report.complete(new WeeklyReportAiResult("이전 주간 요약", "fake"), 1);
        report = emotionReportRepository.saveAndFlush(report);
        NextWeekFlow staleFlow = nextWeekFlowRepository.saveAndFlush(NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(weekStart)
                .periodStart(weekStart.plusDays(7))
                .periodEnd(weekStart.plusDays(13))
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build());
        staleFlow.complete("이전 generation 조언", "fake", "v1");
        nextWeekFlowRepository.saveAndFlush(staleFlow);

        report.requestGeneration(weekStart.plusDays(6), "weekly-report-summary-v1");
        report.markProcessing();
        report.complete(new WeeklyReportAiResult("최신 주간 요약", "fake"), 1);
        report = emotionReportRepository.saveAndFlush(report);
        int latestSequence = report.getGenerationSequence();
        Long staleFlowId = staleFlow.getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        EmotionReport detachedReport = report;
        try {
            CompletableFuture<Void> automaticRefresh = CompletableFuture.runAsync(() -> {
                awaitBarrier(ready, start);
                nextWeekFlowService.refreshIfEligible(detachedReport.getId());
            }, executor);
            CompletableFuture<NextWeekFlowQueryResponse> onDemand = CompletableFuture.supplyAsync(() ->
                    requestAfterBarrier(member.getPhoneNumber(), detachedReport, ready, start), executor);

            org.junit.jupiter.api.Assertions.assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            automaticRefresh.get(10, TimeUnit.SECONDS);
            NextWeekFlowQueryResponse response = onDemand.get(10, TimeUnit.SECONDS);
            NextWeekFlow storedFlow = nextWeekFlowRepository
                    .findByMemberPhoneNumberAndWeekStart(member.getPhoneNumber(), weekStart)
                    .orElseThrow();

            org.junit.jupiter.api.Assertions.assertNotEquals(staleFlowId, response.flowId());
            assertEquals(response.flowId(), storedFlow.getId());
            assertEquals(detachedReport.getId(), response.emotionReportId());
            assertEquals(latestSequence, emotionReportRepository.findById(response.emotionReportId())
                    .orElseThrow()
                    .getGenerationSequence());
            assertEquals(1L, nextWeekFlowRepository
                    .countByMemberPhoneNumberAndWeekStart(member.getPhoneNumber(), weekStart));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void concurrentInitialGenerateAndRefreshCreateOneWeeklyReport() throws Exception {
        LocalDate weekStart = LocalDate.of(2026, 5, 25);
        Member member = memberRepository.save(Member.builder()
                .name("concurrent-weekly-create-user")
                .phoneNumber("01099990204")
                .passwordHash("password")
                .role(Member.Role.ROLE_USER)
                .build());

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            CompletableFuture<GenerateWeeklyReportResponse> generate = CompletableFuture.supplyAsync(() -> {
                awaitBarrier(ready, start);
                return weeklyReportService.generate(
                        member.getPhoneNumber(), new GenerateWeeklyReportRequest(weekStart));
            }, executor);
            CompletableFuture<Void> refresh = CompletableFuture.runAsync(() -> {
                awaitBarrier(ready, start);
                weeklyReportService.refreshForDiaryEntry(member.getPhoneNumber(), weekStart);
            }, executor);

            org.junit.jupiter.api.Assertions.assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();

            GenerateWeeklyReportResponse response = generate.get(10, TimeUnit.SECONDS);
            refresh.get(10, TimeUnit.SECONDS);
            EmotionReport storedReport = emotionReportRepository
                    .findByMemberPhoneNumberAndReportTypeAndPeriodStart(
                            member.getPhoneNumber(), EmotionReportType.WEEKLY, weekStart)
                    .orElseThrow();

            assertEquals(response.emotionReportId(), storedReport.getId());
            assertEquals(2, storedReport.getGenerationSequence());
            assertEquals(1L, emotionReportRepository
                    .countByMemberPhoneNumberAndReportTypeAndPeriodStart(
                            member.getPhoneNumber(), EmotionReportType.WEEKLY, weekStart));
        } finally {
            executor.shutdownNow();
        }
    }

    private NextWeekFlowQueryResponse requestAfterBarrier(
            String phoneNumber,
            EmotionReport report,
            CountDownLatch ready,
            CountDownLatch start
    ) {
        awaitBarrier(ready, start);
        return nextWeekFlowService.getOrGenerateFlowForWeeklyReport(phoneNumber, report);
    }

    private void awaitBarrier(CountDownLatch ready, CountDownLatch start) {
        ready.countDown();
        try {
            if (!start.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Concurrent request barrier timed out");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Concurrent request interrupted", exception);
        }
    }
}
