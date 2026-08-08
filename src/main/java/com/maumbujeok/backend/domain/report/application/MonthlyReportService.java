package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import com.maumbujeok.backend.domain.report.dto.GenerateMonthlyReportRequest;
import com.maumbujeok.backend.domain.report.dto.GenerateWeeklyReportResponse;
import com.maumbujeok.backend.domain.report.dto.MonthlyReportPeriodResponse;
import com.maumbujeok.backend.domain.report.dto.WeeklyReportSummaryResponse;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MonthlyReportService {
    private static final String PROMPT_VERSION = "monthly-report-v1";
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final EmotionReportRepository reportRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public GenerateWeeklyReportResponse generate(String phone, GenerateMonthlyReportRequest request) {
        YearMonth month = parse(request);
        return generateForMonth(phone, month);
    }

    @Transactional(readOnly = true)
    public WeeklyReportSummaryResponse get(String phone, int year, int month) {
        YearMonth target = parse(year, month);
        EmotionReport report = reportRepository.findByMemberPhoneNumberAndReportTypeAndPeriodStart(phone, EmotionReportType.MONTHLY, target.atDay(1))
                .orElseThrow(() -> new ReportRequestException(ErrorCode.REPORT_NOT_FOUND, "Monthly report not found"));
        return WeeklyReportSummaryResponse.from(report);
    }

    @Transactional(readOnly = true)
    public List<MonthlyReportPeriodResponse> getPeriods(String phone) {
        return reportRepository.findAllByMemberPhoneNumberAndReportTypeOrderByPeriodStartDesc(phone, EmotionReportType.MONTHLY)
                .stream().map(MonthlyReportPeriodResponse::from).toList();
    }

    @Transactional
    public GenerateWeeklyReportResponse regenerate(String phone, Long reportId) {
        EmotionReport report = reportRepository.findByIdAndMemberPhoneNumberAndReportType(reportId, phone, EmotionReportType.MONTHLY)
                .orElseThrow(() -> new ReportRequestException(ErrorCode.REPORT_NOT_FOUND, "Monthly report not found"));
        return generateForMonth(phone, YearMonth.from(report.getPeriodStart()));
    }

    private GenerateWeeklyReportResponse generateForMonth(String phone, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        reportRepository.findByMemberPhoneNumberAndReportTypeAndPeriodStart(phone, EmotionReportType.MONTHLY, start).ifPresent(existing -> {
            reportRepository.delete(existing);
            reportRepository.flush();
        });
        Member member = memberRepository.getReferenceById(phone);
        EmotionReport report = reportRepository.save(new EmotionReport(member, EmotionReportType.MONTHLY, start, end, PROMPT_VERSION));
        boolean hasDiary = diaryRepository.existsByMemberPhoneNumberAndRecordedDateGreaterThanEqualAndRecordedDateLessThan(phone, start, end.plusDays(1));
        if (!hasDiary) {
            report.markProcessing();
            report.complete(new WeeklyReportAiResult("해당 월에 작성된 일기가 없어 감정 요약을 만들 수 없습니다.", null), 0);
            return GenerateWeeklyReportResponse.from(report, "작성된 일기가 없어 빈 월간 리포트로 완료되었습니다.");
        }
        eventPublisher.publishEvent(new WeeklyReportGenerationRequestedEvent(report.getId(), report.getGenerationSequence()));
        return GenerateWeeklyReportResponse.from(report, "월간 감정 리포트 생성을 시작했습니다.");
    }

    private YearMonth parse(GenerateMonthlyReportRequest request) {
        if (request == null || request.year() == null || request.month() == null) {
            throw new ReportRequestException(ErrorCode.INVALID_REPORT_REQUEST, "year and month are required");
        }
        return parse(request.year(), request.month());
    }

    private YearMonth parse(int year, int month) {
        try { return YearMonth.of(year, month); }
        catch (DateTimeException exception) { throw new ReportRequestException(ErrorCode.INVALID_REPORT_REQUEST, "Invalid year or month"); }
    }
}