package com.maumbujeok.backend.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "DiaryCalendarResponse", description = "월별 일기 달력 조회 결과")
public record DiaryCalendarResponse(
        @Schema(description = "조회 연도", example = "2026", requiredMode = Schema.RequiredMode.REQUIRED)
        int year,
        @Schema(description = "조회 월", example = "7", minimum = "1", maximum = "12", requiredMode = Schema.RequiredMode.REQUIRED)
        int month,
        @Schema(description = "일기가 존재하는 날짜만 포함한 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        List<DiaryCalendarDayResponse> days
) {}
