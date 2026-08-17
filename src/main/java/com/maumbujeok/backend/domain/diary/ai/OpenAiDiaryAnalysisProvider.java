package com.maumbujeok.backend.domain.diary.ai;

import com.maumbujeok.backend.global.ai.client.AiGateway;
import com.maumbujeok.backend.global.ai.dto.AiExecutionResult;
import com.maumbujeok.backend.global.ai.dto.AiStructuredRequest;
import com.maumbujeok.backend.global.ai.exception.AiClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiDiaryAnalysisProvider implements AiProvider {
    static final String PROMPT_VERSION = "diary-v3";

    private final AiGateway aiGateway;
    private final DiaryAiPromptFactory promptFactory;

    @Override
    public AiCallResult analyzeDiary(DiaryAiRequest request) {
        try {
            AiExecutionResult<DiaryAiResponse> execution = aiGateway.generateStructured(
                    new AiStructuredRequest(
                            "diary-analysis",
                            promptFactory.instructions(),
                            promptFactory.input(request),
                            "diary_analysis",
                            DiaryAiResponseSchema.schema(),
                            null,
                            600,
                            PROMPT_VERSION
                    ),
                    DiaryAiResponse.class
            );
            return new AiCallResult(execution.output().toResult(execution.model()), execution.attempts());
        } catch (AiClientException exception) {
            throw new AiAnalysisException(
                    exception.getFailureCode().name(),
                    exception.getAttempts(),
                    exception
            );
        }
    }
}
