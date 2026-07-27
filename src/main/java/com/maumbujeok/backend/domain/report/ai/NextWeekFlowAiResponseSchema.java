package com.maumbujeok.backend.domain.report.ai;

import java.util.List;
import java.util.Map;

public final class NextWeekFlowAiResponseSchema {
    private NextWeekFlowAiResponseSchema() {
    }

    public static Map<String, Object> schema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "adviceText", Map.of("type", "string", "minLength", 50, "maxLength", 1500)
                ),
                "required", List.of("adviceText")
        );
    }
}
