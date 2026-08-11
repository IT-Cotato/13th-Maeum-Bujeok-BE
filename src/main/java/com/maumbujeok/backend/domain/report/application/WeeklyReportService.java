package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportPromptVersion;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportRequest;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportResponse;
import com.maumbujeok.backend.domain.report.dto.WeeklyReportSummaryResponse;
import com.maumbujeok.backend.domain.report.dto.WeeklyReportPeriodResponse;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyReportService {
    private static final String START_MESSAGE = "주간 감정 리포트 생성을 시작했습니다.";
    private static final String EMPTY_SUMMARY_MESSAGE = "작성된 일기가 없어 안내 문구로 주간 감정 리포트를 생성했습니다.";

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final EmotionReportRepository emotionReportRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public GenerateWeeklyReportResponse generate(String memberPhoneNumber, GenerateWeeklyReportRequest request) {
        LocalDate periodStart = normalizeWeekStart(request);
        return generateForWeek(memberPhoneNumber, periodStart);
    }

    @Transactional
    public void refreshForDiaryEntry(String memberPhoneNumber, LocalDate diaryDate) {
        if (diaryDate == null) {
            log.warn("Weekly report refresh skipped memberPhoneSuffix={} reason=missing_diary_date",
                    maskPhoneNumber(memberPhoneNumber));
            return;
        }

        GenerateWeeklyReportResponse response = generateForWeek(memberPhoneNumber, normalizeWeekStart(diaryDate));
        log.info("Weekly report auto refresh requested emotionReportId={} generationStatus={} memberPhoneSuffix={}",
                response.emotionReportId(), response.generationStatus(), maskPhoneNumber(memberPhoneNumber));
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

    @Transactional(readOnly = true)
    public WeeklyReportSummaryResponse getWeeklyByStartDate(String memberPhoneNumber, LocalDate startDate) {
        LocalDate periodStart = normalizeWeekStart(startDate);
        EmotionReport report = emotionReportRepository.findByMemberPhoneNumberAndReportTypeAndPeriodStart(
                        memberPhoneNumber, EmotionReportType.WEEKLY, periodStart)
                .orElseThrow(() -> new ReportRequestException(ErrorCode.REPORT_NOT_FOUND, "주간 리포트를 찾을 수 없습니다."));
        return WeeklyReportSummaryResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<WeeklyReportPeriodResponse> getWeeklyPeriods(String memberPhoneNumber) {
        return emotionReportRepository.findAllByMemberPhoneNumberAndReportTypeOrderByPeriodStartDesc(
                        memberPhoneNumber, EmotionReportType.WEEKLY)
                .stream()
                .map(WeeklyReportPeriodResponse::from)
                .toList();
    }

    @Transactional
    public GenerateWeeklyReportResponse regenerate(String memberPhoneNumber, Long reportId) {
        EmotionReport report = emotionReportRepository.findByIdAndMemberPhoneNumberAndReportType(
                        reportId, memberPhoneNumber, EmotionReportType.WEEKLY)
                .orElseThrow(() -> new ReportRequestException(ErrorCode.REPORT_NOT_FOUND, "주간 리포트를 찾을 수 없습니다."));
        return generateForWeek(memberPhoneNumber, report.getPeriodStart());
    }
    private GenerateWeeklyReportResponse generateForWeek(String memberPhoneNumber, LocalDate periodStart) {
        LocalDate periodEnd = periodStart.plusDays(6);
        EmotionReport report = prepareReport(memberPhoneNumber, periodStart, periodEnd);

        if (!hasDiarySource(memberPhoneNumber, periodStart, periodEnd)) {
            completeWithEmptySummary(report);
            log.info("Weekly report completed with empty summary reportId={} generationSequence={} periodStart={} periodEnd={} memberPhoneSuffix={}",
                    report.getId(), report.getGenerationSequence(), report.getPeriodStart(), report.getPeriodEnd(),
                    maskPhoneNumber(memberPhoneNumber));
            return GenerateWeeklyReportResponse.from(report, EMPTY_SUMMARY_MESSAGE);
        }

        log.info("Weekly report generation requested reportId={} generationSequence={} periodStart={} periodEnd={} memberPhoneSuffix={}",
                report.getId(), report.getGenerationSequence(), report.getPeriodStart(), report.getPeriodEnd(),
                maskPhoneNumber(memberPhoneNumber));
        eventPublisher.publishEvent(new WeeklyReportGenerationRequestedEvent(report.getId(), report.getGenerationSequence()));
        log.info("Weekly report generation event published reportId={} generationSequence={}",
                report.getId(), report.getGenerationSequence());

        return GenerateWeeklyReportResponse.from(report, START_MESSAGE);
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

    private EmotionReport prepareReport(String memberPhoneNumber, LocalDate periodStart, LocalDate periodEnd) {
        EmotionReport existingReport = emotionReportRepository.findByMemberPhoneNumberAndReportTypeAndPeriodStart(
                memberPhoneNumber,
                EmotionReportType.WEEKLY,
                periodStart
        ).orElse(null);

        if (existingReport != null) {
            Long deletedReportId = existingReport.getId();
            emotionReportRepository.delete(existingReport);
            emotionReportRepository.flush();
            log.info("Weekly report deleted before regeneration reportId={} periodStart={} memberPhoneSuffix={}",
                    deletedReportId, periodStart, maskPhoneNumber(memberPhoneNumber));
        }

        return createReport(memberPhoneNumber, periodStart, periodEnd);
    }

    private boolean hasDiarySource(String memberPhoneNumber, LocalDate periodStart, LocalDate periodEnd) {
        LocalDate endedAtExclusive = periodEnd.plusDays(1);
        return diaryRepository.existsByMemberPhoneNumberAndRecordedDateGreaterThanEqualAndRecordedDateLessThan(
                memberPhoneNumber,
                periodStart,
                endedAtExclusive
        );
    }

    private void completeWithEmptySummary(EmotionReport report) {
        report.markProcessing();
        report.complete(new WeeklyReportAiResult(emptySummary(resolveDisplayName(report.getMember().getName())), null), 0);
    }

    private LocalDate normalizeWeekStart(GenerateWeeklyReportRequest request) {
        if (request == null || request.weekStart() == null) {
            throw new ReportRequestException(ErrorCode.INVALID_REPORT_REQUEST, "weekStart는 필수입니다.");
        }
        return normalizeWeekStart(request.weekStart());
    }

    private LocalDate normalizeWeekStart(LocalDate weekStart) {
        if (weekStart.getDayOfWeek() == DayOfWeek.MONDAY) {
            return weekStart;
        }
        return weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private String emptySummary(String displayName) {
        return "이번 주는 " + displayName + "의 기록이 아직 없어요.\n\n"
                + "이번 주에 남겨진 일기가 없어 감정 흐름을 충분히 요약하지는 못했어요.\n\n"
                + "첫 일기를 작성해 주시면 이번 주의 마음 변화를 따뜻하게 정리해드릴게요.";
    }

    private String resolveDisplayName(String memberName) {
        if (!StringUtils.hasText(memberName)) {
            return "마음님";
        }
        String normalized = memberName.trim();
        return normalized.endsWith("님") ? normalized : normalized + "님";
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(phoneNumber.length() - 4);
    }
}
