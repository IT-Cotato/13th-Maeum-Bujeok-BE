package com.maumbujeok.backend.domain.report.ai;

import com.maumbujeok.backend.domain.report.application.WeeklyDiaryEntry;
import com.maumbujeok.backend.domain.report.application.WeeklyEmotionSnapshot;
import java.time.LocalDate;
import java.util.List;

public record WeeklyReportAiRequest(
        String memberDisplayName,
        LocalDate periodStart,
        LocalDate periodEnd,
        List<WeeklyDiaryEntry> diaryEntries,
        WeeklyEmotionSnapshot emotionSnapshot
) {
}
