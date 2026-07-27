package com.maumbujeok.backend.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(name = "UpdateDiaryRequest", description = "일기 부분 수정 요청. 기록일은 변경하지 않습니다. 최소 한 필드는 전달해야 합니다.")
public record UpdateDiaryRequest(
        @Schema(description = "변경할 일기 원문(공백 제외 1~5000자)", example = "발표 준비를 마치고 나니 마음이 한결 편해졌다.", minLength = 1, maxLength = 5000)
        String content,
        @Schema(description = "변경할 선택 감정 코드", example = "HAPPY", allowableValues = {"JOYFUL", "HAPPY", "EXCITED", "COMFORTABLE", "NORMAL", "LETHARGIC", "SAD", "ANXIOUS", "ANGRY"})
        String selectedEmotion,
        @Schema(description = "생략하면 기존 이미지를 유지하고, 빈 배열이면 모두 제거하며, 값이 있으면 배열 순서대로 전체 교체(최대 5개)", example = "[]", maxItems = 5)
        List<UUID> imageUploadIds
) {
    public UpdateDiaryRequest(String content, String selectedEmotion) {
        this(content, selectedEmotion, null);
    }
}
