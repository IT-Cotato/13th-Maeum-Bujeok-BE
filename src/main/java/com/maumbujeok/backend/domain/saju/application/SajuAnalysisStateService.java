package com.maumbujeok.backend.domain.saju.application;

import com.maumbujeok.backend.domain.saju.ai.SajuAiResult;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SajuAnalysisStateService {
    private final SajuAnalysisRepository repository;

    @Transactional
    public boolean begin(Long analysisId, long requestSequence) {
        SajuAnalysis analysis = repository.findByIdForUpdate(analysisId).orElse(null);
        return analysis != null && analysis.markProcessing(requestSequence);
    }

    @Transactional
    public boolean complete(Long analysisId, long requestSequence, SajuAiResult result, int attempts) {
        SajuAnalysis analysis = repository.findByIdForUpdate(analysisId).orElse(null);
        return analysis != null && analysis.complete(requestSequence, result, attempts);
    }

    @Transactional
    public boolean fail(Long analysisId, long requestSequence, int attempts, String code) {
        SajuAnalysis analysis = repository.findByIdForUpdate(analysisId).orElse(null);
        return analysis != null && analysis.fail(requestSequence, attempts, code);
    }
}
