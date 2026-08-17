package com.maumbujeok.backend.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportGenerationStatus;
import com.maumbujeok.backend.global.util.TimeUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Schema(name = "WeeklyReportSummaryResponse", description = "주간 리포트 요약 조회 결과. 생성 진행 중이거나 실패한 경우 일부 필드는 null일 수 있습니다.")
public record WeeklyReportSummaryResponse(
        @Schema(description = "리포트 요약 ID. 현재는 emotionReportId와 동일하게 사용합니다.", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
        Long summaryId,
        @Schema(description = "주간 감정 리포트 ID", example = "12", requiredMode = Schema.RequiredMode.REQUIRED)
        Long emotionReportId,
        @Schema(description = "리포트 시작일", example = "2026-07-13", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate periodStart,
        @Schema(description = "리포트 종료일", example = "2026-07-19", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate periodEnd,
        @Schema(description = "생성 상태", example = "COMPLETED", requiredMode = Schema.RequiredMode.REQUIRED)
        EmotionReportGenerationStatus generationStatus,
        @Schema(description = "주간 요약 본문. 생성 중이거나 실패한 경우 null", example = "이번 주에는 행복과 평온의 감정이 주로 나타났습니다. 전반적으로 안정적이었지만 일부 상황에서 불안한 감정이 함께 나타났습니다.", nullable = true)
        String insightSummary,
        @Schema(description = "생성에 사용한 AI 모델명. 처리 전/실패 시 null", example = "gpt-5.5", nullable = true)
        String modelName,
        @Schema(description = "리포트 버전", example = "weekly-report-v1", nullable = true)
        String reportVersion,
        @Schema(description = "생성 완료 또는 실패 시각", example = "2026-07-19T23:10:00+09:00", type = "string", format = "date-time", nullable = true)
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime generatedAt
) {
    public static WeeklyReportSummaryResponse from(EmotionReport report) {
        return new WeeklyReportSummaryResponse(
                report.getId(),
                report.getId(),
                report.getPeriodStart(),
                report.getPeriodEnd(),
                report.getGenerationStatus(),
                report.getInsightSummary(),
                report.getModelName(),
                toReportVersion(report.getPromptVersion()),
                TimeUtils.toSeoulOffset(report.getGeneratedAt())
        );
    }

    private static String toReportVersion(String promptVersion) {
        if (promptVersion == null || promptVersion.isBlank()) {
            return null;
        }

        int versionIndex = promptVersion.lastIndexOf("-v");
        if (versionIndex >= 0) {
            String versionNumber = promptVersion.substring(versionIndex + 2);
            if (!versionNumber.isBlank() && versionNumber.chars().allMatch(Character::isDigit)) {
                return "v" + versionNumber + ".0";
            }
        }

        return promptVersion;
    }
}
