package com.maumbujeok.backend.domain.diary.application;

public record DiaryAnalysisRequestedEvent(Long analysisId, long inputRevision) {
}
