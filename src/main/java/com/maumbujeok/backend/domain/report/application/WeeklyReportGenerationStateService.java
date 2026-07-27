package com.maumbujeok.backend.domain.report.application;

import com.maumbujeok.backend.domain.report.ai.WeeklyReportAiResult;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportGenerationStatus;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WeeklyReportGenerationStateService {
    private final EmotionReportRepository repository;

    @Transactional
    public boolean begin(Long reportId, int generationSequence) {
        EmotionReport report = repository.findById(reportId).orElse(null);
        if (report == null
                || !report.matches(generationSequence)
                || report.getGenerationStatus() != EmotionReportGenerationStatus.PENDING) {
            return false;
        }
        report.markProcessing();
        return true;
    }

    @Transactional
    public boolean complete(Long reportId, int generationSequence, WeeklyReportAiResult result, int attempts) {
        EmotionReport report = repository.findById(reportId).orElse(null);
        if (!isActiveProcessing(report, generationSequence)) {
            return false;
        }
        report.complete(result, attempts);
        return true;
    }

    @Transactional
    public boolean fallback(Long reportId, int generationSequence, WeeklyReportAiResult result, int attempts, String code) {
        EmotionReport report = repository.findById(reportId).orElse(null);
        if (!isActiveProcessing(report, generationSequence)) {
            return false;
        }
        report.completeWithFallback(result, attempts, code);
        return true;
    }

    @Transactional
    public boolean fail(Long reportId, int generationSequence, int attempts, String code) {
        EmotionReport report = repository.findById(reportId).orElse(null);
        if (report == null || !report.matches(generationSequence)) {
            return false;
        }
        report.fail(attempts, code);
        return true;
    }

    private boolean isActiveProcessing(EmotionReport report, int generationSequence) {
        return report != null
                && report.matches(generationSequence)
                && report.getGenerationStatus() == EmotionReportGenerationStatus.PROCESSING;
    }
}
