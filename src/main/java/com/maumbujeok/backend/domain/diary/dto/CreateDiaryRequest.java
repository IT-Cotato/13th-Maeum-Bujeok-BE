package com.maumbujeok.backend.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(name = "CreateDiaryRequest", description = "일기 작성 요청")
public record CreateDiaryRequest(
        @Schema(description = "일기 원문(공백 제외 1~5000자)", example = "산책을 하며 마음을 천천히 정리했다.", minLength = 1, maxLength = 5000, requiredMode = Schema.RequiredMode.REQUIRED)
        String content,
        @Schema(description = "사용자가 선택한 감정 코드", example = "COMFORTABLE", allowableValues = {"JOYFUL", "HAPPY", "EXCITED", "COMFORTABLE", "NORMAL", "LETHARGIC", "SAD", "ANXIOUS", "ANGRY"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String selectedEmotion,
        @Schema(description = "사용자 기록일. 생략하면 Asia/Seoul 기준 오늘이며 미래 날짜는 허용하지 않음", example = "2026-07-27", type = "string", format = "date")
        LocalDate recordedDate,
        @Schema(description = "업로드 완료된 이미지 ID 목록(최대 5개, 배열 순서가 표시 순서). 생략 가능", example = "[\"550e8400-e29b-41d4-a716-446655440000\"]")
        List<UUID> imageUploadIds
) {
    public CreateDiaryRequest(String content, String selectedEmotion) {
        this(content, selectedEmotion, null, null);
    }

    public CreateDiaryRequest(String content, String selectedEmotion, LocalDate recordedDate) {
        this(content, selectedEmotion, recordedDate, null);
    }
}
