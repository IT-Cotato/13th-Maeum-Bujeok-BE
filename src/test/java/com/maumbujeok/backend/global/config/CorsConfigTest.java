package com.maumbujeok.backend.global.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(CorsConfigTest.TestCorsControllerConfig.class)
class CorsConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void allowsSmsSendPreflightFromConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/sms/send")
                        .header(HttpHeaders.ORIGIN, "https://13th-maeum-bujeok.vercel.app")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://13th-maeum-bujeok.vercel.app"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void allowsSmsSendPreflightFromProductionApiSwaggerOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/sms/send")
                        .header(HttpHeaders.ORIGIN, "https://maumbujeok.p-e.kr")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://maumbujeok.p-e.kr"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void allowsSmsSendPreflightFromLocalFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/sms/send")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "http://localhost:3000"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @Test
    void includesCorsHeadersOnSmsSendPostResponse() throws Exception {
        mockMvc.perform(post("/api/auth/test/cors/echo")
                        .header(HttpHeaders.ORIGIN, "https://13th-maeum-bujeok.vercel.app")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"01012345678\"}"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                        "https://13th-maeum-bujeok.vercel.app"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
    }

    @TestConfiguration
    static class TestCorsControllerConfig {
        @Bean
        TestCorsController testCorsController() {
            return new TestCorsController();
        }
    }

    @RestController
    static class TestCorsController {
        @PostMapping("/api/auth/test/cors/echo")
        String echo(@RequestBody String body) {
            return body;
        }
    }
}
