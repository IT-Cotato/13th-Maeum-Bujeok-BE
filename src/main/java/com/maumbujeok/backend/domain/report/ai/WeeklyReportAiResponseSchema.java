package com.maumbujeok.backend.domain.report.ai;

import java.util.List;
import java.util.Map;

public final class WeeklyReportAiResponseSchema {
    private WeeklyReportAiResponseSchema() {
    }

    public static Map<String, Object> schema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "insightSummary", Map.of("type", "string", "minLength", 80, "maxLength", 700)
                ),
                "required", List.of("insightSummary")
        );
    }
}
