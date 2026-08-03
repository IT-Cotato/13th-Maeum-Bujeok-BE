package com.maumbujeok.backend.domain.burn.dto;
import com.maumbujeok.backend.domain.burn.domain.BurningAnalysisStatus;
import java.time.LocalDateTime;
public record BurningAnalysisResponse(Long burningId, BurningAnalysisStatus status, int inputRevision, String comment, String talismanType, String talismanText, LocalDateTime completedAt) {}
