package com.maumbujeok.backend.domain.diary.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.maumbujeok.backend.domain.diary.ai.DiaryAiResult;
import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiResponseSafetyGuardTest {
    private final AiResponseSafetyGuard guard = new AiResponseSafetyGuard();

    @Test
    void acceptsValidStructuredOutput() {
        assertDoesNotThrow(() -> guard.validate(validResult()));
    }

    @Test
    void rejectsFortuneTellingAndInvalidSummaryLength() {
        DiaryAiResult unsafe = new DiaryAiResult(
                "당신은 원래 불안한 사람이에요.", "짧음", 50, List.of(), SafetyLevel.NORMAL, "test"
        );
        assertThrows(IllegalArgumentException.class, () -> guard.validate(unsafe));
    }

    @Test
    void separatesCrisisSignalFromOrdinaryRecommendationPolicy() {
        assertEquals(SafetyLevel.CRISIS, guard.resolveSafetyLevel("오늘 죽고 싶다는 생각이 들었다", SafetyLevel.NORMAL));
    }

    private DiaryAiResult validResult() {
        return new DiaryAiResult(
                "오늘의 힘든 마음을 견디느라 애썼어요.",
                "불안한 마음으로 오늘을 천천히 되돌아본 하루의 기록",
                50,
                List.of(),
                SafetyLevel.NORMAL,
                "test"
        );
    }
}
