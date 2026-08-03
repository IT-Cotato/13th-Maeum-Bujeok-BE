package com.maumbujeok.backend.domain.burn.controller;

import com.maumbujeok.backend.domain.burn.application.BurningService;
import com.maumbujeok.backend.domain.burn.dto.*;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/burnings")
@RequiredArgsConstructor
@Tag(name = "Burning API", description = "Burning records and talisman APIs")
@SecurityRequirement(name = "JWT_TOKEN")
public class BurningController {
    private final BurningService service;

    @Operation(summary = "Start burning", description = "Burn direct content or one owned diary.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Burning started"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Diary already burned")})
    @PostMapping
    public ApiResponse<CreateBurningResponse> create(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user, @RequestBody CreateBurningRequest request) {
        return ApiResponse.onSuccess(service.create(user.getMember().getPhoneNumber(), request));
    }

    @Operation(summary = "List burnings", description = "Cursor-based burning history. Source text is excluded.")
    @GetMapping
    public ApiResponse<BurningListResponse> list(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user, @RequestParam(required = false) Long cursor, @RequestParam(required = false) Integer size) {
        return ApiResponse.onSuccess(service.getPage(user.getMember().getPhoneNumber(), cursor, size));
    }

    @Operation(summary = "Get burning detail", description = "Returns analysis result and talisman status without source text.")
    @GetMapping("/{burningId}")
    public ApiResponse<BurningDetailResponse> get(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user, @PathVariable Long burningId) {
        return ApiResponse.onSuccess(service.get(user.getMember().getPhoneNumber(), burningId));
    }

    @Operation(summary = "Get burning analysis")
    @GetMapping("/{burningId}/analysis")
    public ApiResponse<BurningAnalysisResponse> analysis(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user, @PathVariable Long burningId) {
        return ApiResponse.onSuccess(service.getAnalysis(user.getMember().getPhoneNumber(), burningId));
    }

    @Operation(summary = "Create talisman", description = "Creates one talisman after burning analysis completes.")
    @ApiResponses({@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Talisman created"), @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Analysis incomplete or talisman already exists")})
    @PostMapping("/{burningId}/talisman")
    public ApiResponse<TalismanCreationResponse> createTalisman(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user, @PathVariable Long burningId) {
        return ApiResponse.onSuccess(service.createTalisman(user.getMember().getPhoneNumber(), burningId));
    }
}