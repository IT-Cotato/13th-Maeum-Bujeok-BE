package com.maumbujeok.backend.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "DiaryCalendarDayResponse", description = "달력에서 일기가 존재하는 날짜")
public record DiaryCalendarDayResponse(
        @Schema(description = "기록일", example = "2026-07-27", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate date,
        @Schema(description = "일기 보관 상태", example = "STORED", allowableValues = {"STORED", "BURNED"}, requiredMode = Schema.RequiredMode.REQUIRED)
        String status,
        @Schema(description = "원본 일기 ID. 소각 후 원본이 삭제된 경우 null일 수 있음", example = "42", nullable = true)
        Long diaryId,
        @Schema(description = "소각 기록 ID. STORED 상태에서는 null", example = "17", nullable = true)
        Long burningId
) {}
