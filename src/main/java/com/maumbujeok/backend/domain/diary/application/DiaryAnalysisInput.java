package com.maumbujeok.backend.domain.diary.application;

public record DiaryAnalysisInput(
        Long analysisId,
        long inputRevision,
        String content,
        String selectedEmotion
) {
    public DiaryAnalysisInput(Long analysisId, String content, String selectedEmotion) {
        this(analysisId, 1L, content, selectedEmotion);
    }
}
