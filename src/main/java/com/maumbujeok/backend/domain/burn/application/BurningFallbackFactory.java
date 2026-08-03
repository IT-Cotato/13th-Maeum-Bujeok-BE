package com.maumbujeok.backend.domain.burn.application;

import com.maumbujeok.backend.domain.burn.ai.BurningAiResult;
import org.springframework.stereotype.Component;

@Component
public class BurningFallbackFactory {
    public BurningAiResult create(String failureCode) {
        return new BurningAiResult(
                "The memory was difficult, but you can set it down safely and take care of yourself today.",
                "\uB9C8\uC74C\uC815\uD654",
                "\uD3C9\uC628\uD68C\uBCF5",
                "fallback-burning-v1"
        );
    }
}