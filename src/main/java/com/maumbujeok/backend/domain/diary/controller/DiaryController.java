package com.maumbujeok.backend.domain.diary.controller;

import com.maumbujeok.backend.domain.diary.application.DiaryService;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryCalendarResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryCursorPageResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryDetailResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.EmotionStatResponse;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.UpdateDiaryResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/diaries", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "\uC77C\uAE30 API", description = "\uC77C\uAE30 \uC0DD\uC131, \uC870\uD68C, \uC218\uC815, \uC0AD\uC81C \uBC0F AI \uBD84\uC11D API")
@SecurityRequirement(name = "JWT_TOKEN")
public class DiaryController {
    private final DiaryService diaryService;

    @Operation(summary = "\uC77C\uAE30 \uC0DD\uC131", description = "\uC77C\uAE30\uC640 \uC120\uD0DD \uAC10\uC815, \uAE30\uB85D\uC77C, \uC5C5\uB85C\uB4DC \uC774\uBBF8\uC9C0\uB97C \uC800\uC7A5\uD569\uB2C8\uB2E4. \uC751\uB2F5 data\uC5D0\uB294 diaryId, recordedDate, analysisStatus\uAC00 \uD3EC\uD568\uB418\uBA70 AI \uBD84\uC11D\uC740 \uBE44\uB3D9\uAE30\uB85C \uC2DC\uC791\uB429\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uC0DD\uC131 \uC131\uACF5", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.CreateDiaryApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "\uC694\uCCAD \uAC80\uC99D \uC2E4\uD328", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "\uC5F0\uACB0\uD560 \uC5C5\uB85C\uB4DC\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC74C", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class)))
    })
    @PostMapping
    public ApiResponse<CreateDiaryResponse> create(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CreateDiaryRequest request) {
        return ApiResponse.onSuccess(diaryService.create(phone(userDetails), request));
    }

    @Operation(summary = "\uC77C\uAE30 \uCEE4\uC11C \uBAA9\uB85D \uC870\uD68C", description = "recordedDate\uC640 diaryId\uB97C \uAE30\uC900\uC73C\uB85C \uCD5C\uC2E0 \uC21C\uC11C\uC758 \uC77C\uAE30\uB97C \uC870\uD68C\uD569\uB2C8\uB2E4. \uC751\uB2F5 data\uB294 items, nextCursor, hasNext\uB85C \uAD6C\uC131\uB429\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uC870\uD68C \uC131\uACF5", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryCursorPageApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "\uCEE4\uC11C \uB610\uB294 \uD398\uC774\uC9C0 \uD06C\uAE30 \uC624\uB958", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694")
    })
    @GetMapping
    public ApiResponse<DiaryCursorPageResponse> getPage(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @Parameter(description = "\uB2E4\uC74C \uD398\uC774\uC9C0 \uCEE4\uC11C. \uCCAB \uD398\uC774\uC9C0\uB294 \uC0DD\uB7B5", example = "MjAyNi0wNy0yN3w0Mg") @RequestParam(required = false) String cursor, @Parameter(description = "\uD398\uC774\uC9C0 \uD06C\uAE30. \uAE30\uBCF8 20, \uCD5C\uB300 50", example = "20") @RequestParam(required = false) Integer size) {
        return ApiResponse.onSuccess(diaryService.getPage(phone(userDetails), cursor, size));
    }

    @Operation(summary = "\uAE30\uB85D\uC77C\uBCC4 \uC77C\uAE30 \uBAA9\uB85D \uC870\uD68C", description = "\uC9C0\uC815\uD55C \uAE30\uB85D\uC77C\uC758 \uC77C\uAE30 \uBAA9\uB85D\uC744 \uBC18\uD658\uD569\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uC870\uD68C \uC131\uACF5", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryDateListApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694")
    })
    @GetMapping("/by-date")
    public ApiResponse<List<DiaryResponse>> getByDate(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @Parameter(description = "\uC870\uD68C\uD560 \uAE30\uB85D\uC77C", example = "2026-07-27", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.onSuccess(diaryService.getByDate(phone(userDetails), date));
    }

    @Operation(summary = "\uC6D4\uBCC4 \uC77C\uAE30 \uCE98\uB9B0\uB354 \uC870\uD68C", description = "\uC77C\uAE30\uAC00 \uC874\uC7AC\uD558\uB294 \uB0A0\uC9DC\uC640 \uBCF4\uAD00 \uC0C1\uD0DC(STORED/BURNED)\uB97C \uBC18\uD658\uD569\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uC870\uD68C \uC131\uACF5", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryCalendarApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "\uC5F0\uB3C4 \uB610\uB294 \uC6D4 \uBC94\uC704 \uC624\uB958", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694")
    })
    @GetMapping("/calendar")
    public ApiResponse<DiaryCalendarResponse> getCalendar(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @Parameter(description = "\uC870\uD68C \uC5F0\uB3C4", example = "2026", required = true) @RequestParam Integer year, @Parameter(description = "\uC870\uD68C \uC6D4(1~12)", example = "7", required = true) @RequestParam Integer month) {
        return ApiResponse.onSuccess(diaryService.getCalendar(phone(userDetails), year, month));
    }

    @Operation(summary = "\uC77C\uAE30 \uC0C1\uC138 \uC870\uD68C", description = "\uC77C\uAE30 \uBCF8\uBB38, \uC120\uD0DD \uAC10\uC815, \uCCA8\uBD80 \uC774\uBBF8\uC9C0 URL, AI \uBD84\uC11D\uC744 \uBC18\uD658\uD569\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uC870\uD68C \uC131\uACF5", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryDetailApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "\uBCF8\uC778 \uC18C\uC720 \uC77C\uAE30\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC74C", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class)))
    })
    @GetMapping("/{diaryId}")
    public ApiResponse<DiaryDetailResponse> get(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @Parameter(description = "\uC77C\uAE30 ID", example = "42", required = true) @PathVariable Long diaryId) {
        return ApiResponse.onSuccess(diaryService.get(phone(userDetails), diaryId));
    }

    @Operation(summary = "\uC77C\uAE30 \uC218\uC815", description = "\uC77C\uAE30 \uBCF8\uBB38, \uC120\uD0DD \uAC10\uC815, \uCCA8\uBD80 \uC774\uBBF8\uC9C0\uB97C \uC218\uC815\uD569\uB2C8\uB2E4. \uBCF8\uBB38 \uB610\uB294 \uAC10\uC815\uC774 \uBCC0\uACBD\uB418\uBA74 AI \uBD84\uC11D\uC774 \uB2E4\uC2DC \uC2DC\uC791\uB429\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uC218\uC815 \uC131\uACF5", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.UpdateDiaryApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "\uC694\uCCAD \uAC80\uC99D \uC2E4\uD328", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "\uC77C\uAE30\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC74C", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class)))
    })
    @PatchMapping("/{diaryId}")
    public ApiResponse<UpdateDiaryResponse> update(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @Parameter(description = "\uC77C\uAE30 ID", example = "42", required = true) @PathVariable Long diaryId, @RequestBody UpdateDiaryRequest request) {
        return ApiResponse.onSuccess(diaryService.update(phone(userDetails), diaryId, request));
    }

    @Operation(summary = "\uC77C\uAE30 \uC0AD\uC81C", description = "\uC77C\uAE30\uC640 \uAD00\uB828 AI \uBD84\uC11D, \uC774\uBBF8\uC9C0 \uC5F0\uACB0\uC744 \uC0AD\uC81C\uD569\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "\uC0AD\uC81C \uC131\uACF5", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "\uC77C\uAE30\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC74C", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class)))
    })
    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> delete(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @Parameter(description = "\uC77C\uAE30 ID", example = "42", required = true) @PathVariable Long diaryId) {
        diaryService.delete(phone(userDetails), diaryId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "\uAE30\uAC04\uBCC4 \uAC10\uC815 \uD1B5\uACC4 \uC870\uD68C", description = "\uC9C0\uC815\uD55C \uAE30\uAC04\uC5D0 \uBD84\uC11D\uC774 \uC644\uB8CC\uB41C \uC77C\uAE30\uC758 \uAC10\uC815\uBCC4 \uAC1C\uC218\uB97C \uBC18\uD658\uD569\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uC870\uD68C \uC131\uACF5", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryEmotionStatsApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "\uC870\uD68C \uAE30\uAC04 \uC624\uB958", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694")
    })
    @GetMapping("/emotion-stats")
    public ApiResponse<List<EmotionStatResponse>> getEmotionStats(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @Parameter(description = "\uC870\uD68C \uC2DC\uC791\uC77C", example = "2026-07-21") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @Parameter(description = "\uC870\uD68C \uC885\uB8CC\uC77C", example = "2026-07-27") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.onSuccess(diaryService.getEmotionStats(phone(userDetails), from, to));
    }

    @Operation(summary = "\uC77C\uAE30 AI \uBD84\uC11D \uACB0\uACFC \uC870\uD68C", description = "\uBE44\uB3D9\uAE30 AI \uBD84\uC11D\uC758 \uC0C1\uD0DC\uC640 \uACB0\uACFC\uB97C \uBC18\uD658\uD569\uB2C8\uB2E4. \uBD84\uC11D \uC9C4\uD589 \uC911\uC5D0\uB294 \uACB0\uACFC \uD544\uB4DC\uAC00 null\uC77C \uC218 \uC788\uC2B5\uB2C8\uB2E4.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "\uC870\uD68C \uC131\uACF5", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryAnalysisApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "\uC77C\uAE30\uB97C \uCC3E\uC744 \uC218 \uC5C6\uC74C", content = @Content(schema = @Schema(implementation = DiarySwaggerSchemas.DiaryErrorApiResponse.class)))
    })
    @GetMapping("/{diaryId}/analysis")
    public ApiResponse<DiaryAnalysisResponse> getAnalysis(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails, @Parameter(description = "\uC77C\uAE30 ID", example = "42", required = true) @PathVariable Long diaryId) {
        return ApiResponse.onSuccess(diaryService.getAnalysis(phone(userDetails), diaryId));
    }

    private String phone(CustomUserDetails userDetails) {
        return userDetails.getMember().getPhoneNumber();
    }
}