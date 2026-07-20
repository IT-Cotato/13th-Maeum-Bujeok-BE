package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.domain.diary.ai.EmotionKeywordCandidate;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DiaryAnalysisFallbackFactory {
    private static final Map<String, String> EMPATHY_TEMPLATES = Map.ofEntries(
            Map.entry("불안", "불안한 마음을 견디느라 힘들었겠어요. 지금의 감정은 당신의 잘못이 아니에요."),
            Map.entry("슬픔", "슬픈 마음을 품고 오늘을 보내느라 힘들었겠어요. 잠시 쉬어가도 괜찮아요."),
            Map.entry("분노", "화가 날 만큼 중요한 일이었겠어요. 그런 마음이 드는 건 자연스러운 일이에요.")
    );

    public DiaryAiResult create(DiaryAnalysisInput input, SafetyLevel safetyLevel) {
        String empathy = EMPATHY_TEMPLATES.getOrDefault(
                input.selectedEmotion(),
                "오늘의 마음을 견디느라 힘들었겠어요. 지금 느끼는 감정을 천천히 살펴봐도 괜찮아요."
        );
        String summary = fitSummary(input.selectedEmotion() + " 감정을 중심으로 오늘의 마음을 차분히 돌아본 하루");
        return new DiaryAiResult(
                empathy,
                summary,
                50,
                List.of(new EmotionKeywordCandidate(input.selectedEmotion(), 1.0)),
                safetyLevel,
                "fallback-rule-v1"
        );
    }

    private String fitSummary(String value) {
        String summary = value;
        while (summary.length() < 25) summary += "의 기록";
        return summary.substring(0, Math.min(summary.length(), 40));
    }
}
