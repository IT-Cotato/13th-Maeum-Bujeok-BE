package com.maumbujeok.backend.domain.diary.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.analysis")
public class AiAnalysisProperties {
    private int negativeThreshold = 70;

    public int getNegativeThreshold() {
        return negativeThreshold;
    }

    public void setNegativeThreshold(int negativeThreshold) {
        this.negativeThreshold = negativeThreshold;
    }
}
