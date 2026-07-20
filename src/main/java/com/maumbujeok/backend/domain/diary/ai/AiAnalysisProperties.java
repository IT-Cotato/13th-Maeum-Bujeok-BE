package com.maumbujeok.backend.domain.diary.ai;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.analysis")
public class AiAnalysisProperties {
    private Duration timeout = Duration.ofSeconds(3);
    private int maxAttempts = 2;
    private int negativeThreshold = 70;

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getNegativeThreshold() {
        return negativeThreshold;
    }

    public void setNegativeThreshold(int negativeThreshold) {
        this.negativeThreshold = negativeThreshold;
    }
}
