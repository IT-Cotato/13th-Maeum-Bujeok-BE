package com.maumbujeok.backend.domain.report.application;

public record NextWeekFlowGenerationRequestedEvent(
        Long flowId,
        Long emotionReportId,
        int reportGenerationSequence
) {
}
