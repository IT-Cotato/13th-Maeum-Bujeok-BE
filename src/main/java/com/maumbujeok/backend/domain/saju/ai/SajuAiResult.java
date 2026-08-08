package com.maumbujeok.backend.domain.saju.ai;

public record SajuAiResult(
        int woodPercentage,
        int firePercentage,
        int earthPercentage,
        int metalPercentage,
        int waterPercentage,
        String modelName
) {
    public SajuAiResult {
        validateRange(woodPercentage, "woodPercentage");
        validateRange(firePercentage, "firePercentage");
        validateRange(earthPercentage, "earthPercentage");
        validateRange(metalPercentage, "metalPercentage");
        validateRange(waterPercentage, "waterPercentage");
        if (woodPercentage + firePercentage + earthPercentage + metalPercentage + waterPercentage != 100) {
            throw new IllegalArgumentException("Five element percentages must sum to 100");
        }
    }

    private static void validateRange(int value, String fieldName) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException(fieldName + " must be between 0 and 100");
        }
    }
}
