package com.maumbujeok.backend.global.ai.dto;

public record AiExecutionResult<T>(
        T output,
        String model,
        String responseId,
        long inputTokens,
        long outputTokens,
        int attempts,
        long latencyMs
) {
}
