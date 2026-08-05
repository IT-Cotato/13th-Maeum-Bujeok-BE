package com.maumbujeok.backend.domain.diary.ai;

import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "fake", matchIfMissing = true)
public class FakeAiProvider implements AiProvider {
    @Override
    public AiCallResult analyzeDiary(DiaryAiRequest request) {
        SafetyLevel safety = containsCrisisSignal(request.diaryContent()) ? SafetyLevel.CRISIS : SafetyLevel.NORMAL;
        int score = estimateScore(request.diaryContent(), request.selectedEmotion());
        String summary = fitSummary(request.selectedEmotion() + " 감정 속에서 오늘의 마음을 차분히 돌아본 하루");
        String empathy = safety == SafetyLevel.CRISIS
                ? "지금의 힘든 마음을 혼자 감당하지 않아도 괜찮아요."
                : "오늘 그런 마음을 느끼느라 많이 힘들었겠어요. 그 감정은 당신의 잘못이 아니에요.";
        DiaryAiResult result = new DiaryAiResult(
                                null,
empathy,
                summary,
                score,
                List.of(new EmotionKeywordCandidate(request.selectedEmotion(), 0.8)),
                ReportEmotion.fromFreeText(request.selectedEmotion() + " " + request.diaryContent()),
                safety,
                "fake-diary-v1"
        );
        return new AiCallResult(result, 1);
    }

    private int estimateScore(String content, String emotion) {
        int score = 35;
        String combined = (content + " " + emotion).toLowerCase();
        for (String word : List.of("불안", "슬픔", "분노", "우울", "힘들", "자책", "무기력")) {
            if (combined.contains(word)) score += 7;
        }
        return Math.min(score, 100);
    }

    private boolean containsCrisisSignal(String content) {
        return List.of("죽고 싶", "자살", "자해").stream().anyMatch(content::contains);
    }

    private String fitSummary(String value) {
        String summary = value;
        while (summary.length() < 10) summary += "의 기록";
        return summary.substring(0, Math.min(summary.length(), 40));
    }
}
