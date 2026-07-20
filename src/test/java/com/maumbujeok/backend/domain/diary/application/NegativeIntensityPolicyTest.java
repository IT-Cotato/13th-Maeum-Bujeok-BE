package com.maumbujeok.backend.domain.diary.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maumbujeok.backend.domain.diary.ai.AiAnalysisProperties;
import org.junit.jupiter.api.Test;

class NegativeIntensityPolicyTest {
    private final AiAnalysisProperties properties = new AiAnalysisProperties();
    private final NegativeIntensityPolicy policy = new NegativeIntensityPolicy(properties);

    @Test
    void combinesAiLexicalAndSelectedEmotionScores() {
        assertEquals(74, policy.calculate(80, "불안하고 힘들고 자책했다", "불안"));
    }

    @Test
    void appliesConfiguredRecommendationThreshold() {
        assertFalse(policy.recommendsSalpuri(69));
        assertTrue(policy.recommendsSalpuri(70));
    }
}
