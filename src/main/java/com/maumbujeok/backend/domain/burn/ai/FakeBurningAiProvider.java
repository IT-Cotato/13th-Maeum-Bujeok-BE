package com.maumbujeok.backend.domain.burn.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "fake", matchIfMissing = true)
public class FakeBurningAiProvider implements BurningAiProvider {
    @Override
    public BurningAiResult analyze(BurningAiRequest request) {
        return new BurningAiResult(null, "fallback comment", "fallback guidance", 2, "평온회복", "fallback-burning-v1");
    }
}