package com.maumbujeok.backend.domain.report.ai;

public interface NextWeekFlowAiProvider {
    NextWeekFlowAiResponse generate(String memberName, String gender, String calendarType, String birthDate, String birthTime, String weeklyInsight);
}
