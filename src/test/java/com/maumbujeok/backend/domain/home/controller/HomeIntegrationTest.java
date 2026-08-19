package com.maumbujeok.backend.domain.home.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.maumbujeok.backend.domain.burn.application.BurningService;
import com.maumbujeok.backend.domain.burn.domain.BurningSourceType;
import com.maumbujeok.backend.domain.burn.dto.CreateBurningRequest;
import com.maumbujeok.backend.domain.diary.application.DiaryService;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryRequest;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.home.repository.HomeSummaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.report.application.NextWeekFlowTestClockConfig;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.maumbujeok.backend.domain.auth.repository.RefreshTokenRepository;
import com.maumbujeok.backend.domain.auth.repository.SmsAuthCodeRepository;
import com.maumbujeok.backend.domain.burn.repository.BurningAnalysisRepository;
import com.maumbujeok.backend.domain.burn.repository.BurningRepository;
import com.maumbujeok.backend.domain.diary.repository.DiaryAnalysisRepository;
import com.maumbujeok.backend.domain.member.repository.MemberNotificationSettingRepository;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.domain.report.repository.NextWeekFlowRepository;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import com.maumbujeok.backend.domain.talisman.repository.TalismanRepository;
import com.maumbujeok.backend.domain.upload.repository.DiaryUploadRepository;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.Executor;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({NextWeekFlowTestClockConfig.class, HomeIntegrationTest.SyncExecutorConfig.class})
class HomeIntegrationTest {

    @TestConfiguration(proxyBeanMethods = false)
    static class SyncExecutorConfig {
        @Bean(name = "diaryAnalysisExecutor")
        Executor diaryAnalysisExecutor() {
            return new SyncTaskExecutor();
        }

        @Bean(name = "weeklyReportExecutor")
        Executor weeklyReportExecutor() {
            return new SyncTaskExecutor();
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberSajuProfileRepository memberSajuProfileRepository;
    @Autowired DiaryRepository diaryRepository;
    @Autowired DiaryAnalysisRepository diaryAnalysisRepository;
    @Autowired DiaryUploadRepository diaryUploadRepository;
    @Autowired BurningRepository burningRepository;
    @Autowired BurningAnalysisRepository burningAnalysisRepository;
    @Autowired TalismanRepository talismanRepository;
    @Autowired EmotionReportRepository emotionReportRepository;
    @Autowired NextWeekFlowRepository nextWeekFlowRepository;
    @Autowired SajuAnalysisRepository sajuAnalysisRepository;
    @Autowired MemberNotificationSettingRepository notificationSettingRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired SmsAuthCodeRepository smsAuthCodeRepository;
    @Autowired HomeSummaryRepository homeSummaryRepository;
    @Autowired DiaryService diaryService;
    @Autowired BurningService burningService;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired Clock serviceClock;

    private Member member;
    private String token;
    private LocalDate today;

    @BeforeEach
    void setUp() {
        cleanup();

        today = LocalDate.now(serviceClock); // 2026-07-15 from NextWeekFlowTestClockConfig
        String phone = "01099998888";
        member = memberRepository.save(Member.builder()
                .phoneNumber(phone)
                .name("테스트유저")
                .birthDate("19950515")
                .role(Member.Role.ROLE_USER)
                .provider(Member.Provider.LOCAL)
                .providerId("local-provider-id")
                .build());

        memberSajuProfileRepository.save(MemberSajuProfile.builder()
                .member(member)
                .gender(MemberSajuProfile.Gender.MALE)
                .calendarType(MemberSajuProfile.CalendarType.SOLAR)
                .birthTime(LocalTime.of(14, 30))
                .build());

        token = jwtTokenProvider.createToken(phone, member.getRole().name());
    }

    @AfterEach
    void tearDown() {
        cleanup();
    }

    private void cleanup() {
        sajuAnalysisRepository.deleteAllInBatch();
        nextWeekFlowRepository.deleteAllInBatch();
        emotionReportRepository.deleteAllInBatch();
        talismanRepository.deleteAllInBatch();
        burningAnalysisRepository.deleteAllInBatch();
        burningRepository.deleteAllInBatch();
        diaryUploadRepository.deleteAllInBatch();
        diaryAnalysisRepository.deleteAllInBatch();
        diaryRepository.deleteAllInBatch();
        homeSummaryRepository.deleteAllInBatch();
        notificationSettingRepository.deleteAllInBatch();
        memberSajuProfileRepository.deleteAllInBatch();
        refreshTokenRepository.deleteAllInBatch();
        smsAuthCodeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("일기 0개일 때 홈 통합 조회 시 기본 사주 HomeSummary 생성 및 isDiaryWrittenToday는 false")
    void getHomeIntegratedData_initialState() throws Exception {
        mockMvc.perform(get("/api/home")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberName").value("테스트유저"))
                .andExpect(jsonPath("$.data.isDiaryWrittenToday").value(false))
                .andExpect(jsonPath("$.data.todaySummary.primaryElement").exists())
                .andExpect(jsonPath("$.data.todaySummary.todayLuck").isNotEmpty())
                .andExpect(jsonPath("$.data.todaySummary.todayEnergy").isNotEmpty());

        assertTrue(homeSummaryRepository.existsByMemberPhoneNumberAndSummaryDate(member.getPhoneNumber(), today));
    }

    @Test
    @DisplayName("오늘 일기 작성 시 HomeSummary가 실시간 갱신되고 isDiaryWrittenToday가 true로 변경됨")
    void createTodayDiary_refreshesHomeSummary() throws Exception {
        // Initial state
        mockMvc.perform(get("/api/home").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDiaryWrittenToday").value(false));

        // Create today's diary
        CreateDiaryResponse diaryResponse = diaryService.create(
                member.getPhoneNumber(),
                new CreateDiaryRequest("오늘 마음이 너무 슬프고 힘들었습니다.", "SAD", today, List.of())
        );
        assertNotNull(diaryResponse.diaryId());

        // Check home integrated data
        mockMvc.perform(get("/api/home").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDiaryWrittenToday").value(true))
                .andExpect(jsonPath("$.data.todaySummary.todayEnergy").value(
                        org.hamcrest.Matchers.containsString("SAD")
                ));
    }

    @Test
    @DisplayName("오늘 일기 수정 시 HomeSummary가 수정한 감정으로 실시간 재갱신됨")
    void updateTodayDiary_refreshesHomeSummary() throws Exception {
        CreateDiaryResponse diaryResponse = diaryService.create(
                member.getPhoneNumber(),
                new CreateDiaryRequest("원래는 슬펐습니다.", "SAD", today, List.of())
        );

        // Update diary to HAPPY
        diaryService.update(
                member.getPhoneNumber(),
                diaryResponse.diaryId(),
                new UpdateDiaryRequest("이제는 정말 기쁘고 행복합니다!", "HAPPY", List.of())
        );

        // Check home summary reflects HAPPY
        mockMvc.perform(get("/api/home").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDiaryWrittenToday").value(true))
                .andExpect(jsonPath("$.data.todaySummary.todayEnergy").value(
                        org.hamcrest.Matchers.containsString("HAPPY")
                ));
    }

    @Test
    @DisplayName("오늘 일기 소각 시 HomeSummary가 기본 사주 요약으로 복구되고 isDiaryWrittenToday가 false로 변경됨")
    void burnTodayDiary_revertsHomeSummaryAndIsDiaryWrittenFalse() throws Exception {
        CreateDiaryResponse diaryResponse = diaryService.create(
                member.getPhoneNumber(),
                new CreateDiaryRequest("태워버릴 슬픈 일기입니다.", "SAD", today, List.of())
        );

        // Verify it was written
        mockMvc.perform(get("/api/home").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDiaryWrittenToday").value(true));

        // Burn the diary
        burningService.create(
                member.getPhoneNumber(),
                new CreateBurningRequest(BurningSourceType.DIARY, null, diaryResponse.diaryId())
        );

        // Check home integrated data - isDiaryWrittenToday is false and SAD emotion is cleared
        mockMvc.perform(get("/api/home").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDiaryWrittenToday").value(false))
                .andExpect(jsonPath("$.data.todaySummary.todayEnergy").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("SAD"))
                ));
    }

    @Test
    @DisplayName("과거 날짜 일기 작성 시 오늘 HomeSummary와 isDiaryWrittenToday에 영향 없음")
    void createPastDiary_doesNotAffectTodayHome() throws Exception {
        LocalDate pastDate = today.minusDays(3);

        diaryService.create(
                member.getPhoneNumber(),
                new CreateDiaryRequest("과거에 작성한 일기입니다.", "ANXIOUS", pastDate, List.of())
        );

        mockMvc.perform(get("/api/home").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isDiaryWrittenToday").value(false))
                .andExpect(jsonPath("$.data.todaySummary.todayEnergy").value(
                        org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("ANXIOUS"))
                ));
    }
}
