package com.maumbujeok.backend.domain.burn.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BurningOpenApiTest {
    @Autowired MockMvc mockMvc;

    @Test
    void exposesBurningAndTalismanOperations() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/burnings'].post").exists())
                .andExpect(jsonPath("$.paths['/api/burnings'].get").exists())
                .andExpect(jsonPath("$.paths['/api/burnings/{burningId}'].get").exists())
                .andExpect(jsonPath("$.paths['/api/burnings/{burningId}/analysis'].get").exists())
                .andExpect(jsonPath("$.paths['/api/burnings/{burningId}/talisman'].post").exists())
                .andExpect(jsonPath("$.paths['/api/burnings/{burningId}/talisman'].post.responses['409']").exists());
    }
}