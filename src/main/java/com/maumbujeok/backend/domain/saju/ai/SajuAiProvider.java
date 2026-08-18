package com.maumbujeok.backend.domain.saju.ai;

public interface SajuAiProvider {
    SajuAiCallResult analyze(SajuAiRequest request);
}
