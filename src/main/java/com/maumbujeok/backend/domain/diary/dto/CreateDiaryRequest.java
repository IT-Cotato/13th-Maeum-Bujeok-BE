package com.maumbujeok.backend.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "CreateDiaryRequest", description = "일기 작성 요청")
public record CreateDiaryRequest(
        @Schema(description = "일기 내용 (공백 제외 1~5000자)", example = "오늘은 걱정이 많았지만 산책을 하며 마음을 가라앉혔다.", minLength = 1, maxLength = 5000, requiredMode = Schema.RequiredMode.REQUIRED)
        String content,
        @Schema(description = "선택 감정 코드", example = "ANXIOUS", allowableValues = {
                "JOYFUL", "HAPPY", "EXCITED", "COMFORTABLE", "NORMAL", "LETHARGIC", "SAD", "ANXIOUS", "ANGRY"
        }, requiredMode = Schema.RequiredMode.REQUIRED)
        String selectedEmotion,
        @Schema(description = "사용자 기록일. 생략하면 Asia/Seoul 기준 오늘", example = "2026-07-27", type = "string", format = "date")
        LocalDate recordedDate
) {
    public CreateDiaryRequest(String content, String selectedEmotion) {
        this(content, selectedEmotion, null);
    }
}
