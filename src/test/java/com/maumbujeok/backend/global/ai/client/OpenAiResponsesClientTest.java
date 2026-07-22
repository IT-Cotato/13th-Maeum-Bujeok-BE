package com.maumbujeok.backend.global.ai.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.maumbujeok.backend.global.ai.config.AiProperties;
import com.maumbujeok.backend.global.ai.dto.AiExecutionResult;
import com.maumbujeok.backend.global.ai.dto.AiStructuredRequest;
import com.maumbujeok.backend.global.ai.exception.AiClientException;
import com.maumbujeok.backend.global.ai.exception.AiFailureCode;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

class OpenAiResponsesClientTest {
    private MockRestServiceServer server;
    private OpenAiResponsesClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.openai.test/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        AiProperties properties = new AiProperties();
        properties.getOpenai().setModel("test-model");
        properties.getOpenai().setMaxAttempts(2);
        client = new OpenAiResponsesClient(builder.build(), new ObjectMapper(), properties);
    }

    @Test
    void parsesStructuredResponseAndMetadata() {
        server.expect(requestTo("https://api.openai.test/v1/responses"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(successBody(), MediaType.APPLICATION_JSON));

        AiExecutionResult<TestOutput> result = client.generateStructured(request(), TestOutput.class);

        assertEquals("ok", result.output().value());
        assertEquals("test-model", result.model());
        assertEquals("resp_123", result.responseId());
        assertEquals(12, result.inputTokens());
        assertEquals(4, result.outputTokens());
        assertEquals(1, result.attempts());
        server.verify();
    }

    @Test
    void retriesServerErrorWithinConfiguredLimit() {
        server.expect(requestTo("https://api.openai.test/v1/responses"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
        server.expect(requestTo("https://api.openai.test/v1/responses"))
                .andRespond(withSuccess(successBody(), MediaType.APPLICATION_JSON));

        AiExecutionResult<TestOutput> result = client.generateStructured(request(), TestOutput.class);

        assertEquals(2, result.attempts());
        server.verify();
    }

    @Test
    void doesNotRetryAuthenticationError() {
        server.expect(requestTo("https://api.openai.test/v1/responses"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        AiClientException exception = assertThrows(
                AiClientException.class,
                () -> client.generateStructured(request(), TestOutput.class)
        );

        assertEquals(AiFailureCode.AI_AUTH_ERROR, exception.getFailureCode());
        assertEquals(1, exception.getAttempts());
        server.verify();
    }

    @Test
    void reportsModelRefusal() {
        server.expect(requestTo("https://api.openai.test/v1/responses"))
                .andRespond(withSuccess("""
                        {
                          "id": "resp_refused",
                          "model": "test-model",
                          "output": [{
                            "type": "message",
                            "content": [{"type": "refusal", "refusal": "cannot comply"}]
                          }]
                        }
                        """, MediaType.APPLICATION_JSON));

        AiClientException exception = assertThrows(
                AiClientException.class,
                () -> client.generateStructured(request(), TestOutput.class)
        );

        assertEquals(AiFailureCode.AI_REFUSED, exception.getFailureCode());
        server.verify();
    }

    private AiStructuredRequest request() {
        return new AiStructuredRequest(
                "test-task",
                "Return structured data.",
                "input",
                "test_output",
                Map.of(
                        "type", "object",
                        "additionalProperties", false,
                        "properties", Map.of("value", Map.of("type", "string")),
                        "required", List.of("value")
                ),
                null,
                100,
                "test-v1"
        );
    }

    private String successBody() {
        return """
                {
                  "id": "resp_123",
                  "model": "test-model",
                  "output": [{
                    "type": "message",
                    "content": [{"type": "output_text", "text": "{\\\"value\\\":\\\"ok\\\"}"}]
                  }],
                  "usage": {"input_tokens": 12, "output_tokens": 4}
                }
                """;
    }

    private record TestOutput(String value) {
    }
}
