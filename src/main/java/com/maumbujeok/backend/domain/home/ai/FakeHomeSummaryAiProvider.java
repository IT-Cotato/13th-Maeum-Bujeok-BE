package com.maumbujeok.backend.domain.home.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "fake", matchIfMissing = true)
public class FakeHomeSummaryAiProvider implements HomeSummaryAiProvider {

    private final HomeSummaryComposer composer;

    @Override
    public HomeSummaryAiResponse generate(String memberName, String gender, String calendarType,
                                          String birthDate, String birthTime, LocalDate summaryDate,
                                          List<TodayDiaryInput> todayDiaries) {
        return composer.compose(memberName, gender, calendarType, birthDate, birthTime, summaryDate, todayDiaries);
    }
}
