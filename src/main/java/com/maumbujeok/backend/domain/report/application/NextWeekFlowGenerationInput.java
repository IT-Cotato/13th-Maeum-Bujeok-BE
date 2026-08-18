package com.maumbujeok.backend.domain.report.application;

public record NextWeekFlowGenerationInput(
        String memberName,
        String gender,
        String calendarType,
        String birthDate,
        String birthTime,
        String weeklyInsight
) {
}
