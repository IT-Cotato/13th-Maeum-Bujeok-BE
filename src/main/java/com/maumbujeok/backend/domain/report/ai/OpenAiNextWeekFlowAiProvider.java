package com.maumbujeok.backend.domain.report.ai;

import com.maumbujeok.backend.global.ai.client.AiGateway;
import com.maumbujeok.backend.global.ai.dto.AiExecutionResult;
import com.maumbujeok.backend.global.ai.dto.AiStructuredRequest;
import com.maumbujeok.backend.global.ai.exception.AiClientException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiNextWeekFlowAiProvider implements NextWeekFlowAiProvider {
    private final AiGateway aiGateway;
    private final ObjectMapper objectMapper;

    @Override
    public String generate(String memberName, String gender, String calendarType, String birthDate, String birthTime, String weeklyInsight) {
        String instructions;
        try {
            instructions = new ClassPathResource("prompts/next-week-flow-v1.txt")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Next week flow prompt could not be loaded", e);
        }

        String input;
        try {
            input = objectMapper.writeValueAsString(Map.of(
                    "memberName", memberName,
                    "gender", gender,
                    "calendarType", calendarType,
                    "birthDate", birthDate,
                    "birthTime", birthTime,
                    "weeklyInsight", weeklyInsight
            ));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize next week flow AI input", e);
        }

        try {
            AiExecutionResult<NextWeekFlowAiResponse> execution = aiGateway.generateStructured(
                    new AiStructuredRequest(
                            "next-week-flow",
                            instructions,
                            input,
                            "next_week_flow",
                            NextWeekFlowAiResponseSchema.schema(),
                            null,
                            800,
                            "v1"
                    ),
                    NextWeekFlowAiResponse.class
            );
            return objectMapper.writeValueAsString(execution.output());
        } catch (AiClientException exception) {
            throw new WeeklyReportAiException(
                    exception.getFailureCode().name(),
                    exception.getAttempts(),
                    exception
            );
        }
    }
}
