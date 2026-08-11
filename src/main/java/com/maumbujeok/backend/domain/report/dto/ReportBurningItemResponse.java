package com.maumbujeok.backend.domain.report.dto;

import com.maumbujeok.backend.domain.burn.domain.BurningAnalysisStatus;
import com.maumbujeok.backend.domain.burn.domain.BurningSourceType;
import java.time.LocalDateTime;

public record ReportBurningItemResponse(Long burningId, String title, BurningSourceType sourceType, LocalDateTime burnedAt, BurningAnalysisStatus analysisStatus, boolean hasTalisman) {}