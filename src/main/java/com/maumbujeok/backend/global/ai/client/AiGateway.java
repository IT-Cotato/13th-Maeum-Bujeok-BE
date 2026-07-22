package com.maumbujeok.backend.global.ai.client;

import com.maumbujeok.backend.global.ai.dto.AiExecutionResult;
import com.maumbujeok.backend.global.ai.dto.AiStructuredRequest;

public interface AiGateway {
    <T> AiExecutionResult<T> generateStructured(AiStructuredRequest request, Class<T> responseType);
}
