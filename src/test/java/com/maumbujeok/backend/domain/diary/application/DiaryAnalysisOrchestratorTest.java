package com.maumbujeok.backend.domain.diary.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maumbujeok.backend.domain.diary.ai.AiAnalysisException;
import com.maumbujeok.backend.domain.diary.ai.AiDiaryAnalysisClient;
import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiaryAnalysisOrchestratorTest {
    @Mock DiaryAnalysisStateService stateService;
    @Mock DiaryAnalysisInputLoader inputLoader;
    @Mock SajuContextProvider sajuContextProvider;
    @Mock AiDiaryAnalysisClient aiClient;
    @Mock NegativeIntensityPolicy intensityPolicy;
    @Mock AiResponseSafetyGuard safetyGuard;
    @Mock DiaryAnalysisFallbackFactory fallbackFactory;

    private DiaryAnalysisOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new DiaryAnalysisOrchestrator(
                stateService, inputLoader, sajuContextProvider, aiClient,
                intensityPolicy, safetyGuard, fallbackFactory
        );
    }

    @Test
    void skipsAlreadyClaimedAnalysis() {
        when(stateService.begin(1L)).thenReturn(false);

        orchestrator.analyze(1L);

        verify(inputLoader, never()).load(any());
        verify(aiClient, never()).analyze(any());
    }

    @Test
    void savesReviewedFallbackWhenProviderFails() {
        DiaryAnalysisInput input = new DiaryAnalysisInput(1L, "오늘 너무 불안하고 힘들었다", "불안");
        DiaryAiResult fallback = result("fallback-rule-v1");
        when(stateService.begin(1L)).thenReturn(true);
        when(inputLoader.load(1L)).thenReturn(input);
        when(aiClient.analyze(any())).thenThrow(new AiAnalysisException("AI_TIMEOUT", 2, new RuntimeException()));
        when(safetyGuard.resolveSafetyLevel(input.content(), SafetyLevel.NORMAL)).thenReturn(SafetyLevel.NORMAL);
        when(fallbackFactory.create(input, SafetyLevel.NORMAL)).thenReturn(fallback);
        when(intensityPolicy.calculate(50, input.content(), input.selectedEmotion())).thenReturn(62);

        orchestrator.analyze(1L);

        verify(safetyGuard).validate(fallback);
        verify(stateService).fallback(1L, fallback, 62, false, 2, "AI_TIMEOUT", SafetyLevel.NORMAL);
    }

    private DiaryAiResult result(String model) {
        return new DiaryAiResult(
                "오늘의 마음을 견디느라 애썼어요.",
                "불안한 마음으로 오늘을 천천히 되돌아본 하루의 기록",
                50, List.of(), SafetyLevel.NORMAL, model
        );
    }
}
