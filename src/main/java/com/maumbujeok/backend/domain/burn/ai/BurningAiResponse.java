package com.maumbujeok.backend.domain.burn.ai;

public record BurningAiResponse(
        String comment,
        Integer talismanType,
        String talismanText
) {
    public BurningAiResult toResult(String model) {
        return new BurningAiResult(comment, talismanType, talismanText, model);
    }
}