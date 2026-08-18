package com.maumbujeok.backend.domain.burn.application;

import com.maumbujeok.backend.domain.burn.ai.BurningAiResult;
import com.maumbujeok.backend.domain.burn.domain.BurningAnalysis;
import com.maumbujeok.backend.domain.burn.repository.BurningAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BurningAnalysisStateService {
    private final BurningAnalysisRepository repository;

    @Transactional
    public boolean begin(Long id, int revision) {
        BurningAnalysis analysis = repository.findByIdForUpdate(id).orElse(null);
        return analysis != null && analysis.markProcessing(revision);
    }

    @Transactional(readOnly = true)
    public BurningAnalysis find(Long id) {
        return repository.findById(id).orElseThrow();
    }

    @Transactional
    public boolean complete(Long id, int revision, BurningAiResult result) {
        BurningAnalysis analysis = repository.findByIdForUpdate(id).orElse(null);
        return analysis != null && analysis.complete(revision, result);
    }

    @Transactional
    public boolean completeWithFallback(Long id, int revision, BurningAiResult result, String failureCode) {
        BurningAnalysis analysis = repository.findByIdForUpdate(id).orElse(null);
        return analysis != null && analysis.completeWithFallback(revision, result, failureCode);
    }
}