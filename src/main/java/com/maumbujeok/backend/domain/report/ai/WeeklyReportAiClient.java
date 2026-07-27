package com.maumbujeok.backend.domain.report.ai;

import org.springframework.stereotype.Component;

@Component
public class WeeklyReportAiClient {
    private final WeeklyReportAiProvider provider;

    public WeeklyReportAiClient(WeeklyReportAiProvider provider) {
        this.provider = provider;
    }

    public WeeklyReportAiCallResult generate(WeeklyReportAiRequest request) {
        return provider.analyzeWeeklyReport(request);
    }
}
