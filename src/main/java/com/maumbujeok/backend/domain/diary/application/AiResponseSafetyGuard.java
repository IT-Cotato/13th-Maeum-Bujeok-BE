package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.domain.diary.ai.EmotionKeywordCandidate;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AiResponseSafetyGuard {
    private static final List<String> FORBIDDEN_EXPRESSIONS = List.of(
            "너는 원래", "당신은 원래", "운명이다", "반드시 일어난다", "치료된다", "완치된다", "네 탓"
    );
    private static final List<String> CRISIS_SIGNALS = List.of("죽고 싶", "자살", "자해");

    public void validate(DiaryAiResult result) {
        if (!StringUtils.hasText(result.empathyResponse()) || result.empathyResponse().length() > 500) {
            throw new IllegalArgumentException("Invalid empathy response");
        }
        if (!StringUtils.hasText(result.summary()) || result.summary().length() < 25 || result.summary().length() > 40) {
            throw new IllegalArgumentException("Invalid summary length");
        }
        if (result.negativeIntensity() < 0 || result.negativeIntensity() > 100) {
            throw new IllegalArgumentException("Invalid negative intensity");
        }
        if (result.safetyLevel() == null || !StringUtils.hasText(result.modelName())) {
            throw new IllegalArgumentException("Missing structured output field");
        }
        if (result.emotionKeywords().size() > 5 || result.emotionKeywords().stream().anyMatch(this::invalidKeyword)) {
            throw new IllegalArgumentException("Invalid emotion keyword");
        }
        String output = result.empathyResponse() + " " + result.summary();
        if (FORBIDDEN_EXPRESSIONS.stream().anyMatch(output::contains)) {
            throw new IllegalArgumentException("Unsafe expression");
        }
    }

    public SafetyLevel resolveSafetyLevel(String diaryContent, SafetyLevel providerLevel) {
        if (CRISIS_SIGNALS.stream().anyMatch(diaryContent::contains)) return SafetyLevel.CRISIS;
        return providerLevel;
    }

    private boolean invalidKeyword(EmotionKeywordCandidate keyword) {
        return !StringUtils.hasText(keyword.keyword()) || keyword.score() < 0 || keyword.score() > 1;
    }
}
