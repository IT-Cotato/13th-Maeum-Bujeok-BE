package com.maumbujeok.backend.domain.home.ai;

import java.time.LocalDate;
import java.util.List;

public interface HomeSummaryAiProvider {
    HomeSummaryAiResponse generate(String memberName, String gender, String calendarType, String birthDate, String birthTime, LocalDate summaryDate, List<TodayDiaryInput> todayDiaries);
}
