package com.maumbujeok.backend.domain.report.application;

import java.time.LocalDate;
import java.util.List;

public record WeeklyReportGenerationInput(
        Long reportId,
        int generationSequence,
        String memberDisplayName,
        LocalDate periodStart,
        LocalDate periodEnd,
        List<WeeklyDiaryEntry> diaryEntries,
        WeeklyEmotionSnapshot emotionSnapshot
) {
}
