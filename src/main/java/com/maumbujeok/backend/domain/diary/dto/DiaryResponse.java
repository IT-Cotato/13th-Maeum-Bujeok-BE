package com.maumbujeok.backend.domain.diary.dto;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import java.time.LocalDateTime;

public record DiaryResponse(
        Long diaryId,
        String content,
        String selectedEmotion,
        String selectedEmotionLabel,
        LocalDateTime createdAt
) {
    public static DiaryResponse from(Diary diary) {
        return new DiaryResponse(
                diary.getId(),
                diary.getContent(),
                diary.getSelectedEmotion().name(),
                diary.getSelectedEmotion().getLabel(),
                diary.getCreatedAt()
        );
    }
}
