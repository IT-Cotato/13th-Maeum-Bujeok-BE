package com.maumbujeok.backend.domain.diary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiaryAnalysisTest {
    @Test
    void followsPendingProcessingCompletedTransitionForCurrentRevision() {
        DiaryAnalysis analysis = new DiaryAnalysis(null, "prompt-v1", "policy-v1");

        assertTrue(analysis.markProcessing(1L));
        assertTrue(analysis.complete(1L, result(), 72, true, 1, SafetyLevel.NORMAL));

        assertEquals(DiaryAnalysisStatus.COMPLETED, analysis.getStatus());
        assertEquals(72, analysis.getFinalNegativeIntensity());
        assertEquals(1, analysis.getAttemptCount());
        assertEquals(ReportEmotion.ANXIETY, analysis.getReportEmotion());
    }

    @Test
    void refusesDuplicateStartAfterProcessing() {
        DiaryAnalysis analysis = new DiaryAnalysis(null, "prompt-v1", "policy-v1");

        assertTrue(analysis.markProcessing(1L));
        assertFalse(analysis.markProcessing(1L));
    }

    @Test
    void restartClearsOldResultAndRejectsOldRevision() {
        DiaryAnalysis analysis = new DiaryAnalysis(null, "prompt-v1", "policy-v1");
        analysis.markProcessing(1L);
        analysis.complete(1L, result(), 72, true, 1, SafetyLevel.NORMAL);

        long currentRevision = analysis.restart();

        assertEquals(2L, currentRevision);
        assertEquals(DiaryAnalysisStatus.PENDING, analysis.getStatus());
        assertNull(analysis.getReportEmotion());
        assertFalse(analysis.complete(1L, result(), 72, true, 1, SafetyLevel.NORMAL));
        assertTrue(analysis.markProcessing(2L));
    }

    private DiaryAiResult result() {
        return new DiaryAiResult(
                "오늘의 힘든 마음을 견디느라 애썼어요.",
                "불안한 마음으로 오늘을 천천히 되돌아본 하루의 기록",
                70,
                List.of(),
                ReportEmotion.ANXIETY,
                SafetyLevel.NORMAL,
                "test"
        );
    }
}
