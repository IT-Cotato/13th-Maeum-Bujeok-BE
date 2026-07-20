package com.maumbujeok.backend.domain.diary.dto;

import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;

public record CreateDiaryResponse(Long diaryId, DiaryAnalysisStatus analysisStatus) {
}
