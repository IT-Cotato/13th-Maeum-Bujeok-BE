package com.maumbujeok.backend.domain.talisman.dto;

import java.util.List;

public record TalismanListResponse(
        List<TalismanItemResponse> items,
        int count,
        boolean hasNext,
        Long nextCursor
) {
}
