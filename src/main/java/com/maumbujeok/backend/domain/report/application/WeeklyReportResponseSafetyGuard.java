package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class WeeklyReportResponseSafetyGuard {
    private static final int MIN_LENGTH = 80;
    private static final int MAX_LENGTH = 700;

    public void validate(WeeklyReportAiResult result) {
        if (result == null || !StringUtils.hasText(result.insightSummary())) {
            throw new IllegalArgumentException("Missing insight summary");
        }

        String summary = result.insightSummary().trim();
        if (summary.length() < MIN_LENGTH || summary.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid insight summary length");
        }
        if (summary.contains("{") || summary.contains("}") || summary.contains("```")) {
            throw new IllegalArgumentException("Insight summary contains formatting artifacts");
        }
    }
}
