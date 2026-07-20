package com.maumbujeok.backend.domain.diary.ai;

import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import java.util.List;

public record DiaryAiResult(
        String empathyResponse,
        String summary,
        int negativeIntensity,
        List<EmotionKeywordCandidate> emotionKeywords,
        SafetyLevel safetyLevel,
        String modelName
) {
    public DiaryAiResult {
        emotionKeywords = emotionKeywords == null ? List.of() : List.copyOf(emotionKeywords);
    }
}
