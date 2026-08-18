package com.maumbujeok.backend.global.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FrontendUrlProvider {

    private static final String DEFAULT_FRONTEND_BASE_URL = "https://13th-maeum-bujeok.vercel.app";
    private static final String PROD_FRONTEND_BASE_URL = DEFAULT_FRONTEND_BASE_URL;
    private static final String PROD_API_BASE_URL = "https://maumbujeok.p-e.kr";
    private static final String LOCAL_FRONTEND_BASE_URL = "http://localhost:3000";

    private final String frontendBaseUrl;

    public FrontendUrlProvider(
            @Value("${FRONTEND_BASE_URL:https://13th-maeum-bujeok.vercel.app}") String frontendBaseUrl
    ) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public String baseUrl() {
        return frontendBaseUrl;
    }

    public List<String> allowedOrigins() {
        return List.of(frontendBaseUrl, PROD_FRONTEND_BASE_URL, PROD_API_BASE_URL, LOCAL_FRONTEND_BASE_URL).stream()
                .distinct()
                .toList();
    }

    public String oauthCallbackUrl() {
        return UriComponentsBuilder.fromUriString(baseUrl())
                .path("/oauth/callback")
                .build()
                .toUriString();
    }
}
