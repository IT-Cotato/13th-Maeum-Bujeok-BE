package com.maumbujeok.backend.domain.saju.ai;

import org.springframework.stereotype.Component;

@Component
public class SajuAiClient {
    private final SajuAiProvider provider;

    public SajuAiClient(SajuAiProvider provider) {
        this.provider = provider;
    }

    public SajuAiCallResult analyze(SajuAiRequest request) {
        return provider.analyze(request);
    }
}
