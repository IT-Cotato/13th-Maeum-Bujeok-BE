package com.maumbujeok.backend.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private static final String ISSUER = "maumbujeok";
    private static final String TEST_SECRET = encodeBase64("01234567890123456789012345678901");

    @Test
    void createsAndValidatesAccessToken() {
        JwtTokenProvider provider = createProvider(TEST_SECRET, ISSUER);

        String token = provider.createToken("user@example.com", "ROLE_USER");

        assertTrue(provider.validateToken(token));
        assertEquals("user@example.com", provider.getUserEmail(token));

        Claims claims = parseClaims(token, TEST_SECRET, ISSUER);
        assertEquals("ROLE_USER", claims.get("role", String.class));
        assertEquals(ISSUER, claims.getIssuer());
        assertEquals(Duration.ofMinutes(30).toMillis(),
                claims.getExpiration().getTime() - claims.getIssuedAt().getTime());
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenProvider provider = createProvider(TEST_SECRET, ISSUER);
        String token = createToken(TEST_SECRET, ISSUER, new Date(System.currentTimeMillis() - 1_000));

        assertFalse(provider.validateToken(token));
    }

    @Test
    void rejectsTokenSignedWithDifferentKey() {
        JwtTokenProvider provider = createProvider(TEST_SECRET, ISSUER);
        String otherSecret = encodeBase64("abcdefghijklmnopqrstuvwxyz123456");
        String token = createToken(otherSecret, ISSUER, new Date(System.currentTimeMillis() + 60_000));

        assertFalse(provider.validateToken(token));
    }

    @Test
    void rejectsTokenWithDifferentIssuer() {
        JwtTokenProvider provider = createProvider(TEST_SECRET, ISSUER);
        String token = createToken(TEST_SECRET, "other-service", new Date(System.currentTimeMillis() + 60_000));

        assertFalse(provider.validateToken(token));
    }

    @Test
    void rejectsMalformedOrBlankToken() {
        JwtTokenProvider provider = createProvider(TEST_SECRET, ISSUER);

        assertFalse(provider.validateToken("not-a-jwt"));
        assertFalse(provider.validateToken(""));
    }

    @Test
    void validatesJwtProperties() {
        assertThrows(IllegalArgumentException.class,
                () -> new JwtProperties(" ", Duration.ofMinutes(30), Duration.ofDays(14), ISSUER));
        assertThrows(IllegalArgumentException.class,
                () -> new JwtProperties(TEST_SECRET, Duration.ZERO, Duration.ofDays(14), ISSUER));
        assertThrows(IllegalArgumentException.class,
                () -> new JwtProperties(TEST_SECRET, Duration.ofMinutes(30), Duration.ofDays(-1), ISSUER));
        assertThrows(IllegalArgumentException.class,
                () -> new JwtProperties(TEST_SECRET, Duration.ofMinutes(30), Duration.ofDays(14), " "));
    }

    @Test
    void rejectsInvalidOrWeakBase64Secret() {
        assertThrows(IllegalStateException.class, () -> createProvider("not-base64%%%", ISSUER));
        assertThrows(IllegalStateException.class,
                () -> createProvider(encodeBase64("too-short"), ISSUER));
    }

    @Test
    void usesConfiguredRefreshTokenExpiration() {
        JwtProperties properties = new JwtProperties(
                TEST_SECRET,
                Duration.ofMinutes(30),
                Duration.ofDays(14),
                ISSUER
        );

        assertEquals(Duration.ofDays(14), properties.refreshTokenExpiration());
    }

    private static JwtTokenProvider createProvider(String secret, String issuer) {
        JwtProperties properties = new JwtProperties(
                secret,
                Duration.ofMinutes(30),
                Duration.ofDays(14),
                issuer
        );
        JwtTokenProvider provider = new JwtTokenProvider(null, properties);
        provider.init();
        return provider;
    }

    private static String createToken(String secret, String issuer, Date expiration) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        Date issuedAt = new Date(expiration.getTime() - 60_000);

        return Jwts.builder()
                .setSubject("user@example.com")
                .setIssuer(issuer)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private static Claims parseClaims(String token, String secret, String issuer) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private static String encodeBase64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
