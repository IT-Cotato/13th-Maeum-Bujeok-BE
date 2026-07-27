package com.maumbujeok.backend.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import java.time.LocalDate;

public record NextWeekFlowStartResponse(
        Long flowId,
        Long emotionReportId,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate weekStart,
        NextWeekFlowGenerationStatus generationStatus,
        String message
) {
}
