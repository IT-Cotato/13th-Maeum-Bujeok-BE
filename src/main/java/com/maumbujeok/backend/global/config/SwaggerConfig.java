// src/main/java/com/maumbujeok/backend/global/config/SwaggerConfig.java
package com.maumbujeok.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = "JWT_TOKEN";
        
        // 전역 보안 요구사항 추가
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        
        // JWT 인증 스킴 정의
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        // API 메타데이터 설정
        return new OpenAPI()
                .info(new Info()
                        .title("마음부적 API 명세서")
                        .description("마음부적 서비스 백엔드 API 명세서")
                        .version("v1.0.0"))
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}