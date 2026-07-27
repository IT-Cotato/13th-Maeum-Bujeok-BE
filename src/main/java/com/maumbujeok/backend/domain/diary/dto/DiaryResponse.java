package com.maumbujeok.backend.domain.diary.dto;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(name = "DiaryResponse", description = "일기 조회 항목")
public record DiaryResponse(
        @Schema(description = "일기 ID", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
        Long diaryId,
        @Schema(description = "일기 내용", example = "오늘은 걱정이 많았지만 산책을 하며 마음을 가라앉혔다.", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,
        @Schema(description = "선택 감정 코드", example = "ANXIOUS", allowableValues = {
                "JOYFUL", "HAPPY", "EXCITED", "COMFORTABLE", "NORMAL", "LETHARGIC", "SAD", "ANXIOUS", "ANGRY"
        }, requiredMode = Schema.RequiredMode.REQUIRED)
        String selectedEmotion,
        @Schema(description = "화면 표시용 감정 한글 라벨", example = "불안해요", requiredMode = Schema.RequiredMode.REQUIRED)
        String selectedEmotionLabel,
        @Schema(description = "사용자 기록일", example = "2026-07-23", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate recordedDate,
        @Schema(description = "일기 작성 시각", example = "2026-07-23T21:15:30", type = "string", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime createdAt,
        @Schema(description = "마지막 수정 시각", example = "2026-07-27T10:30:00", type = "string", format = "date-time", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime updatedAt
) {
    public static DiaryResponse from(Diary diary) {
        return new DiaryResponse(
                diary.getId(),
                diary.getContent(),
                diary.getSelectedEmotion().name(),
                diary.getSelectedEmotion().getLabel(),
                diary.getRecordedDate(),
                diary.getCreatedAt(),
                diary.getUpdatedAt()
        );
    }
}
