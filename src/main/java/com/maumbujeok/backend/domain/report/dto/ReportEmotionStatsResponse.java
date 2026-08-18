package com.maumbujeok.backend.domain.report.dto;

import com.maumbujeok.backend.domain.diary.dto.EmotionStatResponse;
import java.time.LocalDate;
import java.util.List;

public record ReportEmotionStatsResponse(LocalDate periodStart, LocalDate periodEnd, List<EmotionStatResponse> stats) {}