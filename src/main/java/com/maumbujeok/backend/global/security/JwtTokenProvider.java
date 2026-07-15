package com.maumbujeok.backend.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final CustomUserDetailsService userDetailsService;
    private final JwtProperties jwtProperties;

    private SecretKey key;
    private JwtParser jwtParser;

    @PostConstruct
    protected void init() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        } catch (DecodingException e) {
            throw new IllegalStateException("JWT secret must be a valid Base64 value", e);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes after Base64 decoding");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.jwtParser = Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(jwtProperties.issuer())
                .build();
    }

    // 토큰 생성
    public String createToken(String email, String role) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtProperties.accessTokenExpiration().toMillis());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuer(jwtProperties.issuer())
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserEmail(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에서 회원 이메일 추출
    public String getUserEmail(String token) {
        return jwtParser.parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰 유효성 및 만료일자 확인
    public boolean validateToken(String token) {
        try {
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.debug("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰입니다.");
        } catch (IncorrectClaimException e) {
            log.debug("JWT 발급자가 올바르지 않습니다.");
        } catch (UnsupportedJwtException e) {
            log.debug("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.debug("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}
