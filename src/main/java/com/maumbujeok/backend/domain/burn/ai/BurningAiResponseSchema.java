package com.maumbujeok.backend.domain.burn.ai;

import java.util.List;
import java.util.Map;

public final class BurningAiResponseSchema {
    private BurningAiResponseSchema() {
    }

    public static Map<String, Object> schema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "comment", Map.of("type", "string", "minLength", 1, "maxLength", 500),
                        "talismanType", Map.of("type", "integer", "minimum", 1, "maximum", 13),
                        "talismanText", Map.of("type", "string", "minLength", 4, "maxLength", 4)
                ),
                "required", List.of("comment", "talismanType", "talismanText")
        );
    }
}