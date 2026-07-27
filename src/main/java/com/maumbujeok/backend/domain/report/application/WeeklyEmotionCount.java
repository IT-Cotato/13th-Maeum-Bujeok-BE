package com.maumbujeok.backend.domain.report.application;

public record WeeklyEmotionCount(
        String emotionCode,
        String emotionLabel,
        int count,
        double ratio,
        String polarity
) {
}
