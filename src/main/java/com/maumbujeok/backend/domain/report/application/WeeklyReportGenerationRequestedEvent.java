package com.maumbujeok.backend.domain.report.application;

public record WeeklyReportGenerationRequestedEvent(Long reportId, int generationSequence) {
}
