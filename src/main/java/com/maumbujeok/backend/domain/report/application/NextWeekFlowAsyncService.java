package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.report.ai.NextWeekFlowAiProvider;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlow;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import com.maumbujeok.backend.domain.report.repository.NextWeekFlowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NextWeekFlowAsyncService {
    private final NextWeekFlowRepository nextWeekFlowRepository;
    private final MemberSajuProfileRepository sajuProfileRepository;
    private final NextWeekFlowAiProvider aiProvider;

    @Async("weeklyReportExecutor")
    @Transactional
    public void generate(Long flowId) {
        NextWeekFlow flow = nextWeekFlowRepository.findById(flowId).orElse(null);
        if (flow == null) {
            log.warn("Next week flow not found flowId={}", flowId);
            return;
        }

        if (flow.getGenerationStatus() == NextWeekFlowGenerationStatus.COMPLETED) {
            log.info("Next week flow already completed flowId={}", flowId);
            return;
        }

        log.info("Async next week flow generation started flowId={}", flowId);

        try {
            Member member = flow.getMember();
            MemberSajuProfile sajuProfile = sajuProfileRepository.findByMember(member).orElse(null);

            String gender = sajuProfile != null ? sajuProfile.getGender().name() : "NONE";
            String calendarType = sajuProfile != null ? sajuProfile.getCalendarType().name() : "SOLAR";
            String birthTime = (sajuProfile != null && sajuProfile.getBirthTime() != null) ? sajuProfile.getBirthTime().toString() : "NONE";
            String birthDate = member.getBirthDate() != null ? member.getBirthDate() : "NONE";
            
            String weeklyInsight = "이번 주 작성된 감정 흐름 요약이 없습니다.";
            if (flow.getEmotionReport() != null && flow.getEmotionReport().getInsightSummary() != null) {
                weeklyInsight = flow.getEmotionReport().getInsightSummary();
            }

            String adviceText = aiProvider.generate(
                    member.getName(),
                    gender,
                    calendarType,
                    birthDate,
                    birthTime,
                    weeklyInsight
            );

            flow.complete(adviceText, "openai", "v1");
            log.info("Async next week flow generation completed flowId={}", flowId);
        } catch (Exception e) {
            log.error("Failed to generate next week flow for flowId={}", flowId, e);
            flow.fail();
        }
    }

    @Transactional
    public void failFlowOnRejection(Long flowId) {
        NextWeekFlow flow = nextWeekFlowRepository.findById(flowId).orElse(null);
        if (flow != null && flow.getGenerationStatus() == NextWeekFlowGenerationStatus.PROCESSING) {
            flow.fail();
            log.warn("Marked next week flow as FAILED due to task rejection flowId={}", flowId);
        }
    }
}
