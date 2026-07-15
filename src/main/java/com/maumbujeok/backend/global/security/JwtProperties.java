package com.maumbujeok.backend.global.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.time.Duration;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration,
        String issuer
) {

    public JwtProperties {
        if (!StringUtils.hasText(secret)) {
            throw new IllegalArgumentException("JWT secret must not be blank");
        }
        validatePositive(accessTokenExpiration, "JWT access token expiration");
        validatePositive(refreshTokenExpiration, "JWT refresh token expiration");
        if (!StringUtils.hasText(issuer)) {
            throw new IllegalArgumentException("JWT issuer must not be blank");
        }
    }

    private static void validatePositive(Duration duration, String propertyName) {
        if (duration == null || duration.isNegative() || duration.isZero() || duration.toMillis() <= 0) {
            throw new IllegalArgumentException(propertyName + " must be positive");
        }
    }
}
