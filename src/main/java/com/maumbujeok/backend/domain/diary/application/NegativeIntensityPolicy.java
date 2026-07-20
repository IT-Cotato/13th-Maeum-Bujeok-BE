package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.diary.ai.AiAnalysisProperties;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NegativeIntensityPolicy {
    private static final List<String> NEGATIVE_WORDS = List.of(
            "불안", "슬프", "분노", "우울", "힘들", "자책", "무기력", "괴롭", "외롭", "절망"
    );
    private static final Map<String, Integer> EMOTION_SCORES = Map.ofEntries(
            Map.entry("불안", 75), Map.entry("슬픔", 70), Map.entry("분노", 75),
            Map.entry("우울", 80), Map.entry("무기력", 75), Map.entry("자책", 80),
            Map.entry("기쁨", 10), Map.entry("평온", 5), Map.entry("행복", 5)
    );

    private final AiAnalysisProperties properties;

    public NegativeIntensityPolicy(AiAnalysisProperties properties) {
        this.properties = properties;
    }

    public int calculate(int aiScore, String content, String selectedEmotion) {
        int lexicalScore = lexicalScore(content);
        int selectedScore = EMOTION_SCORES.getOrDefault(selectedEmotion.trim(), 50);
        return clamp((int) Math.round(aiScore * 0.6 + lexicalScore * 0.3 + selectedScore * 0.1));
    }

    public boolean recommendsSalpuri(int finalScore) {
        return finalScore >= properties.getNegativeThreshold();
    }

    private int lexicalScore(String content) {
        long matches = NEGATIVE_WORDS.stream().filter(content::contains).count();
        return clamp((int) matches * 20);
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }
}
