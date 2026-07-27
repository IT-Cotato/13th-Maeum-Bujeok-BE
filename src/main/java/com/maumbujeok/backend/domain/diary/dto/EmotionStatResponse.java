package com.maumbujeok.backend.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "EmotionStatResponse", description = "AI 분석 대표 감정별 일기 개수")
public record EmotionStatResponse(
        @Schema(description = "대표 감정", example = "불안")
        String emotion,
        @Schema(description = "해당 감정의 분석 완료 일기 개수", example = "3")
        long count
) {
}
