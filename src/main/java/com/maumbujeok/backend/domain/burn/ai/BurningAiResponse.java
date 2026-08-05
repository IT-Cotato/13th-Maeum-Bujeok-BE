package com.maumbujeok.backend.domain.burn.ai;

public record BurningAiResponse(
        String title,
        String comment,
        String guidance,
        Integer talismanType,
        String talismanText
) {
    public BurningAiResult toResult(String model) {
        return new BurningAiResult(title, comment, guidance, talismanType, talismanText, model);
    }
}