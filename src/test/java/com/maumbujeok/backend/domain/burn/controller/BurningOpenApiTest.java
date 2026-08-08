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
import static org.hamcrest.Matchers.hasItem;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BurningOpenApiTest {
    @Autowired MockMvc mockMvc;

    @Test
    void exposesBurningOperationsWithActualStatusCodes() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/burnings'].post.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings'].post.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings'].post.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings'].get.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings'].get.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings/{burningId}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings/{burningId}/analysis'].get.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings/{burningId}/talisman'].post.responses['403']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings/{burningId}/talisman'].post.responses['404']").exists())
                .andExpect(jsonPath("$.paths['/api/burnings/{burningId}/talisman'].post.responses['409']").exists())
                .andExpect(jsonPath("$.components.schemas.BurningAnalysisResponse.properties.talismanType.type").value(hasItem("integer")))
                .andExpect(jsonPath("$.components.schemas.BurningDetailResponse.properties.talismanType.type").value(hasItem("integer")));
    }
}
