package com.maumbujeok.backend.domain.report.application;

import java.time.LocalDate;

public record WeeklyDiaryEntry(
        LocalDate recordedDate,
        String selectedEmotionCode,
        String selectedEmotionLabel,
        String content
) {
}
