package com.maumbujeok.backend.domain.saju.ai;

import java.util.List;
import java.util.Map;

public final class SajuAiResponseSchema {
    private SajuAiResponseSchema() {
    }

    public static Map<String, Object> schema() {
        Map<String, Object> percentageField = Map.of(
                "type", "integer",
                "minimum", 0,
                "maximum", 100
        );

        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "woodPercentage", percentageField,
                        "firePercentage", percentageField,
                        "earthPercentage", percentageField,
                        "metalPercentage", percentageField,
                        "waterPercentage", percentageField
                ),
                "required", List.of(
                        "woodPercentage",
                        "firePercentage",
                        "earthPercentage",
                        "metalPercentage",
                        "waterPercentage"
                )
        );
    }
}
