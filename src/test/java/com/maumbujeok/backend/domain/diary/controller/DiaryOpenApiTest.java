package com.maumbujeok.backend.domain.diary.controller;

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
    @Autowired
    MockMvc mockMvc;

    @Test
    void exposesFrontendFriendlyDiarySpecification() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/diaries'].post.summary").value("일기 작성"))
                .andExpect(jsonPath("$.paths['/api/diaries'].post.security[0].JWT_TOKEN").isArray())
                .andExpect(jsonPath("$.paths['/api/diaries'].post.responses['200'].content['application/json'].schema").exists())
                .andExpect(jsonPath("$.paths['/api/diaries'].post.responses['400'].content['application/json'].examples['일기 내용 오류']").exists())
                .andExpect(jsonPath("$.paths['/api/diaries'].get.responses['200'].content['application/json'].example.data").isArray())
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].get.summary").value("일기 단건 조회"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].patch.summary").value("일기 수정"))
                .andExpect(jsonPath("$.paths['/api/diaries/emotion-stats'].get.summary").value("대표 감정 통계 조회"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}/analysis'].get.responses['200'].content['application/json'].examples['분석 완료']").exists())
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}/analysis'].get.responses['404'].content['application/json'].example.code").value("DIARY_404"))
                .andExpect(jsonPath("$.components.schemas.CreateDiaryApiResponse.properties.data['$ref']").value("#/components/schemas/CreateDiaryResponse"))
                .andExpect(jsonPath("$.components.schemas.DiaryListApiResponse.properties.data.items['$ref']").value("#/components/schemas/DiaryResponse"))
                .andExpect(jsonPath("$.components.schemas.DiaryDetailApiResponse.properties.data['$ref']").value("#/components/schemas/DiaryResponse"))
                .andExpect(jsonPath("$.components.schemas.UpdateDiaryApiResponse.properties.data['$ref']").value("#/components/schemas/UpdateDiaryResponse"))
                .andExpect(jsonPath("$.components.schemas.DiaryEmotionStatsApiResponse.properties.data.items['$ref']").value("#/components/schemas/EmotionStatResponse"))
                .andExpect(jsonPath("$.components.schemas.DiaryAnalysisApiResponse.properties.data['$ref']").value("#/components/schemas/DiaryAnalysisResponse"));
    }
}
