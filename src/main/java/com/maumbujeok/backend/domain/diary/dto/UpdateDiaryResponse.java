package com.maumbujeok.backend.domain.diary.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;
import com.maumbujeok.backend.global.util.TimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(name = "UpdateDiaryResponse", description = "일기 수정 결과")
public record UpdateDiaryResponse(
        Long diaryId,
        LocalDate recordedDate,
        @Schema(description = "수정 시각", example = "2026-08-14T19:24:00+09:00")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime updatedAt,
        DiaryAnalysisStatus analysisStatus,
        boolean analysisRestarted
) {
    public static UpdateDiaryResponse from(
            Diary diary,
            DiaryAnalysisStatus analysisStatus,
            boolean analysisRestarted
    ) {
        return new UpdateDiaryResponse(
                diary.getId(),
                diary.getRecordedDate(),
                TimeUtils.toSeoulOffset(diary.getUpdatedAt()),
                analysisStatus,
                analysisRestarted
        );
    }
}
