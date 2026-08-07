package com.maumbujeok.backend.domain.saju.ai;

public record SajuAiResponse(
        int woodPercentage,
        int firePercentage,
        int earthPercentage,
        int metalPercentage,
        int waterPercentage
) {
    public SajuAiResult toResult(String model) {
        return new SajuAiResult(
                woodPercentage,
                firePercentage,
                earthPercentage,
                metalPercentage,
                waterPercentage,
                model
        );
    }
}
