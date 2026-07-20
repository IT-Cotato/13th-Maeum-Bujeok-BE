package com.maumbujeok.backend.domain.diary.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiaryAnalysisTest {
    @Test
    void followsPendingProcessingCompletedTransition() {
        DiaryAnalysis analysis = new DiaryAnalysis(null, "prompt-v1", "policy-v1");
        analysis.markProcessing();
        analysis.complete(result(), 72, true, 1, SafetyLevel.NORMAL);

        assertEquals(DiaryAnalysisStatus.COMPLETED, analysis.getStatus());
        assertEquals(72, analysis.getFinalNegativeIntensity());
        assertEquals(1, analysis.getAttemptCount());
    }

    @Test
    void refusesDuplicateStartAfterProcessing() {
        DiaryAnalysis analysis = new DiaryAnalysis(null, "prompt-v1", "policy-v1");
        analysis.markProcessing();

        assertThrows(IllegalStateException.class, analysis::markProcessing);
    }

    private DiaryAiResult result() {
        return new DiaryAiResult(
                "오늘의 힘든 마음을 견디느라 애썼어요.",
                "불안한 마음으로 오늘을 천천히 되돌아본 하루의 기록",
                70, List.of(), SafetyLevel.NORMAL, "test"
        );
    }
}
