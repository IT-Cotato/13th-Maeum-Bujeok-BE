package com.maumbujeok.backend.domain.diary.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiaryAiResponseTest {

    @Test
    void mapsKoreanReportEmotionFromStructuredOutput() {
        DiaryAiResponse response = new DiaryAiResponse(
                "오늘의 마음을 견디느라 애썼어요.",
                "불안한 마음으로 오늘을 천천히 되돌아본 하루의 기록",
                70,
                List.of(new EmotionKeywordCandidate("걱정", 0.9)),
                "불안",
                SafetyLevel.NORMAL
        );

        DiaryAiResult result = response.toResult("test-model");

        assertEquals(ReportEmotion.ANXIETY, result.reportEmotion());
    }
}
