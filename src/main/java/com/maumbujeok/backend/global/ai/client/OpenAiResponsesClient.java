package com.maumbujeok.backend.global.ai.client;

import com.maumbujeok.backend.global.ai.config.AiProperties;
import com.maumbujeok.backend.global.ai.dto.AiExecutionResult;
import com.maumbujeok.backend.global.ai.dto.AiStructuredRequest;
import com.maumbujeok.backend.global.ai.exception.AiClientException;
import com.maumbujeok.backend.global.ai.exception.AiFailureCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiResponsesClient implements AiGateway {
    private final RestClient openAiRestClient;
    private final ObjectMapper objectMapper;
    private final AiProperties properties;

    @Override
    public <T> AiExecutionResult<T> generateStructured(AiStructuredRequest request, Class<T> responseType) {
        int maxAttempts = Math.max(1, properties.getOpenai().getMaxAttempts());
        long startedAt = System.nanoTime();
        String model = resolveModel(request);

        log.info("AI request started task={} promptVersion={} model={} maxAttempts={}",
                request.taskName(), request.promptVersion(), model, maxAttempts);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                ResponseEntity<String> response = openAiRestClient.post()
                        .uri("/responses")
                        .body(createBody(request))
                        .retrieve()
                        .toEntity(String.class);
                AiExecutionResult<T> result = parse(
                        response.getBody(),
                        responseType,
                        attempt,
                        startedAt,
                        request.taskName(),
                        request.promptVersion(),
                        model
                );
                log.info("AI request completed task={} promptVersion={} model={} responseId={} attempts={} latencyMs={} inputTokens={} outputTokens={}",
                        request.taskName(), request.promptVersion(), result.model(), result.responseId(), result.attempts(),
                        result.latencyMs(), result.inputTokens(), result.outputTokens());
                return result;
            } catch (RestClientResponseException exception) {
                AiFailureCode code = classify(exception.getStatusCode().value());
                boolean retrying = isRetryable(code) && attempt < maxAttempts;
                log.warn("AI request HTTP failure task={} promptVersion={} model={} attempt={}/{} status={} failureCode={} retrying={}",
                        request.taskName(), request.promptVersion(), model, attempt, maxAttempts,
                        exception.getStatusCode().value(), code, retrying);
                if (!retrying) {
                    throw new AiClientException(code, attempt, exception);
                }
            } catch (ResourceAccessException exception) {
                boolean retrying = attempt < maxAttempts;
                log.warn("AI request timeout task={} promptVersion={} model={} attempt={}/{} retrying={}",
                        request.taskName(), request.promptVersion(), model, attempt, maxAttempts, retrying);
                if (attempt == maxAttempts) {
                    throw new AiClientException(AiFailureCode.AI_TIMEOUT, attempt, exception);
                }
            } catch (AiClientException exception) {
                log.warn("AI response rejected task={} promptVersion={} model={} attempt={}/{} failureCode={}",
                        request.taskName(), request.promptVersion(), model, attempt, maxAttempts,
                        exception.getFailureCode());
                throw exception;
            } catch (RuntimeException exception) {
                log.error("AI request unexpected failure task={} promptVersion={} model={} attempt={}/{} exceptionType={}",
                        request.taskName(), request.promptVersion(), model, attempt, maxAttempts,
                        exception.getClass().getSimpleName());
                throw new AiClientException(AiFailureCode.AI_PROVIDER_ERROR, attempt, exception);
            }
        }
        throw new AiClientException(AiFailureCode.AI_PROVIDER_ERROR, maxAttempts);
    }

    private Map<String, Object> createBody(AiStructuredRequest request) {
        String model = resolveModel(request);
        Map<String, Object> format = Map.of(
                "type", "json_schema",
                "name", request.schemaName(),
                "strict", true,
                "schema", request.schema()
        );
        return Map.of(
                "model", model,
                "instructions", request.instructions(),
                "input", request.input(),
                "store", false,
                "max_output_tokens", request.maxOutputTokens(),
                "text", Map.of("format", format)
        );
    }

    private String resolveModel(AiStructuredRequest request) {
        return request.modelOverride() == null || request.modelOverride().isBlank()
                ? properties.getOpenai().getModel()
                : request.modelOverride();
    }

    private <T> AiExecutionResult<T> parse(
            String body,
            Class<T> responseType,
            int attempts,
            long startedAt,
            String taskName,
            String promptVersion,
            String requestedModel
    ) {
        try {
            JsonNode root = objectMapper.readTree(body);
            String responseId = root.path("id").asText("unknown");
            String actualModel = root.path("model").asText("unknown");
            String outputText;
            try {
                outputText = extractOutputText(root, attempts);
            } catch (AiClientException exception) {
                if (exception.getFailureCode() == AiFailureCode.AI_INVALID_RESPONSE) {
                    log.warn("AI response missing output_text task={} promptVersion={} requestedModel={} actualModel={} responseId={} attempt={} summary={}",
                            taskName, promptVersion, requestedModel, actualModel, responseId, attempts, summarizeResponse(root));
                }
                throw exception;
            }

            T output;
            try {
                output = objectMapper.readValue(outputText, responseType);
            } catch (Exception exception) {
                log.warn("AI response JSON parse failed task={} promptVersion={} requestedModel={} actualModel={} responseId={} attempt={} causeType={} causeMessage={} outputPreview={} summary={}",
                        taskName,
                        promptVersion,
                        requestedModel,
                        actualModel,
                        responseId,
                        attempts,
                        exception.getClass().getSimpleName(),
                        sanitize(exception.getMessage(), 200),
                        sanitize(outputText, 400),
                        summarizeResponse(root));
                throw new AiClientException(AiFailureCode.AI_INVALID_RESPONSE, attempts, exception);
            }
            JsonNode usage = root.path("usage");
            return new AiExecutionResult<>(
                    output,
                    actualModel,
                    responseId,
                    usage.path("input_tokens").asLong(0),
                    usage.path("output_tokens").asLong(0),
                    attempts,
                    (System.nanoTime() - startedAt) / 1_000_000
            );
        } catch (AiClientException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AiClientException(AiFailureCode.AI_INVALID_RESPONSE, attempts, exception);
        }
    }

    private String extractOutputText(JsonNode root, int attempts) {
        for (JsonNode output : root.path("output")) {
            for (JsonNode content : output.path("content")) {
                String type = content.path("type").asText();
                if ("refusal".equals(type)) {
                    throw new AiClientException(AiFailureCode.AI_REFUSED, attempts);
                }
                if ("output_text".equals(type) && !content.path("text").asText().isBlank()) {
                    return content.path("text").asText();
                }
            }
        }
        throw new AiClientException(AiFailureCode.AI_INVALID_RESPONSE, attempts);
    }

    private AiFailureCode classify(int status) {
        if (status == 401 || status == 403) return AiFailureCode.AI_AUTH_ERROR;
        if (status == 429) return AiFailureCode.AI_RATE_LIMITED;
        if (status >= 500) return AiFailureCode.AI_SERVER_ERROR;
        if (status >= 400) return AiFailureCode.AI_BAD_REQUEST;
        return AiFailureCode.AI_PROVIDER_ERROR;
    }

    private boolean isRetryable(AiFailureCode code) {
        return List.of(AiFailureCode.AI_RATE_LIMITED, AiFailureCode.AI_SERVER_ERROR).contains(code);
    }

    private String summarizeResponse(JsonNode root) {
        List<String> outputSummaries = new ArrayList<>();
        int index = 0;
        for (JsonNode output : root.path("output")) {
            if (index++ == 3) {
                outputSummaries.add("...");
                break;
            }

            List<String> contentTypes = new ArrayList<>();
            int contentIndex = 0;
            for (JsonNode content : output.path("content")) {
                if (contentIndex++ == 5) {
                    contentTypes.add("...");
                    break;
                }
                contentTypes.add(content.path("type").asText("unknown"));
            }

            outputSummaries.add(output.path("type").asText("unknown") + "(contentTypes=" + contentTypes + ")");
        }

        return "status=" + root.path("status").asText("unknown")
                + ", incompleteDetails=" + sanitize(root.path("incomplete_details").toString(), 160)
                + ", outputCount=" + root.path("output").size()
                + ", output=" + outputSummaries;
    }

    private String sanitize(String value, int maxLength) {
        if (value == null) {
            return "null";
        }
        String normalized = value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }
}
