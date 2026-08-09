package com.maumbujeok.backend.domain.saju.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.global.ai.client.AiGateway;
import com.maumbujeok.backend.global.ai.dto.AiExecutionResult;
import com.maumbujeok.backend.global.ai.dto.AiStructuredRequest;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OpenAiSajuAiProviderTest {

    @Mock AiGateway aiGateway;
    @Mock SajuAiPromptFactory promptFactory;
    @Captor ArgumentCaptor<AiStructuredRequest> requestCaptor;

    @Test
    void usesExpandedOutputTokenBudgetForSajuAnalysis() {
        OpenAiSajuAiProvider provider = new OpenAiSajuAiProvider(aiGateway, promptFactory);
        SajuAiRequest request = new SajuAiRequest(
                "19900101",
                MemberSajuProfile.Gender.FEMALE,
                MemberSajuProfile.CalendarType.SOLAR,
                LocalTime.of(9, 30)
        );
        when(promptFactory.instructions()).thenReturn("instructions");
        when(promptFactory.input(request)).thenReturn("input");
        when(aiGateway.generateStructured(any(AiStructuredRequest.class), any()))
                .thenReturn(new AiExecutionResult<>(
                        new SajuAiResponse(20, 20, 20, 20, 20),
                        "gpt-5.5",
                        "resp_123",
                        100,
                        50,
                        1,
                        1200
                ));

        SajuAiCallResult result = provider.analyze(request);

        verify(aiGateway).generateStructured(requestCaptor.capture(), any());
        AiStructuredRequest captured = requestCaptor.getValue();
        assertEquals("saju-analysis", captured.taskName());
        assertEquals(25000, captured.maxOutputTokens());
        assertEquals(SajuAiPromptVersion.VALUE, captured.promptVersion());
        assertEquals(20, result.result().woodPercentage());
        assertEquals("gpt-5.5", result.result().modelName());
        assertEquals(1, result.attempts());
    }
}
