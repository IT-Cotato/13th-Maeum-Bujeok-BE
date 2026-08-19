package com.maumbujeok.backend.domain.report.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "fake", matchIfMissing = true)
public class FakeNextWeekFlowAiProvider implements NextWeekFlowAiProvider {
    private final NextWeekFlowComposer composer;

    @Override
    public NextWeekFlowAiResponse generate(String memberName, String gender, String calendarType, String birthDate, String birthTime, String weeklyInsight) {
        return composer.compose(memberName, gender, calendarType, birthDate, birthTime, weeklyInsight);
    }
}
