package com.maumbujeok.backend.domain.diary.dto;

import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysisStatus;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import com.maumbujeok.backend.global.util.TimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Schema(name = "DiaryAnalysisResponse", description = "일기 AI 분석 결과. 분석 진행 중이거나 실패한 경우 일부 필드는 null일 수 있습니다.")
public record DiaryAnalysisResponse(
        @Schema(description = "분석 처리 상태", example = "COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
        DiaryAnalysisStatus status,
        @Schema(description = "일기 요약", example = "걱정 속에서도 산책으로 마음을 돌본 하루", nullable = true)
        String summary,
        @Schema(description = "공감 메시지", example = "복잡한 마음을 잘 다독이며 하루를 지나오셨네요.", nullable = true)
        String empathyResponse,
        @Schema(description = "부정 감정 강도 (0~100)", example = "35", minimum = "0", maximum = "100", nullable = true)
        Integer negativeIntensity,
        @Schema(description = "분석된 대표 감정 한글 라벨", example = "불안", nullable = true)
        String reportEmotion,
        @Schema(description = "살풀이 추천 여부", example = "false", nullable = true)
        Boolean salpuriRecommended,
        @Schema(description = "안전 단계", example = "NORMAL", nullable = true)
        SafetyLevel safetyLevel,
        @Schema(description = "생성된 부적 ID. 부적 생성 전에는 null", nullable = true)
        Long amuletId,
        @Schema(description = "생성된 부적 유형. 부적 생성 전에는 null", nullable = true)
        String amuletType,
        @Schema(description = "생성된 부적 제목. 부적 생성 전에는 null", nullable = true)
        String title,
        @Schema(description = "분석 생성일 (yyyy.MM.dd)", example = "2026.07.23", pattern = "^\\d{4}\\.\\d{2}\\.\\d{2}$", requiredMode = Schema.RequiredMode.REQUIRED)
        String createdAt,
        @Schema(description = "분석에 사용한 AI 모델명. 처리 전에는 null", example = "gpt-5.5", nullable = true)
        String modelName,
        @Schema(description = "분석 실패 코드. 정상 처리 시 null", example = "AI_TIMEOUT", nullable = true)
        String failureCode,
        @Schema(description = "AI 분석 시도 횟수", example = "1", minimum = "0", requiredMode = Schema.RequiredMode.REQUIRED)
        int attemptCount
) {
    private static final DateTimeFormatter AMULET_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static DiaryAnalysisResponse from(DiaryAnalysis analysis) {
        OffsetDateTime seoulCreatedAt = TimeUtils.toSeoulOffset(analysis.getCreatedAt());
        String formattedDate = seoulCreatedAt == null ? null : seoulCreatedAt.format(AMULET_DATE_FORMAT);

        return new DiaryAnalysisResponse(
                analysis.getStatus(),
                analysis.getSummary(),
                analysis.getEmpathyResponse(),
                analysis.getFinalNegativeIntensity(),
                analysis.getReportEmotion() == null ? null : analysis.getReportEmotion().getLabel(),
                analysis.getSalpuriRecommended(),
                analysis.getSafetyLevel(),
                null,
                null,
                null,
                formattedDate,
                analysis.getModelName(),
                analysis.getFailureCode(),
                analysis.getAttemptCount()
        );
    }
}
