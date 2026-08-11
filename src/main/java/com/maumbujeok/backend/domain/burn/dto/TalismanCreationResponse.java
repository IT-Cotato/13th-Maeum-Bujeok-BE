package com.maumbujeok.backend.domain.burn.dto;
import com.maumbujeok.backend.domain.talisman.dto.TalismanItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;
@Schema(name="TalismanCreationResponse", description="\uBD80\uC801 \uC0DD\uC131 \uACB0\uACFC")
public record TalismanCreationResponse(
 @Schema(description="\uBD80\uC801 \uC0DD\uC131 \uC5EC\uBD80", example="true") boolean hasTalisman,
 @Schema(description="\uC0DD\uC131\uB41C \uBD80\uC801 \uC815\uBCF4") TalismanItemResponse talisman
) {}