package com.maumbujeok.backend.global.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class FrontendUrlProvider {

    @Value("${cors.allowed-origins:https://13th-maeum-bujeok.vercel.app,http://localhost:3000,https://localhost:3000}")
    private List<String> allowedOrigins;

    public String baseUrl() {
        return allowedOrigins != null && !allowedOrigins.isEmpty() ? allowedOrigins.get(0) : "https://13th-maeum-bujeok.vercel.app";
    }

    public List<String> allowedOrigins() {
        return allowedOrigins;
    }

    public String oauthCallbackUrl() {
        return UriComponentsBuilder.fromUriString(baseUrl())
                .path("/oauth/callback")
                .build()
                .toUriString();
    }
}
