package com.maumbujeok.backend.global.config;

import com.maumbujeok.backend.global.security.JwtAuthenticationFilter;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import com.maumbujeok.backend.global.security.oauth2.CustomOAuth2UserService;
import com.maumbujeok.backend.global.security.oauth2.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 기본 보안 설정 비활성화 (JWT 사용 목적)
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // 세션 정책: STATELESS (서버에 세션 저장 X)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // API 엔드포인트 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 1. 접근 허용 (permitAll) 엔드포인트
                .requestMatchers(
                    "/api/auth/**",
                    "/api/local-uploads/**",
                    "/oauth/**",
                    "/login/oauth2/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                // 2. 인증 필수 (authenticated) 엔드포인트
                .requestMatchers(
                    "/api/diaries/**",
                    "/api/members/**",
                    "/api/reports/**",
                    "/api/talismans/**"
                ).authenticated()
                // 3. 그 외 모든 요청은 인증 필수
                .anyRequest().authenticated()
            )
            
            // 비인증 사용자 차단 시 403 Forbidden 응답 처리
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")
                )
            )

            // OAuth2 로그인 설정
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            )

            // JWT 인증 필터 등록
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}