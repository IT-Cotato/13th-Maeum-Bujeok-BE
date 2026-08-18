package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
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
import java.time.DayOfWeek;
import java.time.Duration;
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
public class NextWeekFlowService {
    private static final String DEFAULT_FAILED_ADVICE = "AI 조언 생성에 실패했습니다. 다음 주 흐름 분석을 다시 요청해 주세요.";
    private static final String START_MESSAGE = "다음 주 흐름 생성을 비동기로 시작했습니다.";
    public static final Duration STALE_PROCESSING_THRESHOLD = Duration.ofSeconds(30);

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final EmotionReportRepository emotionReportRepository;
    private final NextWeekFlowRepository nextWeekFlowRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public NextWeekFlowStartResponse generate(String memberPhoneNumber, NextWeekFlowRequest request) {
        if (request == null || request.weekStart() == null) {
            throw new CustomException(ErrorCode.INVALID_REPORT_REQUEST);
        }
        LocalDate weekStart = normalizeWeekStart(request.parsedWeekStart());

        // 1. 최소 3개 작성 여부 검증 (활성 + 소각 포함)
        long diaryCount = diaryRepository
                .countByMemberPhoneNumberAndRecordedDateGreaterThanEqualAndRecordedDateLessThan(
                        memberPhoneNumber,
                        weekStart,
                        weekStart.plusDays(7));
        if (diaryCount < 3) {
            throw new CustomException(ErrorCode.INVALID_REPORT_REQUEST);
        }

        EmotionReport weeklyReport = emotionReportRepository.findByMemberPhoneNumberAndReportTypeAndPeriodStart(
                memberPhoneNumber,
                EmotionReportType.WEEKLY,
                weekStart).orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if (weeklyReport.getGenerationStatus() != EmotionReportGenerationStatus.COMPLETED
                && weeklyReport.getGenerationStatus() != EmotionReportGenerationStatus.FALLBACK_COMPLETED) {
            throw new CustomException(ErrorCode.INVALID_REPORT_REQUEST);
        }

        return generateForReport(memberPhoneNumber, weeklyReport, request);
    }

    @Transactional
    public NextWeekFlowQueryResponse getOrGenerateFlowForWeeklyReport(String memberPhoneNumber,
            EmotionReport weeklyReport) {
        if (weeklyReport == null) {
            throw new CustomException(ErrorCode.REPORT_NOT_FOUND);
        }
        LocalDate weekStart = weeklyReport.getPeriodStart();

        return nextWeekFlowRepository.findByMemberPhoneNumberAndWeekStart(memberPhoneNumber, weekStart)
                .map(flow -> {
                    LocalDateTime now = LocalDateTime.now();
                    LocalDateTime cutoff = now.minus(STALE_PROCESSING_THRESHOLD);
                    if (isStaleProcessing(flow, cutoff)) {
                        int acquired = nextWeekFlowRepository.tryAcquireStaleRecovery(
                                flow.getId(),
                                NextWeekFlowGenerationStatus.PROCESSING,
                                cutoff,
                                now);
                        if (acquired > 0) {
                            log.warn("Stale PROCESSING NextWeekFlow acquired for recovery flowId={} weekStart={}",
                                    flow.getId(), weekStart);
                            eventPublisher.publishEvent(new NextWeekFlowGenerationRequestedEvent(flow.getId()));
                        }
                    }
                    return getFlow(memberPhoneNumber, flow.getId());
                })
                .orElseGet(() -> {
                    NextWeekFlowRequest request = new NextWeekFlowRequest(weekStart.toString());
                    NextWeekFlowStartResponse startResponse = generateForReport(memberPhoneNumber, weeklyReport,
                            request);
                    return getFlow(memberPhoneNumber, startResponse.flowId());
                });
    }

    private boolean isStaleProcessing(NextWeekFlow flow, LocalDateTime cutoff) {
        if (flow.getGenerationStatus() != NextWeekFlowGenerationStatus.PROCESSING || flow.getAdviceText() != null) {
            return false;
        }
        LocalDateTime referenceTime = flow.getUpdatedAt() != null ? flow.getUpdatedAt() : flow.getCreatedAt();
        return referenceTime != null && referenceTime.isBefore(cutoff);
    }

    private NextWeekFlowStartResponse generateForReport(String memberPhoneNumber, EmotionReport weeklyReport,
            NextWeekFlowRequest request) {
        LocalDate weekStart = normalizeWeekStart(request.parsedWeekStart());
        Member member = memberRepository.findByPhoneNumber(memberPhoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Delete existing next week flow if exists
        nextWeekFlowRepository.findByMemberPhoneNumberAndWeekStart(memberPhoneNumber, weekStart)
                .ifPresent(existing -> {
                    nextWeekFlowRepository.delete(existing);
                    nextWeekFlowRepository.flush();
                });

        LocalDate periodStart = weekStart.plusDays(7);
        LocalDate periodEnd = weekStart.plusDays(13);

        NextWeekFlow flow = NextWeekFlow.builder()
                .member(member)
                .emotionReport(weeklyReport)
                .weekStart(weekStart)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build();

        NextWeekFlow savedFlow = nextWeekFlowRepository.save(flow);

        // Publish event for AFTER_COMMIT execution
        eventPublisher.publishEvent(new NextWeekFlowGenerationRequestedEvent(savedFlow.getId()));

        return new NextWeekFlowStartResponse(
                savedFlow.getId(),
                weeklyReport.getId(),
                savedFlow.getWeekStart(),
                savedFlow.getGenerationStatus(),
                START_MESSAGE);
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

        return new NextWeekFlowQueryResponse(
                flow.getId(),
                flow.getEmotionReport() != null ? flow.getEmotionReport().getId() : null,
                flow.getPeriodStart(),
                flow.getPeriodEnd(),
                flow.getGenerationStatus(),
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

        boolean flowExists = nextWeekFlowRepository.findByMemberPhoneNumberAndWeekStart(phone, weekStart).isPresent();
        if (flowExists) {
            try {
                generate(phone, new NextWeekFlowRequest(weekStart.toString()));
                log.info("Next week flow auto-refreshed on new diary entry memberPhoneSuffix={} weekStart={}",
                        maskPhoneNumber(phone), weekStart);
            } catch (Exception e) {
                log.warn("Failed to auto-refresh next week flow memberPhoneSuffix={} weekStart={}",
                        maskPhoneNumber(phone), weekStart, e);
            }
        }
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
}
