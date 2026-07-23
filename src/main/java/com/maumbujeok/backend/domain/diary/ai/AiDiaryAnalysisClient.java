package com.maumbujeok.backend.domain.diary.ai;

import org.springframework.stereotype.Component;

@Component
public class AiDiaryAnalysisClient {
    private final AiProvider provider;

    public AiDiaryAnalysisClient(AiProvider provider) {
        this.provider = provider;
    }

    public AiCallResult analyze(DiaryAiRequest request) {
        return provider.analyzeDiary(request);
    }
}
