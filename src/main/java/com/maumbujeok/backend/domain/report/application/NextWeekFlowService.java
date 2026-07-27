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
import java.time.LocalDate;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final NextWeekFlowAsyncService asyncService;

    @Transactional
    public NextWeekFlowStartResponse generate(String memberPhoneNumber, NextWeekFlowRequest request) {
        LocalDate weekStart = request.weekStart();

        Member member = memberRepository.findByPhoneNumber(memberPhoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        EmotionReport weeklyReport = emotionReportRepository.findByMemberPhoneNumberAndReportTypeAndPeriodStart(
                memberPhoneNumber,
                EmotionReportType.WEEKLY,
                weekStart
        ).orElseThrow(() -> new CustomException(ErrorCode.REPORT_NOT_FOUND));

        if (weeklyReport.getGenerationStatus() != EmotionReportGenerationStatus.COMPLETED
                && weeklyReport.getGenerationStatus() != EmotionReportGenerationStatus.FALLBACK_COMPLETED) {
            throw new CustomException(ErrorCode.INVALID_REPORT_REQUEST);
        }

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

        // Run async generation
        try {
            asyncService.generate(savedFlow.getId());
        } catch (org.springframework.core.task.TaskRejectedException e) {
            log.error("Task rejected for flowId={} due to thread pool saturation", savedFlow.getId(), e);
            savedFlow.fail();
            nextWeekFlowRepository.saveAndFlush(savedFlow);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        return new NextWeekFlowStartResponse(
                savedFlow.getId(),
                weeklyReport.getId(),
                savedFlow.getWeekStart(),
                savedFlow.getGenerationStatus(),
                START_MESSAGE
        );
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

        ZoneOffset SEOUL_OFFSET = ZoneOffset.ofHours(9);

        return new NextWeekFlowQueryResponse(
                flow.getId(),
                flow.getEmotionReport().getId(),
                flow.getPeriodStart(),
                flow.getPeriodEnd(),
                flow.getGenerationStatus(),
                adviceText,
                flow.getModelName(),
                flow.getReportVersion(),
                flow.getGeneratedAt() == null ? null : flow.getGeneratedAt().atOffset(SEOUL_OFFSET)
        );
    }
}
