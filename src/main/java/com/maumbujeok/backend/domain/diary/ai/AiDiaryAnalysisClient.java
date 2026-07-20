package com.maumbujeok.backend.domain.diary.ai;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class AiDiaryAnalysisClient {
    private final AiProvider provider;
    private final ThreadPoolTaskExecutor providerExecutor;
    private final AiAnalysisProperties properties;

    public AiDiaryAnalysisClient(
            AiProvider provider,
            @Qualifier("aiProviderExecutor") ThreadPoolTaskExecutor providerExecutor,
            AiAnalysisProperties properties
    ) {
        this.provider = provider;
        this.providerExecutor = providerExecutor;
        this.properties = properties;
    }

    public AiCallResult analyze(DiaryAiRequest request) {
        int maxAttempts = Math.max(1, properties.getMaxAttempts());
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            Future<DiaryAiResult> future = providerExecutor.submit(() -> provider.analyzeDiary(request));
            try {
                DiaryAiResult result = future.get(properties.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
                return new AiCallResult(result, attempt);
            } catch (TimeoutException exception) {
                future.cancel(true);
                if (attempt == maxAttempts) {
                    throw new AiAnalysisException("AI_TIMEOUT", attempt, exception);
                }
            } catch (InterruptedException exception) {
                future.cancel(true);
                Thread.currentThread().interrupt();
                throw new AiAnalysisException("AI_INTERRUPTED", attempt, exception);
            } catch (ExecutionException exception) {
                Throwable cause = exception.getCause();
                if (!(cause instanceof AiProviderTransientException) || attempt == maxAttempts) {
                    String code = cause instanceof AiProviderTransientException ? "AI_TRANSIENT_ERROR" : "AI_PROVIDER_ERROR";
                    throw new AiAnalysisException(code, attempt, cause);
                }
            }
        }
        throw new IllegalStateException("AI retry loop finished unexpectedly");
    }
}
