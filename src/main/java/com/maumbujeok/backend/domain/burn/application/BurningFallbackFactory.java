package com.maumbujeok.backend.domain.burn.application;

import com.maumbujeok.backend.domain.burn.ai.BurningAiResult;
import org.springframework.stereotype.Component;

@Component
public class BurningFallbackFactory {
    public BurningAiResult create(String failureCode) {
        return new BurningAiResult(null, "fallback comment", "fallback guidance", 2, "평온회복", "fallback-burning-v1");
    }
}