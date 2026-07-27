package com.maumbujeok.backend.domain.diary.dto;

import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(name = "CreateDiaryResponse", description = "일기 작성 결과")
public record CreateDiaryResponse(
        @Schema(description = "생성된 일기 ID", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
        Long diaryId,
        @Schema(description = "사용자 기록일", example = "2026-07-27", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate recordedDate,
        @Schema(description = "비동기 AI 분석 상태. 생성 직후에는 PENDING", example = "PENDING", requiredMode = Schema.RequiredMode.REQUIRED)
        DiaryAnalysisStatus analysisStatus
) {
}
