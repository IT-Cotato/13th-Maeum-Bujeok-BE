package com.maumbujeok.backend.domain.diary.ai;

import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import java.util.List;

public record DiaryAiResponse(
        String empathyResponse,
        String summary,
        int negativeIntensity,
        List<EmotionKeywordCandidate> emotionKeywords,
        String reportEmotion,
        SafetyLevel safetyLevel
) {
    public DiaryAiResult toResult(String model) {
        return new DiaryAiResult(
                empathyResponse,
                summary,
                negativeIntensity,
                emotionKeywords,
                ReportEmotion.fromLabel(reportEmotion),
                safetyLevel,
                model
        );
    }
}
