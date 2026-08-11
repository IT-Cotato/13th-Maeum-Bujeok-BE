package com.maumbujeok.backend.domain.burn.controller;

import com.maumbujeok.backend.domain.burn.application.BurningService;
import com.maumbujeok.backend.domain.burn.dto.*;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "\uC18C\uAC01 API", description = "\uAE30\uC5B5 \uC18C\uAC01, \uBD84\uC11D, \uBD80\uC801 \uC0DD\uC131 API")
@SecurityRequirement(name = "JWT_TOKEN")
public class BurningController {
    private final BurningService service;

    @Operation(summary = "\uC18C\uAC01 \uC2DC\uC791", description = "\uC9C1\uC811 \uC785\uB825 \uB610\uB294 \uC77C\uAE30\uB97C \uC18C\uAC01\uD569\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uC18C\uAC01 \uC2DC\uC791 \uC131\uACF5", content = @Content(schema = @Schema(implementation = BurningSwaggerSchemas.CreateBurningApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "BURN_400: \uC694\uCCAD \uD615\uC2DD\uC774 \uC798\uBABB\uB418\uAC70\uB098 sourceType\uBCC4 \uC785\uB825 \uADDC\uCE59\uC744 \uC5B4\uAE34 \uACBD\uC6B0"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D\uB41C \uC0AC\uC6A9\uC790\uC758 \uC694\uCCAD\uC774 \uC544\uB2CC \uACBD\uC6B0"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "BURN_404: \uC9C1\uC811 \uC785\uB825\uC5D0\uC11C \uC9C0\uC815\uD55C \uC77C\uAE30\uB97C \uCC3E\uC9C0 \uBABB\uD55C \uACBD\uC6B0"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "BURN_409: \uC774\uBBF8 \uC18C\uAC01\uB41C \uC77C\uAE30\uC778 \uACBD\uC6B0")
    })
    @PostMapping
    public ApiResponse<CreateBurningResponse> create(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user, @RequestBody CreateBurningRequest request) {
        return ApiResponse.onSuccess(service.create(user.getMember().getPhoneNumber(), request));
    }

    @Operation(summary = "\uC18C\uAC01 \uAE30\uB85D \uBAA9\uB85D \uC870\uD68C", description = "\uC18C\uAC01 \uC6D0\uBB38\uC744 \uC81C\uC678\uD558\uACE0 \uCD5C\uC2E0\uC21C\uC73C\uB85C \uC870\uD68C\uD569\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uC18C\uAC01 \uAE30\uB85D \uBAA9\uB85D \uC870\uD68C \uC131\uACF5", content = @Content(schema = @Schema(implementation = BurningSwaggerSchemas.BurningListApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "BURN_400: size\uAC00 1\uC774\uC0C1 50\uC774\uD558\uAC00 \uC544\uB2CC \uACBD\uC6B0"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D\uB41C \uC0AC\uC6A9\uC790\uC758 \uC694\uCCAD\uC774 \uC544\uB2CC \uACBD\uC6B0")
    })
    @GetMapping
    public ApiResponse<BurningListResponse> list(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user, @Parameter(description = "\uB2E4\uC74C \uD398\uC774\uC9C0 \uCEE4\uC11C", example = "17") @RequestParam(required = false) Long cursor, @Parameter(description = "\uD398\uC774\uC9C0 \uD06C\uAE30. \uAE30\uBCF8 20, \uCD5C\uB300 50", example = "20") @RequestParam(required = false) Integer size) {
        return ApiResponse.onSuccess(service.getPage(user.getMember().getPhoneNumber(), cursor, size));
    }

    @Operation(summary = "\uC18C\uAC01 \uC0C1\uC138 \uC870\uD68C", description = "\uC18C\uAC01 \uC6D0\uBB38\uC744 \uC81C\uC678\uD558\uACE0 \uBD84\uC11D \uACB0\uACFC\uC640 \uBD80\uC801 \uC0C1\uD0DC\uB97C \uC870\uD68C\uD569\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uC18C\uAC01 \uC0C1\uC138 \uC870\uD68C \uC131\uACF5", content = @Content(schema = @Schema(implementation = BurningSwaggerSchemas.BurningDetailApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D\uB41C \uC0AC\uC6A9\uC790\uC758 \uC694\uCCAD\uC774 \uC544\uB2CC \uACBD\uC6B0"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "BURN_404: \uD574\uB2F9 \uC18C\uAC01 \uAE30\uB85D\uC744 \uCC3E\uC744 \uC218 \uC5C6\uC740 \uACBD\uC6B0")
    })
    @GetMapping("/{burningId}")
    public ApiResponse<BurningDetailResponse> get(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user, @Parameter(description = "\uC18C\uAC01 \uAE30\uB85D ID", example = "17") @PathVariable Long burningId) {
        return ApiResponse.onSuccess(service.get(user.getMember().getPhoneNumber(), burningId));
    }

    @Operation(summary = "\uC18C\uAC01 \uBD84\uC11D \uC870\uD68C", description = "\uC18C\uAC01 AI \uBD84\uC11D\uC758 \uC0C1\uD0DC\uC640 \uACB0\uACFC\uB97C \uC870\uD68C\uD569\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uBD84\uC11D \uC0C1\uD0DC \uBC0F \uACB0\uACFC \uC870\uD68C \uC131\uACF5", content = @Content(schema = @Schema(implementation = BurningSwaggerSchemas.BurningAnalysisApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D\uB41C \uC0AC\uC6A9\uC790\uC758 \uC694\uCCAD\uC774 \uC544\uB2CC \uACBD\uC6B0"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "BURN_404: \uD574\uB2F9 \uC18C\uAC01 \uAE30\uB85D\uC744 \uCC3E\uC744 \uC218 \uC5C6\uC74C")
    })
    @GetMapping("/{burningId}/analysis")
    public ApiResponse<BurningAnalysisResponse> analysis(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user, @Parameter(description = "\uC18C\uAC01 \uAE30\uB85D ID", example = "17") @PathVariable Long burningId) {
        return ApiResponse.onSuccess(service.getAnalysis(user.getMember().getPhoneNumber(), burningId));
    }

    @Operation(summary = "\uBD80\uC801 \uC0DD\uC131", description = "\uC18C\uAC01 \uBD84\uC11D \uC644\uB8CC \uD6C4 \uBD80\uC801\uC744 \uCD5C\uCD08 1\uD68C \uC0DD\uC131\uD569\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uBD80\uC801 \uC0DD\uC131 \uC131\uACF5", content = @Content(schema = @Schema(implementation = BurningSwaggerSchemas.TalismanCreationApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D\uB41C \uC0AC\uC6A9\uC790\uC758 \uC694\uCCAD\uC774 \uC544\uB2CC \uACBD\uC6B0"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "BURN_404: \uC18C\uAC01 \uAE30\uB85D\uC744 \uCC3E\uC744 \uC218 \uC5C6\uC74C"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "BURN_410: \uBD84\uC11D \uBBF8\uC644\uB8CC \uB610\uB294 TALISMAN_409: \uBD80\uC801 \uC911\uBCF5 \uC0DD\uC131")
    })
    @PostMapping("/{burningId}/talisman")
    public ApiResponse<TalismanCreationResponse> createTalisman(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails user, @Parameter(description = "\uC18C\uAC01 \uAE30\uB85D ID", example = "17") @PathVariable Long burningId) {
        return ApiResponse.onSuccess(service.createTalisman(user.getMember().getPhoneNumber(), burningId));
    }
}
