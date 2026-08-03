package com.maumbujeok.backend.domain.burn.dto;
import com.maumbujeok.backend.domain.burn.domain.BurningSourceType;
public record CreateBurningRequest(BurningSourceType sourceType, String content, Long diaryId) {}
