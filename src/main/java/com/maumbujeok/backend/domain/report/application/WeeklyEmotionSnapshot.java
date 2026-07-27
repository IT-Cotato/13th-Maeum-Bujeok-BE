package com.maumbujeok.backend.domain.report.application;

import java.util.List;

public record WeeklyEmotionSnapshot(
        int totalDiaryCount,
        double positiveRatio,
        double neutralRatio,
        double negativeRatio,
        String dominantEmotionLabel,
        List<WeeklyEmotionCount> emotionCounts
) {
}
