package com.maumbujeok.backend.domain.burn.dto;
import java.util.List;
public record BurningListResponse(List<BurningListItemResponse> items, Long nextCursor, boolean hasNext) {}
