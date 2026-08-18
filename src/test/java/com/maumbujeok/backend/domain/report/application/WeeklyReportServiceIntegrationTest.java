package com.maumbujeok.backend.domain.report.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maumbujeok.backend.domain.diary.application.DiaryService;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportRequest;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportResponse;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WeeklyReportServiceIntegrationTest {
    private static final ZoneId SERVICE_ZONE = ZoneId.of("Asia/Seoul");

    @Autowired WeeklyReportService weeklyReportService;
    @Autowired DiaryService diaryService;
    @Autowired MemberRepository memberRepository;
    @Autowired EmotionReportRepository emotionReportRepository;

    @Test
    void keepsReportIdAndIncrementsSequenceWhenRegeneratedForSameWeek() {
        Member member = memberRepository.save(Member.builder()
                .name("weekly-user")
                .phoneNumber("01010000003")
                .passwordHash("encoded-password")
                .role(Member.Role.ROLE_USER)
                .build());
        LocalDate weekStart = currentWeekStart();
        for (int i = 1; i <= 3; i++) {
            diaryService.create(member.getPhoneNumber(), new CreateDiaryRequest(
                    "한 주를 정리하는 기분으로 기록했다 " + i,
                    "COMFORTABLE",
                    weekStart
            ));
        }

        GenerateWeeklyReportResponse first = weeklyReportService.generate(
                member.getPhoneNumber(),
                new GenerateWeeklyReportRequest(weekStart)
        );
        diaryService.create(member.getPhoneNumber(), new CreateDiaryRequest(
                "네 번째 기록을 추가했다",
                "COMFORTABLE",
                weekStart.plusDays(1)
        ));
        GenerateWeeklyReportResponse second = weeklyReportService.generate(
                member.getPhoneNumber(),
                new GenerateWeeklyReportRequest(weekStart)
        );

        assertEquals(first.emotionReportId(), second.emotionReportId());
        assertTrue(emotionReportRepository.findById(second.emotionReportId()).isPresent());
        assertEquals(2, emotionReportRepository.findById(second.emotionReportId()).orElseThrow()
                .getGenerationSequence());
        assertEquals(1, emotionReportRepository
                .countByMemberPhoneNumberAndReportTypeAndPeriodStart(
                        member.getPhoneNumber(), EmotionReportType.WEEKLY, weekStart));
    }

    private LocalDate currentWeekStart() {
        return LocalDate.now(SERVICE_ZONE).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
