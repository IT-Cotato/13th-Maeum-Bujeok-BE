package com.maumbujeok.backend.domain.diary.ai;

public interface AiProvider {
    DiaryAiResult analyzeDiary(DiaryAiRequest request);
}
