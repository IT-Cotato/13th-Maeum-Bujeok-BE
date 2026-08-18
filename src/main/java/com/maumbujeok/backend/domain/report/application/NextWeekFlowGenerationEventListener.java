package com.maumbujeok.backend.domain.report.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NextWeekFlowGenerationEventListener {
    private final NextWeekFlowAsyncService asyncService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(NextWeekFlowGenerationRequestedEvent event) {
        log.info("Next week flow generation event received flowId={}", event.flowId());
        try {
            asyncService.generate(event.flowId(), event.emotionReportId(), event.reportGenerationSequence());
            log.info("Next week flow generation event dispatched flowId={}", event.flowId());
        } catch (TaskRejectedException e) {
            log.error("Task rejected for flowId={} due to thread pool saturation", event.flowId(), e);
            asyncService.failFlowOnRejection(
                    event.flowId(), event.emotionReportId(), event.reportGenerationSequence());
        }
    }
}
