package com.maumbujeok.backend.domain.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.global.util.TimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(name = "DiaryResponse", description = "Diary list item")
public record DiaryResponse(
        Long diaryId,
        String title,
        String content,
        String selectedEmotion,
        String selectedEmotionLabel,
        LocalDate recordedDate,
        @Schema(description = "작성 시각", example = "2026-08-14T19:24:00+09:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime createdAt,
        @Schema(description = "수정 시각", example = "2026-08-14T19:24:00+09:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime updatedAt,
        @Schema(description = "Diary lifecycle status", allowableValues = {"STORED", "BURNED"})
        String status,
        Long burningId
) {
    public static DiaryResponse from(Diary diary) {
        return new DiaryResponse(
                diary.getId(), diary.getTitle(), diary.getContent(), diary.getSelectedEmotion().name(),
                diary.getSelectedEmotion().getLabel(), diary.getRecordedDate(),
                TimeUtils.toSeoulOffset(diary.getCreatedAt()),
                TimeUtils.toSeoulOffset(diary.getUpdatedAt()),
                diary.isBurned() ? "BURNED" : "STORED", diary.getBurningId()
        );
    }
}