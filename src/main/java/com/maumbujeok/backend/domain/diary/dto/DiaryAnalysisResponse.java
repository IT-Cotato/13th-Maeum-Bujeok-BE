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
        Boolean salpuriRecommended,
        SafetyLevel safetyLevel,
        Long amuletId,
        String amuletType,
        String title,
        String createdAt
) {
    private static final long TEST_AMULET_ID = 0L;
    private static final String TEST_AMULET_TYPE = "friendship";
    private static final String TEST_AMULET_TITLE = "테스트 응답";
    private static final DateTimeFormatter AMULET_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static DiaryAnalysisResponse from(DiaryAnalysis analysis) {
        return new DiaryAnalysisResponse(
                analysis.getStatus(),
                analysis.getSummary(),
                analysis.getEmpathyResponse(),
                analysis.getFinalNegativeIntensity(),
                analysis.getSalpuriRecommended(),
                analysis.getSafetyLevel(),
                TEST_AMULET_ID,
                TEST_AMULET_TYPE,
                TEST_AMULET_TITLE,
                analysis.getCreatedAt().format(AMULET_DATE_FORMAT)
        );
    }
}
