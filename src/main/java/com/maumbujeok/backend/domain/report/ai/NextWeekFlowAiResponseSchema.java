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
                        "title", Map.of("type", "string", "minLength", 5, "maxLength", 100),
                        "content", Map.of("type", "string", "minLength", 50, "maxLength", 1500),
                        "highlight", Map.of("type", "string", "minLength", 2, "maxLength", 100)
                ),
                "required", List.of("title", "content", "highlight")
        );
    }
}
