package com.maumbujeok.backend.domain.diary.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maumbujeok.backend.domain.diary.ai.AiAnalysisException;
import com.maumbujeok.backend.domain.diary.ai.AiCallResult;
import com.maumbujeok.backend.domain.diary.ai.AiDiaryAnalysisClient;
import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiaryAnalysisOrchestratorTest {
    private static final long REVISION = 2L;

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
                stateService,
                inputLoader,
                sajuContextProvider,
                aiClient,
                intensityPolicy,
                safetyGuard,
                fallbackFactory
        );
    }

    @Test
    void skipsAnalysisThatIsNotCurrentPendingRevision() {
        when(stateService.begin(1L, REVISION)).thenReturn(false);

        orchestrator.analyze(1L, REVISION);

        verify(inputLoader, never()).load(any(), any(Long.class));
        verify(aiClient, never()).analyze(any());
    }

    @Test
    void savesReviewedFallbackWhenProviderFails() {
        DiaryAnalysisInput input = input("오늘 너무 불안하고 힘들었다", "불안");
        DiaryAiResult fallback = result("fallback-rule-v1");
        when(stateService.begin(1L, REVISION)).thenReturn(true);
        when(inputLoader.load(1L, REVISION)).thenReturn(input);
        when(aiClient.analyze(any()))
                .thenThrow(new AiAnalysisException("AI_TIMEOUT", 2, new RuntimeException()));
        when(safetyGuard.resolveSafetyLevel(input.content(), SafetyLevel.NORMAL))
                .thenReturn(SafetyLevel.NORMAL);
        when(fallbackFactory.create(input, SafetyLevel.NORMAL)).thenReturn(fallback);
        when(intensityPolicy.calculate(50, input.content(), input.selectedEmotion())).thenReturn(62);

        orchestrator.analyze(1L, REVISION);

        verify(safetyGuard).validate(fallback);
        verify(stateService).fallback(
                1L,
                REVISION,
                fallback,
                62,
                false,
                2,
                "AI_TIMEOUT",
                SafetyLevel.NORMAL
        );
    }

    @Test
    void retriesOnceWhenAiOutputFailsQualityValidation() {
        DiaryAnalysisInput input = input("답장이 오지 않아 서운하고 슬펐다", "슬픔");
        DiaryAiResult contaminated = result("gpt-test");
        DiaryAiResult corrected = new DiaryAiResult(
                "기다리던 답이 없어 서운하고 슬펐겠어요.",
                "응답을 기다리며 서운하고 슬펐던 하루",
                32,
                List.of(),
                ReportEmotion.SADNESS,
                SafetyLevel.NORMAL,
                "gpt-test"
        );
        when(stateService.begin(1L, REVISION)).thenReturn(true);
        when(inputLoader.load(1L, REVISION)).thenReturn(input);
        when(aiClient.analyze(any()))
                .thenReturn(new AiCallResult(contaminated, 1))
                .thenReturn(new AiCallResult(corrected, 1));
        doThrow(new IllegalArgumentException("Summary must be Korean"))
                .doNothing()
                .when(safetyGuard).validate(any());
        when(safetyGuard.resolveSafetyLevel(input.content(), SafetyLevel.NORMAL))
                .thenReturn(SafetyLevel.NORMAL);
        when(intensityPolicy.calculate(32, input.content(), input.selectedEmotion())).thenReturn(32);

        orchestrator.analyze(1L, REVISION);

        verify(aiClient, times(2)).analyze(any());
        verify(stateService).complete(
                1L,
                REVISION,
                corrected,
                32,
                false,
                2,
                SafetyLevel.NORMAL
        );
        verify(stateService, never()).fallback(
                any(),
                any(Long.class),
                any(),
                anyInt(),
                anyBoolean(),
                anyInt(),
                any(),
                any()
        );
    }

    @Test
    void passesSajuContextToDiaryAiRequest() {
        DiaryAnalysisInput input = new DiaryAnalysisInput(1L, REVISION, "01000000204", "오늘은 마음이 무거웠다", "슬픔");
        DiaryAiResult result = result("gpt-test");
        when(stateService.begin(1L, REVISION)).thenReturn(true);
        when(inputLoader.load(1L, REVISION)).thenReturn(input);
        when(sajuContextProvider.getContext("01000000204")).thenReturn("사주 균형 참고 정보: 목 18%, 화 22%, 토 20%, 금 17%, 수 23%. 상대적으로 두드러진 기운은 수(水) 23%입니다.");
        when(aiClient.analyze(any())).thenReturn(new AiCallResult(result, 1));
        when(safetyGuard.resolveSafetyLevel(input.content(), SafetyLevel.NORMAL)).thenReturn(SafetyLevel.NORMAL);
        when(intensityPolicy.calculate(50, input.content(), input.selectedEmotion())).thenReturn(50);

        orchestrator.analyze(1L, REVISION);

        verify(aiClient).analyze(argThat(request -> request.sajuContext().contains("수(水) 23%")));
    }

    @Test
    void usesFallbackWhenBothAiOutputsFailQualityValidation() {
        DiaryAnalysisInput input = input("답장이 오지 않아 서운하고 슬펐다", "슬픔");
        DiaryAiResult rejected = result("gpt-test");
        DiaryAiResult fallback = result("fallback-rule-v1");
        when(stateService.begin(1L, REVISION)).thenReturn(true);
        when(inputLoader.load(1L, REVISION)).thenReturn(input);
        when(aiClient.analyze(any())).thenReturn(new AiCallResult(rejected, 1));
        doThrow(new IllegalArgumentException("Summary must be Korean"))
                .doThrow(new IllegalArgumentException("AI meta expression"))
                .doNothing()
                .when(safetyGuard).validate(any());
        when(safetyGuard.resolveSafetyLevel(input.content(), SafetyLevel.NORMAL))
                .thenReturn(SafetyLevel.NORMAL);
        when(fallbackFactory.create(input, SafetyLevel.NORMAL)).thenReturn(fallback);
        when(intensityPolicy.calculate(50, input.content(), input.selectedEmotion())).thenReturn(50);

        orchestrator.analyze(1L, REVISION);

        verify(aiClient, times(2)).analyze(any());
        verify(stateService).fallback(
                1L,
                REVISION,
                fallback,
                50,
                false,
                2,
                "AI_OUTPUT_REJECTED",
                SafetyLevel.NORMAL
        );
    }

    @Test
    void ignoresResultWhenRevisionChangedDuringAiCall() {
        DiaryAnalysisInput input = input("오늘은 불안했다", "불안");
        DiaryAiResult result = result("gpt-test");
        when(stateService.begin(1L, REVISION)).thenReturn(true);
        when(inputLoader.load(1L, REVISION)).thenReturn(input);
        when(aiClient.analyze(any())).thenReturn(new AiCallResult(result, 1));
        when(safetyGuard.resolveSafetyLevel(input.content(), SafetyLevel.NORMAL))
                .thenReturn(SafetyLevel.NORMAL);
        when(intensityPolicy.calculate(50, input.content(), input.selectedEmotion())).thenReturn(50);
        when(stateService.complete(
                eq(1L),
                eq(REVISION),
                eq(result),
                anyInt(),
                anyBoolean(),
                anyInt(),
                eq(SafetyLevel.NORMAL)
        )).thenReturn(false);

        orchestrator.analyze(1L, REVISION);

        verify(stateService, never()).fallback(
                any(),
                any(Long.class),
                any(),
                anyInt(),
                anyBoolean(),
                anyInt(),
                any(),
                any()
        );
    }

    private DiaryAnalysisInput input(String content, String emotion) {
        return new DiaryAnalysisInput(1L, REVISION, content, emotion);
    }

    private DiaryAiResult result(String model) {
        return new DiaryAiResult(
                "오늘의 마음을 견디느라 애썼어요.",
                "불안한 마음으로 오늘을 천천히 되돌아본 하루의 기록",
                50,
                List.of(),
                ReportEmotion.ANXIETY,
                SafetyLevel.NORMAL,
                model
        );
    }
}
