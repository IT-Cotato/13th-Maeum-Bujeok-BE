package com.maumbujeok.backend.domain.talisman.controller;

import com.maumbujeok.backend.domain.talisman.dto.TalismanListResponse;
import com.maumbujeok.backend.domain.talisman.service.TalismanService;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/talismans")
@RequiredArgsConstructor
@Tag(name = "\uBD80\uC801 API", description = "\uB85C\uADF8\uC778\uD55C \uC0AC\uC6A9\uC790\uC758 \uBD80\uC801 \uBAA9\uB85D API")
@SecurityRequirement(name = "JWT_TOKEN")
public class TalismanController {
    private final TalismanService talismanService;

    @Operation(
            summary = "\uB0B4 \uBD80\uC801 \uBAA9\uB85D \uC870\uD68C",
            description = "\uD604\uC7AC \uC8FC\uAC04(\uC6D4\uC694\uC77C~\uC77C\uC694\uC77C)\uC5D0 \uC0DD\uC131\uB41C \uBD80\uC801\uC744 \uCEE4\uC11C \uAE30\uBC18\uC73C\uB85C \uC870\uD68C\uD569\uB2C8\uB2E4. \uC751\uB2F5 data\uB294 items, count, hasNext, nextCursor\uB85C \uAD6C\uC131\uB429\uB2C8\uB2E4."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "\uC870\uD68C \uC131\uACF5",
                    content = @Content(schema = @Schema(implementation = TalismanSwaggerSchemas.TalismanListApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "\uCEE4\uC11C \uB610\uB294 \uD398\uC774\uC9C0 \uD06C\uAE30 \uC624\uB958",
                    content = @Content(schema = @Schema(implementation = TalismanSwaggerSchemas.TalismanErrorApiResponse.class))
            )
    })
    @GetMapping
    public ApiResponse<TalismanListResponse> getMyTalismans(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "\uB2E4\uC74C \uD398\uC774\uC9C0 \uCEE4\uC11C. \uCCAB \uD398\uC774\uC9C0\uB294 \uC0DD\uB7B5", example = "17")
            @RequestParam(required = false) Long cursor,
            @Parameter(description = "\uD398\uC774\uC9C0 \uD06C\uAE30. \uAE30\uBCF8 3", example = "3")
            @RequestParam(defaultValue = "3") int size
    ) {
        return ApiResponse.onSuccess(
                talismanService.getTalismans(userDetails.getMember().getPhoneNumber(), cursor, size)
        );
    }
}