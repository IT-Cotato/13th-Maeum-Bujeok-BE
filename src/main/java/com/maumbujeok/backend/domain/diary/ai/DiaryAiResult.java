package com.maumbujeok.backend.domain.diary.ai;

import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import java.util.List;

public record DiaryAiResult(
        String empathyResponse,
        String summary,
        int negativeIntensity,
        List<EmotionKeywordCandidate> emotionKeywords,
        ReportEmotion reportEmotion,
        SafetyLevel safetyLevel,
        String modelName
) {
    public DiaryAiResult {
        emotionKeywords = emotionKeywords == null ? List.of() : List.copyOf(emotionKeywords);
    }
}
