package com.maumbujeok.backend.global.config;

import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FrontendUrlProvider {

    private static final String PROD_FRONTEND_BASE_URL = "https://13th-maeum-bujeok.vercel.app";
    private static final String LOCAL_FRONTEND_BASE_URL = "http://localhost:3000";

    public String baseUrl() {
        return PROD_FRONTEND_BASE_URL;
    }

    public List<String> allowedOrigins() {
        return List.of(PROD_FRONTEND_BASE_URL, LOCAL_FRONTEND_BASE_URL);
    }

    public String oauthCallbackUrl() {
        return UriComponentsBuilder.fromUriString(baseUrl())
                .path("/oauth/callback")
                .build()
                .toUriString();
    }
}
