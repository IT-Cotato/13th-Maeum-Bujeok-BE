package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import com.maumbujeok.backend.domain.diary.repository.DiaryAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DiaryAnalysisStateService {
    private final DiaryAnalysisRepository repository;

    @Transactional
    public boolean begin(Long analysisId) {
        DiaryAnalysis analysis = repository.findById(analysisId).orElse(null);
        if (analysis == null || analysis.getStatus() != DiaryAnalysisStatus.PENDING) return false;
        analysis.markProcessing();
        return true;
    }

    @Transactional
    public void complete(Long id, DiaryAiResult result, int score, boolean recommended, int attempts, SafetyLevel safety) {
        require(id).complete(result, score, recommended, attempts, safety);
    }

    @Transactional
    public void fallback(Long id, DiaryAiResult result, int score, boolean recommended, int attempts, String code, SafetyLevel safety) {
        require(id).completeWithFallback(result, score, recommended, attempts, code, safety);
    }

    @Transactional
    public void fail(Long id, int attempts, String code) {
        require(id).fail(attempts, code);
    }

    private DiaryAnalysis require(Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Analysis not found"));
    }
}
