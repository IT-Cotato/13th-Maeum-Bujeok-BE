package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.domain.diary.ai.EmotionKeywordCandidate;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AiResponseSafetyGuard {
    private static final int SUMMARY_MIN_LENGTH = 10;
    private static final int SUMMARY_MAX_LENGTH = 40;
    private static final double MIN_KOREAN_LETTER_RATIO = 0.7;
    private static final List<String> FORBIDDEN_EXPRESSIONS = List.of(
            "너는 원래", "당신은 원래", "운명이다", "반드시 일어난다", "치료된다", "완치된다", "네 탓",
            "사주에 정해", "부적만 있으면", "살풀이만 하면"
    );
    private static final List<String> META_EXPRESSIONS = List.of(
            "요청하신 코드", "JSON 형식", "응답을 생성", "번역하면", "AI로서", "언어 모델로서",
            "지시사항에 따라", "답변 생성 과정"
    );
    private static final List<String> CRISIS_SIGNALS = List.of(
            "죽고 싶", "죽고싶", "자살", "자해", "목숨을 끊", "사라지고 싶", "해치고 싶"
    );

    public void validate(DiaryAiResult result) {
        if (result == null) {
            throw new IllegalArgumentException("Missing AI result");
        }
        if (!StringUtils.hasText(result.empathyResponse()) || result.empathyResponse().length() > 500) {
            throw new IllegalArgumentException("Invalid empathy response");
        }
        if (!isMostlyKorean(result.empathyResponse())) {
            throw new IllegalArgumentException("Empathy response must be Korean");
        }
        if (!StringUtils.hasText(result.summary())
                || result.summary().length() < SUMMARY_MIN_LENGTH
                || result.summary().length() > SUMMARY_MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid summary length");
        }
        if (!isMostlyKorean(result.summary()) || containsEastAsianForeignScript(result.summary())) {
            throw new IllegalArgumentException("Summary must be Korean");
        }
        if (result.negativeIntensity() < 0 || result.negativeIntensity() > 100) {
            throw new IllegalArgumentException("Invalid negative intensity");
        }
        if (result.safetyLevel() == null || !StringUtils.hasText(result.modelName())) {
            throw new IllegalArgumentException("Missing structured output field");
        }
        if (result.reportEmotion() == null) {
            throw new IllegalArgumentException("Missing report emotion");
        }
        if (result.emotionKeywords() == null
                || result.emotionKeywords().size() > 5
                || result.emotionKeywords().stream().anyMatch(this::invalidKeyword)) {
            throw new IllegalArgumentException("Invalid emotion keyword");
        }
        String output = result.empathyResponse() + " " + result.summary();
        if (FORBIDDEN_EXPRESSIONS.stream().anyMatch(output::contains)) {
            throw new IllegalArgumentException("Unsafe expression");
        }
        if (META_EXPRESSIONS.stream().anyMatch(output::contains)) {
            throw new IllegalArgumentException("AI meta expression");
        }
    }

    public SafetyLevel resolveSafetyLevel(String diaryContent, SafetyLevel providerLevel) {
        if (CRISIS_SIGNALS.stream().anyMatch(diaryContent::contains)) return SafetyLevel.CRISIS;
        return providerLevel;
    }

    private boolean invalidKeyword(EmotionKeywordCandidate keyword) {
        return keyword == null
                || !StringUtils.hasText(keyword.keyword())
                || keyword.score() < 0
                || keyword.score() > 1;
    }

    private boolean isMostlyKorean(String value) {
        int letterCount = 0;
        int koreanLetterCount = 0;
        for (int index = 0; index < value.length();) {
            int codePoint = value.codePointAt(index);
            index += Character.charCount(codePoint);
            if (!Character.isLetter(codePoint)) continue;
            letterCount++;
            if (Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HANGUL) {
                koreanLetterCount++;
            }
        }
        return letterCount > 0 && (double) koreanLetterCount / letterCount >= MIN_KOREAN_LETTER_RATIO;
    }

    private boolean containsEastAsianForeignScript(String value) {
        for (int index = 0; index < value.length();) {
            int codePoint = value.codePointAt(index);
            index += Character.charCount(codePoint);
            Character.UnicodeScript script = Character.UnicodeScript.of(codePoint);
            if (script == Character.UnicodeScript.HAN
                    || script == Character.UnicodeScript.HIRAGANA
                    || script == Character.UnicodeScript.KATAKANA) {
                return true;
            }
        }
        return false;
    }
}
