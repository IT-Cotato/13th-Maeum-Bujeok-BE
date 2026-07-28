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
                .andExpect(jsonPath("$.paths['/api/diaries'].post.summary").value("일기 작성"))
                .andExpect(jsonPath("$.paths['/api/diaries'].get.summary").value("내 일기 커서 목록 조회"))
                .andExpect(jsonPath("$.paths['/api/diaries/calendar'].get.summary").value("월별 일기 달력 조회"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].get.summary").value("일기 상세 조회"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].patch.summary").value("일기 수정"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/diaries'].get.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/diaries/by-date'].get.summary").value("기록일별 일기 목록 조회"))
                .andExpect(jsonPath("$.paths['/api/diaries/by-date'].get.responses['200'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/DiaryDateListApiResponse"))
                .andExpect(jsonPath("$.paths['/api/diaries/calendar'].get.responses['200'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/DiaryCalendarApiResponse"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].get.responses['200'].content['application/json'].schema['$ref']")
                        .value("#/components/schemas/DiaryDetailApiResponse"))
                .andExpect(jsonPath("$.paths['/api/uploads/presigned-url'].post.summary").value("일기 이미지 업로드 URL 발급"))
                .andExpect(jsonPath("$.paths['/api/uploads/presigned-url'].post.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/api/uploads/{uploadId}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.components.schemas.CreateDiaryRequest.required", hasItems("content", "selectedEmotion")))
                .andExpect(jsonPath("$.components.schemas.CreateDiaryRequest.properties.content.maxLength").value(5000))
                .andExpect(jsonPath("$.components.schemas.DiaryCalendarDayResponse.properties.status.enum", hasItems("STORED", "BURNED")))
                .andExpect(jsonPath("$.components.schemas.PresignedUrlRequest.properties.fileSize.maximum").value(10485760))
                .andExpect(jsonPath("$.components.securitySchemes.JWT_TOKEN.scheme").value("bearer"));
    }
}
