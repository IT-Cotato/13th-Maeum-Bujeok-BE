package com.maumbujeok.backend.domain.diary.ai;

import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import java.util.List;
import java.util.Map;

public final class DiaryAiResponseSchema {
    private DiaryAiResponseSchema() {
    }

    public static Map<String, Object> schema() {
        Map<String, Object> keyword = Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "keyword", Map.of("type", "string", "minLength", 1, "maxLength", 30),
                        "score", Map.of("type", "number", "minimum", 0, "maximum", 1)
                ),
                "required", List.of("keyword", "score")
        );
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "title", Map.of("type", "string", "minLength", 1, "maxLength", 100),
                        "empathyResponse", Map.of("type", "string", "minLength", 1, "maxLength", 500),
                        "summary", Map.of("type", "string", "minLength", 10, "maxLength", 40),
                        "negativeIntensity", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                        "emotionKeywords", Map.of("type", "array", "maxItems", 5, "items", keyword),
                        "reportEmotion", Map.of("type", "string", "enum", ReportEmotion.labels()),
                        "safetyLevel", Map.of("type", "string", "enum", List.of("NORMAL", "CAUTION", "CRISIS"))
                ),
                "required", List.of(
                        "title", "empathyResponse", "summary", "negativeIntensity", "emotionKeywords", "reportEmotion", "safetyLevel"
                )
        );
    }
}
