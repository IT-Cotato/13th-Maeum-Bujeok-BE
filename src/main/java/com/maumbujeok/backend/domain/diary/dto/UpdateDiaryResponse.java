package com.maumbujeok.backend.domain.diary.dto;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(name = "UpdateDiaryResponse", description = "일기 수정 결과")
public record UpdateDiaryResponse(
        Long diaryId,
        LocalDate recordedDate,
        LocalDateTime updatedAt,
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
                diary.getUpdatedAt(),
                analysisStatus,
                analysisRestarted
        );
    }
}
