package com.maumbujeok.backend.domain.diary.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.maumbujeok.backend.domain.diary.domain.SafetyLevel;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

class AiDiaryAnalysisClientTest {
    private ThreadPoolTaskExecutor executor;

    @AfterEach
    void tearDown() {
        if (executor != null) executor.shutdown();
    }

    @Test
    void retriesTransientFailureWithinConfiguredLimit() {
        AtomicInteger calls = new AtomicInteger();
        AiProvider provider = request -> {
            if (calls.incrementAndGet() == 1) throw new AiProviderTransientException("temporary");
            return result();
        };
        AiAnalysisProperties properties = new AiAnalysisProperties();
        properties.setMaxAttempts(2);
        properties.setTimeout(Duration.ofSeconds(1));
        executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.initialize();
        AiDiaryAnalysisClient client = new AiDiaryAnalysisClient(provider, executor, properties);

        AiCallResult response = client.analyze(new DiaryAiRequest("내용", "불안", ""));

        assertEquals(2, response.attempts());
        assertEquals(2, calls.get());
    }

    private DiaryAiResult result() {
        return new DiaryAiResult(
                "오늘의 힘든 마음을 견디느라 애썼어요.",
                "불안한 마음으로 오늘을 천천히 되돌아본 하루의 기록",
                50, List.of(), SafetyLevel.NORMAL, "test"
        );
    }
}
