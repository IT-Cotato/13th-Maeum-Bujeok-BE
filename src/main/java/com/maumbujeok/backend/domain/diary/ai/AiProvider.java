package com.maumbujeok.backend.domain.diary.ai;

public interface AiProvider {
    AiCallResult analyzeDiary(DiaryAiRequest request);
}
