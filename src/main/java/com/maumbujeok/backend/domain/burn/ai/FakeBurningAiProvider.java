package com.maumbujeok.backend.domain.burn.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "fake", matchIfMissing = true)
public class FakeBurningAiProvider implements BurningAiProvider {
    @Override
    public BurningAiResult analyze(BurningAiRequest request) {
        return new BurningAiResult(
                "\uD798\uB4E4\uC5C8\uB358 \uAE30\uC5B5\uC744 \uC774\uC81C \uC548\uC804\uD558\uAC8C \uB0B4\uB824\uB193\uACE0 \uC624\uB298\uC758 \uB098\uB97C \uB3CC\uBD10\uC8FC\uC138\uC694.",
                2,
                "\uD3C9\uC628\uD68C\uBCF5",
                "fake-burning-v1"
        );
    }
}