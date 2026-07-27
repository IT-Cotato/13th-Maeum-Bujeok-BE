package com.maumbujeok.backend.domain.diary.application;

public class StaleDiaryAnalysisException extends RuntimeException {
    public StaleDiaryAnalysisException(Long analysisId, long inputRevision) {
        super("Stale diary analysis input: analysisId=" + analysisId + ", inputRevision=" + inputRevision);
    }
}
