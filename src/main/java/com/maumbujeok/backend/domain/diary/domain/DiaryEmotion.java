package com.maumbujeok.backend.domain.diary.domain;

import java.util.Arrays;
import java.util.List;

public enum DiaryEmotion {
    JOYFUL("기뻐요", "기쁨"),
    HAPPY("행복해요", "행복"),
    EXCITED("즐거워요", "즐거움"),
    COMFORTABLE("편안해요", "편안"),
    NORMAL("보통이에요", "보통"),
    LETHARGIC("무기력해요", "무기력"),
    SAD("슬퍼요", "슬픔"),
    ANXIOUS("불안해요", "불안"),
    ANGRY("화나요", "분노");

    private final String label;
    private final String analysisValue;

    DiaryEmotion(String label, String analysisValue) {
        this.label = label;
        this.analysisValue = analysisValue;
    }

    public String getLabel() {
        return label;
    }

    public String getAnalysisValue() {
        return analysisValue;
    }

    public static DiaryEmotion fromInput(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Emotion is required");
        }

        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(emotion -> emotion.name().equalsIgnoreCase(normalized)
                        || emotion.label.equals(normalized)
                        || emotion.analysisValue.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported emotion: " + normalized));
    }

    public static List<String> codes() {
        return Arrays.stream(values()).map(Enum::name).toList();
    }
}
