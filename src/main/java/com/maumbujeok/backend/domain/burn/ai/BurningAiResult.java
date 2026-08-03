package com.maumbujeok.backend.domain.burn.ai;

public record BurningAiResult(
        String comment,
        Integer talismanType,
        String talismanText,
        String modelName
) {
    public BurningAiResult {
        if (talismanType == null || talismanType < 1 || talismanType > 13) {
            throw new IllegalArgumentException("talismanType must be between 1 and 13");
        }
        if (talismanText == null || !talismanText.matches("[\\uAC00-\\uD7A3]{4}")) {
            throw new IllegalArgumentException("talismanText must be exactly four Korean characters");
        }
    }
}