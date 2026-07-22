package com.maumbujeok.backend.global.ai.dto;

import java.util.Map;

public record AiStructuredRequest(
        String taskName,
        String instructions,
        String input,
        String schemaName,
        Map<String, Object> schema,
        String modelOverride,
        int maxOutputTokens,
        String promptVersion
) {
    public AiStructuredRequest {
        schema = Map.copyOf(schema);
        if (maxOutputTokens < 1) throw new IllegalArgumentException("maxOutputTokens must be positive");
    }
}
