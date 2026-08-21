package com.maumbujeok.backend.global.config;

import com.maumbujeok.backend.global.security.JwtAuthenticationFilter;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import com.maumbujeok.backend.global.security.oauth2.CustomOAuth2UserService;
import com.maumbujeok.backend.global.security.oauth2.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
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
            .cors(Customizer.withDefaults())
            // 湲곕낯 蹂댁븞 ?ㅼ젙 鍮꾪솢?깊솕 (JWT ?ъ슜 紐⑹쟻)
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // JWT API는 세션을 사용하지 않지만 OAuth2 로그인은 Google callback의 state 검증을 위해 authorization request를 세션에 잠시 저장해야 한다.
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            
            // API ?붾뱶?ъ씤???묎렐 沅뚰븳 ?ㅼ젙
            .authorizeHttpRequests(auth -> auth
                // CORS preflight ?붿껌 ?덉슜
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 1. ?묎렐 ?덉슜 (permitAll) ?붾뱶?ъ씤??
                .requestMatchers(
                    "/api/auth/**",
                    "/api/local-uploads/**",
                    "/oauth/**",
                    "/login/oauth2/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/actuator/health",
                    "/actuator/prometheus"
                ).permitAll()
                // 2. ?몄쬆 ?꾩닔 (authenticated) ?붾뱶?ъ씤??
                .requestMatchers(
                    "/api/diaries/**",
                    "/api/members/**",
                    "/api/saju/**",
                    "/api/reports/**",
                    "/api/talismans/**",
                    "/api/burnings/**"
                ).authenticated()
                // 3. 洹???紐⑤뱺 ?붿껌? ?몄쬆 ?꾩닔
                .anyRequest().authenticated()
            )
            
            // 鍮꾩씤利??ъ슜??李⑤떒 ??403 Forbidden ?묐떟 泥섎━
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")
                )
            )

            // OAuth2 濡쒓렇???ㅼ젙
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2SuccessHandler)
            )

            // JWT ?몄쬆 ?꾪꽣 ?깅줉
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

