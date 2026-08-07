package com.maumbujeok.backend.domain.saju.application;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.saju.ai.SajuAiCallResult;
import com.maumbujeok.backend.domain.saju.ai.SajuAiClient;
import com.maumbujeok.backend.domain.saju.ai.SajuAiException;
import com.maumbujeok.backend.domain.saju.ai.SajuAiResult;
import com.maumbujeok.backend.domain.saju.ai.SajuAiPromptVersion;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysisStatus;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import java.time.LocalTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SajuAnalysisOrchestratorTest {

    @Mock SajuAnalysisRepository repository;
    @Mock SajuAiClient aiClient;
    @Mock SajuAnalysisStateService stateService;

    private SajuAnalysisOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new SajuAnalysisOrchestrator(repository, aiClient, stateService);
    }

    @Test
    void skipsMissingAnalysis() {
        when(stateService.begin(1L, 1L)).thenReturn(false);

        orchestrator.analyze(1L, 1L);

        verify(aiClient, never()).analyze(any());
        verify(repository, never()).findById(1L);
        verify(stateService, never()).complete(any(), anyLong(), any(), org.mockito.ArgumentMatchers.anyInt());
        verify(stateService, never()).fail(any(), anyLong(), org.mockito.ArgumentMatchers.anyInt(), any());
    }

    @Test
    void skipsAnalysisMissingAfterClaim() {
        when(stateService.begin(1L, 1L)).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.empty());

        orchestrator.analyze(1L, 1L);

        verify(aiClient, never()).analyze(any());
        verify(stateService, never()).complete(any(), anyLong(), any(), org.mockito.ArgumentMatchers.anyInt());
        verify(stateService, never()).fail(any(), anyLong(), org.mockito.ArgumentMatchers.anyInt(), any());
    }

    @Test
    void completesWhenAiSucceeds() {
        SajuAnalysis processing = analysis(SajuAnalysisStatus.PROCESSING);
        SajuAiResult result = new SajuAiResult(20, 20, 20, 20, 20, "gpt-test");
        when(stateService.begin(1L, 1L)).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(processing));
        when(aiClient.analyze(any())).thenReturn(new SajuAiCallResult(result, 2));

        orchestrator.analyze(1L, 1L);

        verify(stateService).complete(1L, 1L, result, 2);
        verify(stateService, never()).fail(any(), anyLong(), org.mockito.ArgumentMatchers.anyInt(), any());
    }

    @Test
    void storesProviderFailureCodeWhenAiThrowsKnownException() {
        SajuAnalysis processing = analysis(SajuAnalysisStatus.PROCESSING);
        when(stateService.begin(1L, 1L)).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(processing));
        when(aiClient.analyze(any())).thenThrow(new SajuAiException("AI_TIMEOUT", 3, new RuntimeException("timeout")));

        orchestrator.analyze(1L, 1L);

        verify(stateService).fail(1L, 1L, 3, "AI_TIMEOUT");
    }

    @Test
    void storesInvalidResponseWhenAiOutputIsRejected() {
        SajuAnalysis processing = analysis(SajuAnalysisStatus.PROCESSING);
        when(stateService.begin(1L, 1L)).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(processing));
        when(aiClient.analyze(any())).thenThrow(new IllegalArgumentException("sum must be 100"));

        orchestrator.analyze(1L, 1L);

        verify(stateService).fail(1L, 1L, 1, "AI_INVALID_RESPONSE");
    }

    @Test
    void storesProviderErrorWhenUnexpectedRuntimeExceptionOccurs() {
        SajuAnalysis processing = analysis(SajuAnalysisStatus.PROCESSING);
        when(stateService.begin(1L, 1L)).thenReturn(true);
        when(repository.findById(1L)).thenReturn(Optional.of(processing));
        when(aiClient.analyze(any())).thenThrow(new RuntimeException("network down"));

        orchestrator.analyze(1L, 1L);

        verify(stateService).fail(1L, 1L, 1, "AI_PROVIDER_ERROR");
    }

    private SajuAnalysis analysis(SajuAnalysisStatus status) {
        SajuAnalysis analysis = new SajuAnalysis(
                Member.builder()
                        .phoneNumber("01030000001")
                        .name("테스트")
                        .provider(Member.Provider.LOCAL)
                        .role(Member.Role.ROLE_USER)
                        .birthDate("19900101")
                        .build(),
                "19900101",
                MemberSajuProfile.Gender.MALE,
                MemberSajuProfile.CalendarType.SOLAR,
                LocalTime.of(8, 0),
                SajuAiPromptVersion.VALUE
        );
        if (status == SajuAnalysisStatus.PROCESSING) {
            analysis.markProcessing(1L);
        }
        return analysis;
    }
}
