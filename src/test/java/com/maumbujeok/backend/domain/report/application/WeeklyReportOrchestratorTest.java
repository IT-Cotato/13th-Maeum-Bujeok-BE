package com.maumbujeok.backend.domain.report.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiCallResult;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiClient;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiException;
import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WeeklyReportOrchestratorTest {
    @Mock WeeklyReportGenerationStateService stateService;
    @Mock WeeklyReportGenerationInputLoader inputLoader;
    @Mock WeeklyReportAiClient aiClient;
    @Mock WeeklyReportResponseSafetyGuard safetyGuard;
    @Mock WeeklyReportFallbackFactory fallbackFactory;
    @Mock NextWeekFlowService nextWeekFlowService;

    private WeeklyReportOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new WeeklyReportOrchestrator(
                stateService,
                inputLoader,
                aiClient,
                safetyGuard,
                fallbackFactory,
                nextWeekFlowService
        );
    }

    @Test
    void skipsAlreadyClaimedOrStaleGeneration() {
        when(stateService.begin(1L, 2)).thenReturn(false);

        orchestrator.generate(1L, 2);

        verify(inputLoader, never()).load(any());
        verify(aiClient, never()).generate(any());
    }

    @Test
    void savesFallbackWhenProviderFails() {
        WeeklyReportGenerationInput input = input();
        WeeklyReportAiResult fallback = result("fallback-weekly-report-v1");
        when(stateService.begin(1L, 2)).thenReturn(true);
        when(inputLoader.load(1L)).thenReturn(input);
        when(aiClient.generate(any())).thenThrow(new WeeklyReportAiException("AI_TIMEOUT", 2, new RuntimeException()));
        when(fallbackFactory.create(input)).thenReturn(fallback);

        orchestrator.generate(1L, 2);

        verify(safetyGuard).validate(fallback);
        verify(stateService).fallback(1L, 2, fallback, 2, "AI_TIMEOUT");
    }

    @Test
    void retriesOnceWhenAiOutputFailsQualityValidation() {
        WeeklyReportGenerationInput input = input();
        WeeklyReportAiResult contaminated = result("gpt-test");
        WeeklyReportAiResult corrected = new WeeklyReportAiResult(
                "이번 주는 마음님에게 꽤 괜찮은 한 주였어요!\n\n이번 주 마음님의 기록에는 따뜻한 기운이 가득했어요.\n\n75.0%가 긍정적인 감정으로 채워진 한 주였네요. 기록 속에서 스스로를 다독이는 흐름이 자주 보였고, 그 안정감을 다음 주에도 이어가 보면 좋겠어요.",
                "gpt-test"
        );
        when(stateService.begin(1L, 2)).thenReturn(true);
        when(inputLoader.load(1L)).thenReturn(input);
        when(aiClient.generate(any()))
                .thenReturn(new WeeklyReportAiCallResult(contaminated, 1))
                .thenReturn(new WeeklyReportAiCallResult(corrected, 1));
        when(stateService.complete(1L, 2, corrected, 2)).thenReturn(true);
        org.mockito.Mockito.doThrow(new IllegalArgumentException("invalid output"))
                .doNothing()
                .when(safetyGuard).validate(any());

        orchestrator.generate(1L, 2);

        verify(aiClient, times(2)).generate(any());
        verify(stateService).complete(1L, 2, corrected, 2);
        verify(stateService, never()).fallback(any(), anyInt(), any(), anyInt(), any());
    }

    private WeeklyReportGenerationInput input() {
        return new WeeklyReportGenerationInput(
                1L,
                2,
                "마음님",
                LocalDate.of(2026, 7, 13),
                LocalDate.of(2026, 7, 19),
                List.of(new WeeklyDiaryEntry(LocalDate.of(2026, 7, 15), "HAPPY", "행복해요", "오늘은 좋은 일이 있었다")),
                new WeeklyEmotionSnapshot(
                        1,
                        100.0,
                        0.0,
                        0.0,
                        "행복해요",
                        List.of(new WeeklyEmotionCount("HAPPY", "행복해요", 1, 100.0, "POSITIVE"))
                )
        );
    }

    private WeeklyReportAiResult result(String modelName) {
        return new WeeklyReportAiResult(
                "이번 주는 마음님에게 차분한 한 주였어요.\n\n이번 주 마음님의 기록에는 잔잔한 흐름이 이어졌어요.\n\n100.0%가 긍정적인 감정으로 채워진 한 주였네요. 일상 속에서 마음을 정리하는 힘이 느껴졌고, 그 흐름을 천천히 이어가도 좋겠어요.",
                modelName
        );
    }
}
