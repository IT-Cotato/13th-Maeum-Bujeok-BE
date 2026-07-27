package com.maumbujeok.backend.domain.diary.ai;

import static org.junit.jupiter.api.Assertions.assertSame;

import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import com.maumbujeok.backend.global.ai.emotion.ReportEmotion;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiDiaryAnalysisClientTest {
    @Test
    void delegatesToConfiguredProvider() {
        AiCallResult expected = new AiCallResult(result(), 2);
        AiProvider provider = request -> expected;
        AiDiaryAnalysisClient client = new AiDiaryAnalysisClient(provider);

        AiCallResult response = client.analyze(new DiaryAiRequest("내용", "불안", ""));

        assertSame(expected, response);
    }

    private DiaryAiResult result() {
        return new DiaryAiResult(
                "오늘의 힘든 마음을 견디느라 애썼어요.",
                "불안한 마음으로 오늘을 천천히 되돌아본 하루의 기록",
                50, List.of(), ReportEmotion.ANXIETY, SafetyLevel.NORMAL, "test"
        );
    }
}
