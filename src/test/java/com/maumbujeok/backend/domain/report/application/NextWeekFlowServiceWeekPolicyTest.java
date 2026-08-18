package com.maumbujeok.backend.domain.report.application;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.domain.report.repository.NextWeekFlowRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

class NextWeekFlowServiceWeekPolicyTest {
    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Test
    void sundayStillBelongsToThePreviousMondayWeekInSeoul() {
        NextWeekFlowService service = serviceAt("2026-08-23T14:59:59Z");

        assertTrue(service.isCurrentWeek(LocalDate.of(2026, 8, 17)));
        assertFalse(service.isCurrentWeek(LocalDate.of(2026, 8, 24)));
    }

    @Test
    void mondayMidnightStartsANewWeekInSeoul() {
        NextWeekFlowService service = serviceAt("2026-08-23T15:00:00Z");

        assertTrue(service.isCurrentWeek(LocalDate.of(2026, 8, 24)));
        assertFalse(service.isCurrentWeek(LocalDate.of(2026, 8, 17)));
    }

    private NextWeekFlowService serviceAt(String instant) {
        return new NextWeekFlowService(
                mock(MemberRepository.class),
                mock(DiaryRepository.class),
                mock(EmotionReportRepository.class),
                mock(NextWeekFlowRepository.class),
                mock(ApplicationEventPublisher.class),
                Clock.fixed(Instant.parse(instant), SEOUL)
        );
    }
}
