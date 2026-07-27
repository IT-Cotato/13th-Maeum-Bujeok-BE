package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportPromptVersion;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportRequest;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportResponse;
import com.maumbujeok.backend.domain.report.dto.WeeklyReportSummaryResponse;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportService {
    private static final String START_MESSAGE = "주간 감정 리포트 생성을 시작했습니다.";

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final EmotionReportRepository emotionReportRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public GenerateWeeklyReportResponse generate(String memberPhoneNumber, GenerateWeeklyReportRequest request) {
        LocalDate periodStart = normalizeWeekStart(request);
        LocalDate periodEnd = periodStart.plusDays(6);
        requireDiarySource(memberPhoneNumber, periodStart, periodEnd);

        EmotionReport report = emotionReportRepository.findByMemberPhoneNumberAndReportTypeAndPeriodStart(
                memberPhoneNumber,
                EmotionReportType.WEEKLY,
                periodStart
        ).orElse(null);

        if (report == null) {
            report = createReport(memberPhoneNumber, periodStart, periodEnd);
        } else {
            report.requestGeneration(periodEnd, WeeklyReportPromptVersion.VALUE);
        }

        log.info("Weekly report generation requested reportId={} generationSequence={} periodStart={} periodEnd={} memberPhoneSuffix={}",
                report.getId(), report.getGenerationSequence(), report.getPeriodStart(), report.getPeriodEnd(),
                maskPhoneNumber(memberPhoneNumber));
        eventPublisher.publishEvent(new WeeklyReportGenerationRequestedEvent(report.getId(), report.getGenerationSequence()));
        log.info("Weekly report generation event published reportId={} generationSequence={}",
                report.getId(), report.getGenerationSequence());

        return GenerateWeeklyReportResponse.from(report, START_MESSAGE);
    }

    @Transactional(readOnly = true)
    public WeeklyReportSummaryResponse getWeeklySummary(String memberPhoneNumber, Long summaryId) {
        EmotionReport report = emotionReportRepository.findByIdAndMemberPhoneNumberAndReportType(
                        summaryId,
                        memberPhoneNumber,
                        EmotionReportType.WEEKLY
                )
                .orElseThrow(() -> new ReportRequestException(ErrorCode.REPORT_NOT_FOUND, "주간 리포트 요약을 찾을 수 없습니다."));

        log.info("Weekly report summary queried summaryId={} generationStatus={} model={} failureCode={}",
                summaryId, report.getGenerationStatus(), report.getModelName(), report.getFailureCode());

        return WeeklyReportSummaryResponse.from(report);
    }

    private EmotionReport createReport(String memberPhoneNumber, LocalDate periodStart, LocalDate periodEnd) {
        Member member = memberRepository.getReferenceById(memberPhoneNumber);
        return emotionReportRepository.save(new EmotionReport(
                member,
                EmotionReportType.WEEKLY,
                periodStart,
                periodEnd,
                WeeklyReportPromptVersion.VALUE
        ));
    }

    private void requireDiarySource(String memberPhoneNumber, LocalDate periodStart, LocalDate periodEnd) {
        LocalDateTime startedAt = periodStart.atStartOfDay();
        LocalDateTime endedAtExclusive = periodEnd.plusDays(1).atStartOfDay();
        boolean exists = diaryRepository.existsByMemberPhoneNumberAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                memberPhoneNumber,
                startedAt,
                endedAtExclusive
        );
        if (!exists) {
            throw new ReportRequestException(ErrorCode.INVALID_REPORT_REQUEST, "리포트를 생성할 일기 기록이 없습니다.");
        }
    }

    private LocalDate normalizeWeekStart(GenerateWeeklyReportRequest request) {
        if (request == null || request.weekStart() == null) {
            throw new ReportRequestException(ErrorCode.INVALID_REPORT_REQUEST, "weekStart는 필수입니다.");
        }
        LocalDate weekStart = request.weekStart();
        if (weekStart.getDayOfWeek() == DayOfWeek.MONDAY) {
            return weekStart;
        }
        return weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(phoneNumber.length() - 4);
    }
}
