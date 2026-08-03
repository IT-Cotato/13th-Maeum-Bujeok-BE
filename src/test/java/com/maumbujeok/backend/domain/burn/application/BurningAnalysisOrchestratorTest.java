package com.maumbujeok.backend.domain.burn.application;

import com.maumbujeok.backend.domain.burn.ai.*;
import com.maumbujeok.backend.domain.burn.domain.*;
import com.maumbujeok.backend.domain.burn.repository.BurningAnalysisRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BurningAnalysisOrchestratorTest {
    @Mock BurningAnalysisRepository repository;
    @Mock BurningAiClient client;
    @Mock BurningFallbackFactory fallbackFactory;
    @InjectMocks BurningAnalysisOrchestrator orchestrator;

    @Test
    void analyzesPendingRecordAndPersistsCompletedResult() {
        BurningAnalysis analysis = analysis();
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(analysis));
        when(repository.findById(7L)).thenReturn(Optional.of(analysis));
        when(client.analyze(any(BurningAiRequest.class))).thenReturn(new BurningAiResult("comment", "type", "CALM", "fake"));

        orchestrator.analyze(7L, 1);

        org.junit.jupiter.api.Assertions.assertEquals(BurningAnalysisStatus.COMPLETED, analysis.getStatus());
        verify(client).analyze(any(BurningAiRequest.class));
        verifyNoInteractions(fallbackFactory);
    }

    @Test
    void usesLocalFallbackAfterSingleAiFailure() {
        BurningAnalysis analysis = analysis();
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(analysis));
        when(repository.findById(7L)).thenReturn(Optional.of(analysis));
        when(client.analyze(any(BurningAiRequest.class))).thenThrow(new RuntimeException("AI_DOWN"));
        when(fallbackFactory.create(any())).thenReturn(new BurningAiResult("fallback", "type", "CALM", "fallback"));

        orchestrator.analyze(7L, 1);

        org.junit.jupiter.api.Assertions.assertEquals(BurningAnalysisStatus.FALLBACK_COMPLETED, analysis.getStatus());
        org.junit.jupiter.api.Assertions.assertEquals("fallback", analysis.getComment());
        verify(client, times(1)).analyze(any(BurningAiRequest.class));
        verify(fallbackFactory, times(1)).create(any());
    }

    @Test
    void ignoresDuplicateEventAfterFirstProcessing() {
        BurningAnalysis analysis = analysis();
        analysis.markProcessing(1);
        when(repository.findByIdForUpdate(7L)).thenReturn(Optional.of(analysis));

        orchestrator.analyze(7L, 1);

        verifyNoInteractions(client, fallbackFactory);
    }

    private BurningAnalysis analysis() {
        Member member = Member.builder().phoneNumber("01000000000").build();
        Burning burning = new Burning(member, BurningSourceType.DIRECT, null, "source", LocalDateTime.now());
        return new BurningAnalysis(burning);
    }
}