package com.maumbujeok.backend.domain.report.controller;

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
class ReportOpenApiTest {
    @Autowired MockMvc mockMvc;

    @Test
    void exposesWeeklyReportGenerationSpecification() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/reports/weekly/generate'].post.summary").value("주간 감정 리포트 요약 생성"))
                .andExpect(jsonPath("$.paths['/api/reports/weekly/generate'].post.security[0].JWT_TOKEN").isArray())
                .andExpect(jsonPath("$.paths['/api/reports/weekly/generate'].post.responses['200'].content['application/json'].schema").exists())
                .andExpect(jsonPath("$.paths['/api/reports/weekly/generate'].post.responses['200'].content['application/json'].examples['일기 없음']").exists())
                .andExpect(jsonPath("$.paths['/api/reports/weekly-summary/{summaryId}'].get.summary").value("주간 리포트 요약 결과 조회"))
                .andExpect(jsonPath("$.paths['/api/reports/weekly-summary/{summaryId}'].get.responses['200'].content['application/json'].examples['생성 실패']").exists())
                .andExpect(jsonPath("$.paths['/api/reports/weekly-summary/{summaryId}'].get.responses['404'].content['application/json'].example.code").value("REPORT_404"))
                .andExpect(jsonPath("$.components.schemas.GenerateWeeklyReportApiResponse.properties.data['$ref']")
                        .value("#/components/schemas/GenerateWeeklyReportResponse"))
                .andExpect(jsonPath("$.components.schemas.WeeklyReportSummaryApiResponse.properties.data['$ref']")
                        .value("#/components/schemas/WeeklyReportSummaryResponse"));
    }
    @Test
    void exposesWeeklyQueryPeriodsAndRegenerateSpecifications() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/reports/weekly'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/reports/weeks'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/reports/{reportId}/regenerate'].post.responses['200']").exists())
                .andExpect(jsonPath("$.components.schemas.WeeklyReportPeriodsApiResponse.properties.data.type").value("array"));
    }
    @Test
    void exposesReportScreenSpecifications() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/reports/weekly/{reportId}/summary'].get").exists())
                .andExpect(jsonPath("$.paths['/api/reports/weekly/{reportId}/emotion-stats'].get").exists())
                .andExpect(jsonPath("$.paths['/api/reports/weekly/{reportId}/burnings'].get").exists())
                .andExpect(jsonPath("$.paths['/api/reports/weekly/{reportId}/talismans'].get").exists())
                .andExpect(jsonPath("$.paths['/api/reports/weekly/{reportId}/next-week-flow'].get").exists());
    }}
