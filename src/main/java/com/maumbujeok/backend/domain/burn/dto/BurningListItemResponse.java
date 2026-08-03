package com.maumbujeok.backend.domain.burn.dto;
import com.maumbujeok.backend.domain.burn.domain.*;
import java.time.LocalDateTime;
public record BurningListItemResponse(Long burningId, BurningSourceType sourceType, LocalDateTime burnedAt, BurningAnalysisStatus analysisStatus, boolean hasTalisman) {}
