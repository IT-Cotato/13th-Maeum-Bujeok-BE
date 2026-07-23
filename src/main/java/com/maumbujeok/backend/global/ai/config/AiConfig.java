package com.maumbujeok.backend.global.ai.config;

import java.net.http.HttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {

    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
    public RestClient openAiRestClient(AiProperties properties) {
        AiProperties.OpenAi openAi = properties.getOpenai();
        if (!StringUtils.hasText(openAi.getApiKey())) {
            throw new IllegalStateException("OPENAI_API_KEY is required when ai.provider=openai");
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(openAi.getTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(openAi.getTimeout());

        return RestClient.builder()
                .baseUrl(openAi.getBaseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("Authorization", "Bearer " + openAi.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
