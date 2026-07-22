package com.maumbujeok.backend.domain.diary.dto;

import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import java.time.format.DateTimeFormatter;

public record DiaryAnalysisResponse(
        DiaryAnalysisStatus status,
        String summary,
        String empathyResponse,
        Integer negativeIntensity,
        String reportEmotion,
        Boolean salpuriRecommended,
        SafetyLevel safetyLevel,
        Long amuletId,
        String amuletType,
        String title,
        String createdAt,
        String modelName,
        String failureCode,
        int attemptCount
) {
    private static final DateTimeFormatter AMULET_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static DiaryAnalysisResponse from(DiaryAnalysis analysis) {
        return new DiaryAnalysisResponse(
                analysis.getStatus(),
                analysis.getSummary(),
                analysis.getEmpathyResponse(),
                analysis.getFinalNegativeIntensity(),
                analysis.getReportEmotion() == null ? null : analysis.getReportEmotion().getLabel(),
                analysis.getSalpuriRecommended(),
                analysis.getSafetyLevel(),
                null,
                null,
                null,
                analysis.getCreatedAt().format(AMULET_DATE_FORMAT),
                analysis.getModelName(),
                analysis.getFailureCode(),
                analysis.getAttemptCount()
        );
    }
}
