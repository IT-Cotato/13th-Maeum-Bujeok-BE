package com.maumbujeok.backend.domain.home.ai;

import java.util.List;
import java.util.Map;

public final class HomeSummaryAiResponseSchema {
    private HomeSummaryAiResponseSchema() {
    }

    public static Map<String, Object> schema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "primaryElement", Map.of(
                                "type", "string",
                                "enum", List.of("WOOD", "FIRE", "EARTH", "METAL", "WATER")
                        ),
                        "todayLuck", Map.of("type", "string", "minLength", 10, "maxLength", 300),
                        "todayEnergy", Map.of("type", "string", "minLength", 20, "maxLength", 600)
                ),
                "required", List.of("primaryElement", "todayLuck", "todayEnergy")
        );
    }
}
