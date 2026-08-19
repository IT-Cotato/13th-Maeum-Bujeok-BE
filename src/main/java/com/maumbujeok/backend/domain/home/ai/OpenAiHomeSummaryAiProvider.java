package com.maumbujeok.backend.domain.home.ai;

import com.maumbujeok.backend.global.ai.client.AiGateway;
import com.maumbujeok.backend.global.ai.dto.AiExecutionResult;
import com.maumbujeok.backend.global.ai.dto.AiStructuredRequest;
import com.maumbujeok.backend.global.ai.exception.AiClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiHomeSummaryAiProvider implements HomeSummaryAiProvider {

    private final AiGateway aiGateway;
    private final ObjectMapper objectMapper;

    @Override
    public HomeSummaryAiResponse generate(String memberName, String gender, String calendarType,
                                          String birthDate, String birthTime, LocalDate summaryDate,
                                          List<TodayDiaryInput> todayDiaries) {
        String instructions;
        try {
            instructions = new ClassPathResource("prompts/home-summary-v1.txt")
                    .getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Home summary prompt could not be loaded", e);
        }

        String input;
        try {
            input = objectMapper.writeValueAsString(Map.of(
                    "memberName", memberName != null ? memberName : "사용자",
                    "gender", gender != null ? gender : "NONE",
                    "calendarType", calendarType != null ? calendarType : "SOLAR",
                    "birthDate", birthDate != null ? birthDate : "19950101",
                    "birthTime", birthTime != null ? birthTime : "12:00",
                    "todayDate", summaryDate.toString(),
                    "todayDiaries", todayDiaries != null ? todayDiaries : List.of()
            ));
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to serialize home summary AI input", e);
        }

        try {
            AiExecutionResult<HomeSummaryAiResponse> execution = aiGateway.generateStructured(
                    new AiStructuredRequest(
                            "home-summary",
                            instructions,
                            input,
                            "home_summary",
                            HomeSummaryAiResponseSchema.schema(),
                            null,
                            500,
                            "v1"
                    ),
                    HomeSummaryAiResponse.class
            );
            return execution.output();
        } catch (AiClientException exception) {
            throw new IllegalStateException("Failed to generate home summary via OpenAI: " + exception.getFailureCode(), exception);
        }
    }
}
