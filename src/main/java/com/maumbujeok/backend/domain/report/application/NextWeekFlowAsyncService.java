package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.report.ai.NextWeekFlowAiProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NextWeekFlowAsyncService {
    private final NextWeekFlowGenerationStateService stateService;
    private final NextWeekFlowAiProvider aiProvider;

    @Async("weeklyReportExecutor")
    public void generate(Long flowId, Long expectedReportId, int expectedReportGenerationSequence) {
        NextWeekFlowGenerationInput input = stateService.loadIfCurrent(
                flowId, expectedReportId, expectedReportGenerationSequence).orElse(null);
        if (input == null) {
            log.info("Next week flow generation skipped flowId={} reportId={} generationSequence={} reason=stale_or_not_processing",
                    flowId, expectedReportId, expectedReportGenerationSequence);
            return;
        }

        log.info("Async next week flow generation started flowId={} reportId={} generationSequence={}",
                flowId, expectedReportId, expectedReportGenerationSequence);

        try {
            String adviceText = aiProvider.generate(
                    input.memberName(),
                    input.gender(),
                    input.calendarType(),
                    input.birthDate(),
                    input.birthTime(),
                    input.weeklyInsight()
            );

            boolean completed = stateService.completeIfCurrent(
                    flowId, expectedReportId, expectedReportGenerationSequence, adviceText);
            if (completed) {
                log.info("Async next week flow generation completed flowId={} reportId={} generationSequence={}",
                        flowId, expectedReportId, expectedReportGenerationSequence);
            } else {
                log.info("Next week flow result ignored flowId={} reportId={} generationSequence={} reason=stale",
                        flowId, expectedReportId, expectedReportGenerationSequence);
            }
        } catch (Exception e) {
            log.error("Failed to generate next week flow for flowId={}", flowId, e);
            stateService.failIfCurrent(flowId, expectedReportId, expectedReportGenerationSequence);
        }
    }

    public void failFlowOnRejection(
            Long flowId,
            Long expectedReportId,
            int expectedReportGenerationSequence
    ) {
        if (stateService.failIfCurrent(flowId, expectedReportId, expectedReportGenerationSequence)) {
            log.warn("Marked next week flow as FAILED due to task rejection flowId={}", flowId);
        }
    }
}
