package com.maumbujeok.backend.domain.diary.dto;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.upload.dto.DiaryImageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "DiaryDetailResponse", description = "Diary detail")
public record DiaryDetailResponse(
        Long diaryId,
                String title,
String content,
        String selectedEmotion,
        String selectedEmotionLabel,
        LocalDate recordedDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String status,
        Long burningId,
        List<DiaryImageResponse> images,
        DiaryAnalysisResponse analysis
) {
    public static DiaryDetailResponse from(Diary diary, DiaryAnalysis analysis, List<DiaryImageResponse> images) {
        return new DiaryDetailResponse(
                diary.getId(), diary.getTitle(), diary.getContent(), diary.getSelectedEmotion().name(),
                diary.getSelectedEmotion().getLabel(), diary.getRecordedDate(), diary.getCreatedAt(),
                diary.getUpdatedAt(), diary.isBurned() ? "BURNED" : "STORED", diary.getBurningId(),
                images, DiaryAnalysisResponse.from(analysis)
        );
    }
}