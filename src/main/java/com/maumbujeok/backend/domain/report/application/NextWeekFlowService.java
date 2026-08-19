package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportGenerationStatus;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlow;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import com.maumbujeok.backend.domain.report.dto.NextWeekFlowQueryResponse;
import com.maumbujeok.backend.domain.report.dto.NextWeekFlowRequest;
import com.maumbujeok.backend.domain.report.dto.NextWeekFlowStartResponse;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.domain.report.repository.NextWeekFlowRepository;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NextWeekFlowService {
    private static final String DEFAULT_FAILED_ADVICE = "AI 조언 생성에 실패했습니다. 다음 주 흐름 분석을 다시 요청해 주세요.";
    private static final String START_MESSAGE = "다음 주 흐름 생성을 비동기로 시작했습니다.";

    private final MemberRepository memberRepository;
    private final EmotionReportRepository emotionReportRepository;
    private final NextWeekFlowRepository nextWeekFlowRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock serviceClock;
    @Transactional
    public NextWeekFlowStartResponse generate(String memberPhoneNumber, NextWeekFlowRequest request) {
        if (request == null || request.weekStart() == null) {
            throw new CustomException(ErrorCode.INVALID_REPORT_REQUEST);
        }
        LocalDate weekStart = normalizeWeekStart(request.parsedWeekStart());

        EmotionReport weeklyReport = emotionReportRepository.findByMemberPhoneNumberAndReportTypeAndPeriodStart(
                memberPhoneNumber,
                EmotionReportType.WEEKLY,
                weekStart).orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if (!isEligibleReportStatus(weeklyReport)) {
            throw new CustomException(ErrorCode.INVALID_REPORT_REQUEST);
        }

        return generateForReport(memberPhoneNumber, weeklyReport, request, GenerationMode.FORCE_REPLACE);
    }

    @Transactional
    public NextWeekFlowQueryResponse getOrGenerateFlowForWeeklyReport(String memberPhoneNumber,
            EmotionReport weeklyReport) {
        if (weeklyReport == null) {
            throw new CustomException(ErrorCode.REPORT_NOT_FOUND);
        }
        LocalDate weekStart = weeklyReport.getPeriodStart();
        NextWeekFlowRequest request = new NextWeekFlowRequest(weekStart.toString());
        NextWeekFlowStartResponse startResponse = generateForReport(
                memberPhoneNumber, weeklyReport, request, GenerationMode.REFRESH_IF_STALE);
        return getFlow(memberPhoneNumber, startResponse.flowId());
    }

    private NextWeekFlowStartResponse generateForReport(String memberPhoneNumber, EmotionReport weeklyReport,
            NextWeekFlowRequest request, GenerationMode generationMode) {
        LocalDate weekStart = normalizeWeekStart(request.parsedWeekStart());
        EmotionReport lockedReport = emotionReportRepository.findByIdForUpdate(weeklyReport.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));
        if (!lockedReport.getMember().getPhoneNumber().equals(memberPhoneNumber)
                || lockedReport.getReportType() != EmotionReportType.WEEKLY
                || !lockedReport.getPeriodStart().equals(weekStart)) {
            throw new CustomException(ErrorCode.REPORT_NOT_FOUND);
        }
        if (!isEligibleReportStatus(lockedReport)) {
            throw new CustomException(ErrorCode.INVALID_REPORT_REQUEST);
        }

        Member member = memberRepository.findByPhoneNumber(memberPhoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        NextWeekFlow existingFlow = nextWeekFlowRepository
                .findByMemberPhoneNumberAndWeekStart(memberPhoneNumber, weekStart)
                .orElse(null);
        if (existingFlow != null) {
            if (generationMode == GenerationMode.REFRESH_IF_STALE
                    && isCreatedForCurrentReportGeneration(existingFlow, lockedReport)) {
                return toStartResponse(existingFlow, lockedReport);
            }
            nextWeekFlowRepository.delete(existingFlow);
            nextWeekFlowRepository.flush();
        }

        LocalDate periodStart = weekStart.plusDays(7);
        LocalDate periodEnd = weekStart.plusDays(13);

        NextWeekFlow flow = NextWeekFlow.builder()
                .member(member)
                .emotionReport(lockedReport)
                .weekStart(weekStart)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build();

        NextWeekFlow savedFlow = nextWeekFlowRepository.save(flow);

        // Publish event for AFTER_COMMIT execution
        eventPublisher.publishEvent(new NextWeekFlowGenerationRequestedEvent(
                savedFlow.getId(), lockedReport.getId(), lockedReport.getGenerationSequence()));

        return toStartResponse(savedFlow, lockedReport);
    }

    @Transactional(readOnly = true)
    public NextWeekFlowQueryResponse getFlow(String memberPhoneNumber, Long flowId) {
        NextWeekFlow flow = nextWeekFlowRepository.findById(flowId)
                .orElseThrow(() -> new CustomException(ErrorCode.FLOW_NOT_FOUND));

        if (!flow.getMember().getPhoneNumber().equals(memberPhoneNumber)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        String adviceText = flow.getAdviceText();
        if (flow.getGenerationStatus() == NextWeekFlowGenerationStatus.FAILED) {
            adviceText = DEFAULT_FAILED_ADVICE;
        }

        String title = flow.getTitle();
        if ((title == null || title.isBlank()) && flow.getGenerationStatus() == NextWeekFlowGenerationStatus.COMPLETED) {
            title = "다음 주를 위한 마음가짐";
        }

        return new NextWeekFlowQueryResponse(
                flow.getId(),
                flow.getEmotionReport() != null ? flow.getEmotionReport().getId() : null,
                flow.getPeriodStart(),
                flow.getPeriodEnd(),
                flow.getGenerationStatus(),
                title,
                adviceText,
                flow.getModelName(),
                flow.getReportVersion(),
                com.maumbujeok.backend.global.util.TimeUtils.toSeoulOffset(flow.getGeneratedAt()));
    }

    @Transactional
    public void refreshIfEligible(Long emotionReportId) {
        EmotionReport report = emotionReportRepository.findById(emotionReportId).orElse(null);
        if (report == null || report.getReportType() != EmotionReportType.WEEKLY) {
            return;
        }

        String phone = report.getMember().getPhoneNumber();
        LocalDate weekStart = report.getPeriodStart();
        if (!isEligibleReportStatus(report)) {
            return;
        }

        LocalDate currentWeekStart = LocalDate.now(serviceClock).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        if (weekStart.isBefore(currentWeekStart)) {
            log.info("Next week flow auto-refresh skipped for past week memberPhoneSuffix={} weekStart={} currentWeekStart={}",
                    maskPhoneNumber(phone), weekStart, currentWeekStart);
            return;
        }

        try {
            generateForReport(phone, report, new NextWeekFlowRequest(weekStart.toString()),
                    GenerationMode.REFRESH_IF_STALE);
            log.info("Next week flow auto-generated or refreshed memberPhoneSuffix={} weekStart={} reportId={} generationSequence={}",
                    maskPhoneNumber(phone), weekStart, report.getId(), report.getGenerationSequence());
        } catch (Exception e) {
            log.warn("Failed to auto-generate or refresh next week flow memberPhoneSuffix={} weekStart={}",
                    maskPhoneNumber(phone), weekStart, e);
        }
    }

    private boolean isEligibleReportStatus(EmotionReport report) {
        return report.getGenerationStatus() == EmotionReportGenerationStatus.COMPLETED
                || report.getGenerationStatus() == EmotionReportGenerationStatus.FALLBACK_COMPLETED;
    }

    private boolean isCreatedForCurrentReportGeneration(NextWeekFlow flow, EmotionReport report) {
        return flow.getEmotionReport().getId().equals(report.getId())
                && flow.getCreatedAt() != null
                && report.getGeneratedAt() != null
                && !flow.getCreatedAt().isBefore(report.getGeneratedAt());
    }

    private NextWeekFlowStartResponse toStartResponse(NextWeekFlow flow, EmotionReport report) {
        return new NextWeekFlowStartResponse(
                flow.getId(),
                report.getId(),
                flow.getWeekStart(),
                flow.getGenerationStatus(),
                START_MESSAGE);
    }

    private LocalDate normalizeWeekStart(LocalDate weekStart) {
        if (weekStart == null) {
            return null;
        }
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

    private enum GenerationMode {
        REFRESH_IF_STALE,
        FORCE_REPLACE
    }
}
