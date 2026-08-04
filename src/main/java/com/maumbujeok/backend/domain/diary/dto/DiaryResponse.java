package com.maumbujeok.backend.domain.diary.dto;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(name = "DiaryResponse", description = "Diary list item")
public record DiaryResponse(
        Long diaryId,
                String title,
String content,
        String selectedEmotion,
        String selectedEmotionLabel,
        LocalDate recordedDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        @Schema(description = "Diary lifecycle status", allowableValues = {"STORED", "BURNED"})
        String status,
        Long burningId
) {
    public static DiaryResponse from(Diary diary) {
        return new DiaryResponse(
                diary.getId(), diary.getTitle(), diary.getContent(), diary.getSelectedEmotion().name(),
                diary.getSelectedEmotion().getLabel(), diary.getRecordedDate(), diary.getCreatedAt(),
                diary.getUpdatedAt(), diary.isBurned() ? "BURNED" : "STORED", diary.getBurningId()
        );
    }
}