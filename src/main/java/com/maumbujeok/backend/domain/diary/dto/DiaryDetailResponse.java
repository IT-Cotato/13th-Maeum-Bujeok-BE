package com.maumbujeok.backend.domain.diary.dto;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.upload.dto.DiaryImageResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "DiaryDetailResponse", description = "일기 원문, 이미지와 AI 분석을 포함한 상세 결과")
public record DiaryDetailResponse(
        @Schema(description = "일기 ID", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
        Long diaryId,
        @Schema(description = "일기 원문", example = "산책을 하며 마음을 천천히 정리했다.", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,
        @Schema(description = "사용자가 선택한 감정 코드", example = "COMFORTABLE", requiredMode = Schema.RequiredMode.REQUIRED)
        String selectedEmotion,
        @Schema(description = "화면 표시용 감정 라벨", example = "편안해요", requiredMode = Schema.RequiredMode.REQUIRED)
        String selectedEmotionLabel,
        @Schema(description = "사용자 기록일", example = "2026-07-27", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate recordedDate,
        @Schema(description = "작성 시각", example = "2026-07-27T21:15:30", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime createdAt,
        @Schema(description = "마지막 수정 시각", example = "2026-07-27T22:10:00", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime updatedAt,
        @Schema(description = "표시 순서대로 정렬된 첨부 이미지. URL은 10분간 유효", requiredMode = Schema.RequiredMode.REQUIRED)
        List<DiaryImageResponse> images,
        @Schema(description = "AI 분석 결과와 처리 상태", requiredMode = Schema.RequiredMode.REQUIRED)
        DiaryAnalysisResponse analysis
) {
    public static DiaryDetailResponse from(Diary diary, DiaryAnalysis analysis, List<DiaryImageResponse> images) {
        return new DiaryDetailResponse(
                diary.getId(), diary.getContent(), diary.getSelectedEmotion().name(),
                diary.getSelectedEmotion().getLabel(), diary.getRecordedDate(),
                diary.getCreatedAt(), diary.getUpdatedAt(), images,
                DiaryAnalysisResponse.from(analysis)
        );
    }
}
