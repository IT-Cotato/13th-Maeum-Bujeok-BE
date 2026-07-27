package com.maumbujeok.backend.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record NextWeekFlowQueryResponse(
        Long flowId,
        Long emotionReportId,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate periodStart,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate periodEnd,
        NextWeekFlowGenerationStatus generationStatus,
        String adviceText,
        String modelName,
        String reportVersion,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        OffsetDateTime generatedAt
) {
}
