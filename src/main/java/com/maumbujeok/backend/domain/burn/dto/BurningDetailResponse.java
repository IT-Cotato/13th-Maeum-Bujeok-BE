package com.maumbujeok.backend.domain.burn.dto;
import com.maumbujeok.backend.domain.burn.domain.*;
import java.time.LocalDateTime;
public record BurningDetailResponse(Long burningId, BurningSourceType sourceType, LocalDateTime burnedAt, BurningAnalysisStatus analysisStatus, String comment, String talismanType, String talismanText, boolean hasTalisman) {}
