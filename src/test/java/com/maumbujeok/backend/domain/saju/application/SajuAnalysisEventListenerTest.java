package com.maumbujeok.backend.domain.saju.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

@ExtendWith(MockitoExtension.class)
class SajuAnalysisEventListenerTest {

    @Mock TaskExecutor sajuAnalysisExecutor;
    @Mock SajuAnalysisOrchestrator orchestrator;

    private SajuAnalysisEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new SajuAnalysisEventListener(sajuAnalysisExecutor, orchestrator);
    }

    @Test
    void delegatesAcceptedEventToExecutorTask() {
        SajuAnalysisRequestedEvent event = new SajuAnalysisRequestedEvent(1L, 2L);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(sajuAnalysisExecutor).execute(any(Runnable.class));

        listener.handle(event);

        verify(orchestrator).analyze(1L, 2L);
    }

    @Test
    void retriesInCallerThreadWhenExecutorRejectsTask() {
        SajuAnalysisRequestedEvent event = new SajuAnalysisRequestedEvent(3L, 4L);
        doThrow(new TaskRejectedException("queue full"))
                .when(sajuAnalysisExecutor).execute(any(Runnable.class));

        listener.handle(event);

        verify(orchestrator).analyze(3L, 4L);
    }
}
