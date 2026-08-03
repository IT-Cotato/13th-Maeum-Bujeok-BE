package com.maumbujeok.backend.domain.burn.dto;
import com.maumbujeok.backend.domain.talisman.dto.TalismanItemResponse;
public record TalismanCreationResponse(boolean hasTalisman, TalismanItemResponse talisman) {}