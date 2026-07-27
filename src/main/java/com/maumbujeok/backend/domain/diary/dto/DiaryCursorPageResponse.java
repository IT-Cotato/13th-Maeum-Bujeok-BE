package com.maumbujeok.backend.domain.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "DiaryCursorPageResponse", description = "일기 커서 페이지")
public record DiaryCursorPageResponse(
        @Schema(description = "recordedDate와 diaryId 기준 최신순 일기 목록", requiredMode = Schema.RequiredMode.REQUIRED)
        List<DiaryResponse> items,
        @Schema(description = "다음 페이지 커서. 다음 페이지가 없으면 null", example = "MjAyNi0wNy0yM3w0Mg", nullable = true)
        String nextCursor,
        @Schema(description = "다음 페이지 존재 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        boolean hasNext
) {}
