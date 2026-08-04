package com.maumbujeok.backend.domain.report.ai;

import org.springframework.stereotype.Component;

@Component
public class NextWeekFlowComposer {
    public String compose(String name, String gender, String calendarType, String birthDate, String birthTime, String weeklyInsight) {
        String weeklySummaryText = weeklyInsight != null ? weeklyInsight : "이번 주 기록 없음";
        return String.format(
                "안녕하세요 %s님. 사주 정보(생년월일: %s, 성별: %s, 달력: %s, 시간: %s)와 "
                + "이번 주 감정 흐름(%s)을 종합한 결과, 다음 주는 대단히 차분하고 안정된 흐름이 이어질 것입니다. "
                + "스스로에게 과한 부담감을 주지 마시고 한 박자 쉬어가며, 가까운 이들과 따뜻한 차를 마시며 여유를 느껴보세요.",
                name, birthDate, gender, calendarType, birthTime, weeklySummaryText
        );
    }
}
