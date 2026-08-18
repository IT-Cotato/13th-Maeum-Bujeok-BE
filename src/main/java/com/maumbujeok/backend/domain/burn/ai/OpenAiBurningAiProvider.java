package com.maumbujeok.backend.domain.burn.ai;
import com.maumbujeok.backend.global.ai.client.AiGateway;
import com.maumbujeok.backend.global.ai.dto.*;
import com.maumbujeok.backend.global.ai.exception.AiClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor @ConditionalOnProperty(name="ai.provider", havingValue="openai")
public class OpenAiBurningAiProvider implements BurningAiProvider {
 private final AiGateway gateway; private final BurningAiPromptFactory prompts;
 public BurningAiResult analyze(BurningAiRequest request){try{AiExecutionResult<BurningAiResponse> r=gateway.generateStructured(new AiStructuredRequest("burning-analysis",prompts.instructions(),prompts.input(request),"burning_analysis",BurningAiResponseSchema.schema(),null,500,"burning-v1"),BurningAiResponse.class); return r.output().toResult(r.model());}catch(AiClientException e){throw new BurningAiAnalysisException(e.getFailureCode().name(),e);}}
}