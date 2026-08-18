package com.maumbujeok.backend.domain.diary.controller;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DiaryOpenApiTest {
    @Autowired MockMvc mockMvc;

    @Test
    void exposesDiaryAndUploadSpecification() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/diaries'].post.summary").value("\uC77C\uAE30 \uC0DD\uC131"))
                .andExpect(jsonPath("$.paths['/api/diaries'].get.summary").value("\uC77C\uAE30 \uCEE4\uC11C \uBAA9\uB85D \uC870\uD68C"))
                .andExpect(jsonPath("$.paths['/api/diaries/calendar'].get.summary").value("\uC6D4\uBCC4 \uC77C\uAE30 \uCE98\uB9B0\uB354 \uC870\uD68C"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].get.summary").value("\uC77C\uAE30 \uC0C1\uC138 \uC870\uD68C"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].patch.summary").value("\uC77C\uAE30 \uC218\uC815"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/diaries'].get.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/diaries/by-date'].get.summary").value("\uAE30\uB85D\uC77C\uBCC4 \uC77C\uAE30 \uBAA9\uB85D \uC870\uD68C"))
                .andExpect(jsonPath("$.paths['/api/diaries/by-date'].get.responses['200'].content['application/json'].schema['$ref']").value("#/components/schemas/DiaryDateListApiResponse"))
                .andExpect(jsonPath("$.paths['/api/diaries/calendar'].get.responses['200'].content['application/json'].schema['$ref']").value("#/components/schemas/DiaryCalendarApiResponse"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].get.responses['200'].content['application/json'].schema['$ref']").value("#/components/schemas/DiaryDetailApiResponse"))
                .andExpect(jsonPath("$.paths['/api/diaries/emotion-stats'].get.responses['200'].content['application/json'].schema['$ref']").value("#/components/schemas/DiaryEmotionStatsApiResponse"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}/analysis'].get.responses['200'].content['application/json'].schema['$ref']").value("#/components/schemas/DiaryAnalysisApiResponse"))
                .andExpect(jsonPath("$.paths['/api/uploads/presigned-url'].post.summary").value("\uC77C\uAE30 \uC774\uBBF8\uC9C0 \uC5C5\uB85C\uB4DC URL \uBC1C\uAE09"))
                .andExpect(jsonPath("$.paths['/api/uploads/presigned-url'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/uploads/{uploadId}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.components.schemas.CreateDiaryRequest.required", hasItems("content", "selectedEmotion")))
                .andExpect(jsonPath("$.components.schemas.CreateDiaryRequest.properties.content.maxLength").value(5000))
                .andExpect(jsonPath("$.components.schemas.DiaryCalendarDayResponse.properties.status.enum", hasItems("STORED", "BURNED")))
                .andExpect(jsonPath("$.components.schemas.PresignedUrlRequest.properties.fileSize.maximum").value(10485760))
                .andExpect(jsonPath("$.components.securitySchemes.JWT_TOKEN.scheme").value("bearer"));
    }
}
