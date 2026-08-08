package com.maumbujeok.backend.domain.saju.ai;

public record SajuAiCallResult(
        SajuAiResult result,
        int attempts
) {
}
