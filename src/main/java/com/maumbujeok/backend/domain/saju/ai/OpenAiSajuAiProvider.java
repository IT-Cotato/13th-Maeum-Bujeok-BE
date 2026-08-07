package com.maumbujeok.backend.domain.saju.ai;

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
public class OpenAiSajuAiProvider implements SajuAiProvider {
    private final AiGateway aiGateway;
    private final SajuAiPromptFactory promptFactory;

    @Override
    public SajuAiCallResult analyze(SajuAiRequest request) {
        try {
            AiExecutionResult<SajuAiResponse> execution = aiGateway.generateStructured(
                    new AiStructuredRequest(
                            "saju-analysis",
                            promptFactory.instructions(),
                            promptFactory.input(request),
                            "saju_analysis",
                            SajuAiResponseSchema.schema(),
                            null,
                            200,
                            SajuAiPromptVersion.VALUE
                    ),
                    SajuAiResponse.class
            );
            return new SajuAiCallResult(execution.output().toResult(execution.model()), execution.attempts());
        } catch (AiClientException exception) {
            throw new SajuAiException(
                    exception.getFailureCode().name(),
                    exception.getAttempts(),
                    exception
            );
        }
    }
}
