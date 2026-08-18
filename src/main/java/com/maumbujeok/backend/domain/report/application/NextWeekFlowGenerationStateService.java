package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportGenerationStatus;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlow;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.domain.report.repository.NextWeekFlowRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NextWeekFlowGenerationStateService {
    private static final String DEFAULT_WEEKLY_INSIGHT = "이번 주 작성된 감정 흐름 요약이 없습니다.";

    private final NextWeekFlowRepository nextWeekFlowRepository;
    private final EmotionReportRepository emotionReportRepository;
    private final MemberSajuProfileRepository sajuProfileRepository;

    @Transactional(readOnly = true)
    public Optional<NextWeekFlowGenerationInput> loadIfCurrent(
            Long flowId,
            Long expectedReportId,
            int expectedReportGenerationSequence
    ) {
        NextWeekFlow flow = nextWeekFlowRepository.findById(flowId).orElse(null);
        EmotionReport report = emotionReportRepository.findById(expectedReportId).orElse(null);
        if (!isCurrent(flow, report, expectedReportGenerationSequence)
                || flow.getGenerationStatus() != NextWeekFlowGenerationStatus.PROCESSING) {
            return Optional.empty();
        }

        Member member = flow.getMember();
        MemberSajuProfile sajuProfile = sajuProfileRepository.findByMember(member).orElse(null);
        String weeklyInsight = report.getInsightSummary() != null
                ? report.getInsightSummary()
                : DEFAULT_WEEKLY_INSIGHT;

        return Optional.of(new NextWeekFlowGenerationInput(
                member.getName(),
                sajuProfile != null ? sajuProfile.getGender().name() : "NONE",
                sajuProfile != null ? sajuProfile.getCalendarType().name() : "SOLAR",
                member.getBirthDate() != null ? member.getBirthDate() : "NONE",
                sajuProfile != null && sajuProfile.getBirthTime() != null
                        ? sajuProfile.getBirthTime().toString()
                        : "NONE",
                weeklyInsight
        ));
    }

    @Transactional
    public boolean completeIfCurrent(
            Long flowId,
            Long expectedReportId,
            int expectedReportGenerationSequence,
            String adviceText
    ) {
        EmotionReport report = emotionReportRepository.findByIdForUpdate(expectedReportId).orElse(null);
        NextWeekFlow flow = nextWeekFlowRepository.findById(flowId).orElse(null);
        if (!isCurrent(flow, report, expectedReportGenerationSequence)
                || flow.getGenerationStatus() != NextWeekFlowGenerationStatus.PROCESSING) {
            return false;
        }

        flow.complete(adviceText, "openai", "v1");
        return true;
    }

    @Transactional
    public boolean failIfCurrent(
            Long flowId,
            Long expectedReportId,
            int expectedReportGenerationSequence
    ) {
        EmotionReport report = emotionReportRepository.findByIdForUpdate(expectedReportId).orElse(null);
        NextWeekFlow flow = nextWeekFlowRepository.findById(flowId).orElse(null);
        if (!isCurrent(flow, report, expectedReportGenerationSequence)
                || flow.getGenerationStatus() != NextWeekFlowGenerationStatus.PROCESSING) {
            return false;
        }

        flow.fail();
        return true;
    }

    private boolean isCurrent(
            NextWeekFlow flow,
            EmotionReport report,
            int expectedReportGenerationSequence
    ) {
        if (flow == null || report == null || flow.getEmotionReport() == null) {
            return false;
        }

        EmotionReportGenerationStatus status = report.getGenerationStatus();
        return flow.getEmotionReport().getId().equals(report.getId())
                && report.matches(expectedReportGenerationSequence)
                && (status == EmotionReportGenerationStatus.COMPLETED
                    || status == EmotionReportGenerationStatus.FALLBACK_COMPLETED);
    }
}
