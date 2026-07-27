package com.maumbujeok.backend.domain.report.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.maumbujeok.backend.domain.diary.application.DiaryService;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportRequest;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportResponse;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
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
    @Autowired WeeklyReportService weeklyReportService;
    @Autowired DiaryService diaryService;
    @Autowired MemberRepository memberRepository;
    @Autowired EmotionReportRepository emotionReportRepository;

    @Test
    void reusesExistingWeeklyReportIdWhenRegeneratedForSameWeek() {
        Member member = memberRepository.save(Member.builder()
                .name("weekly-user")
                .phoneNumber("01010000003")
                .passwordHash("encoded-password")
                .role(Member.Role.ROLE_USER)
                .build());
        diaryService.create(member.getPhoneNumber(), new CreateDiaryRequest("한 주를 정리하는 기분으로 기록했다", "COMFORTABLE"));
        LocalDate weekStart = currentWeekStart();

        GenerateWeeklyReportResponse first = weeklyReportService.generate(
                member.getPhoneNumber(),
                new GenerateWeeklyReportRequest(weekStart)
        );
        GenerateWeeklyReportResponse second = weeklyReportService.generate(
                member.getPhoneNumber(),
                new GenerateWeeklyReportRequest(weekStart)
        );

        assertEquals(first.emotionReportId(), second.emotionReportId());
        assertEquals(1, emotionReportRepository.count());
    }

    private LocalDate currentWeekStart() {
        return LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
}
