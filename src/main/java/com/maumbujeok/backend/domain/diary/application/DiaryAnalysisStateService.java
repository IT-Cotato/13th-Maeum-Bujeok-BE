package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
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
    public boolean begin(Long analysisId, long inputRevision) {
        DiaryAnalysis analysis = repository.findByIdForUpdate(analysisId).orElse(null);
        return analysis != null && analysis.markProcessing(inputRevision);
    }

    @Transactional
    public boolean complete(
            Long id,
            long inputRevision,
            DiaryAiResult result,
            int score,
            boolean recommended,
            int attempts,
            SafetyLevel safety
    ) {
        DiaryAnalysis analysis = repository.findByIdForUpdate(id).orElse(null);
        return analysis != null && analysis.complete(inputRevision, result, score, recommended, attempts, safety);
    }

    @Transactional
    public boolean fallback(
            Long id,
            long inputRevision,
            DiaryAiResult result,
            int score,
            boolean recommended,
            int attempts,
            String code,
            SafetyLevel safety
    ) {
        DiaryAnalysis analysis = repository.findByIdForUpdate(id).orElse(null);
        return analysis != null
                && analysis.completeWithFallback(inputRevision, result, score, recommended, attempts, code, safety);
    }

    @Transactional
    public boolean fail(Long id, long inputRevision, int attempts, String code) {
        DiaryAnalysis analysis = repository.findByIdForUpdate(id).orElse(null);
        return analysis != null && analysis.fail(inputRevision, attempts, code);
    }
}
