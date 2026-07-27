package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.repository.DiaryAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DiaryAnalysisInputLoader {
    private final DiaryAnalysisRepository analysisRepository;

    @Transactional(readOnly = true)
    public DiaryAnalysisInput load(Long analysisId, long inputRevision) {
        DiaryAnalysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found"));
        if (analysis.getInputRevision() != inputRevision) {
            throw new StaleDiaryAnalysisException(analysisId, inputRevision);
        }
        return new DiaryAnalysisInput(
                analysisId,
                inputRevision,
                analysis.getDiary().getContent(),
                analysis.getDiary().getSelectedEmotion().getAnalysisValue()
        );
    }
}
