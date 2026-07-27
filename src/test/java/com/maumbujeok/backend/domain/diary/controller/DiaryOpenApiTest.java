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
    @Autowired MockMvc mockMvc;

    @Test
    void exposesDiaryAndUploadSpecification() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/diaries'].post.summary").value("일기 작성"))
                .andExpect(jsonPath("$.paths['/api/diaries'].get.summary").value("내 일기 목록 조회"))
                .andExpect(jsonPath("$.paths['/api/diaries/calendar'].get.summary").value("월별 일기 달력 조회"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].get.summary").value("일기 단건 조회"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].patch.summary").value("일기 수정"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}'].delete.summary").value("일기 삭제"))
                .andExpect(jsonPath("$.paths['/api/diaries/{diaryId}/analysis'].get.summary").value("일기 AI 분석 결과 조회"))
                .andExpect(jsonPath("$.paths['/api/uploads/presigned-url'].post").exists())
                .andExpect(jsonPath("$.paths['/api/uploads/{uploadId}'].delete").exists());
    }
}