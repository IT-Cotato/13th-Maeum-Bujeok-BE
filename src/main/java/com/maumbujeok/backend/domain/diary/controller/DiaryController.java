package com.maumbujeok.backend.domain.diary.controller;

import com.maumbujeok.backend.domain.diary.application.DiaryService;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryRequest;
import com.maumbujeok.backend.domain.diary.dto.CreateDiaryResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;

    @PostMapping
    public ApiResponse<CreateDiaryResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateDiaryRequest request
    ) {
        return ApiResponse.onSuccess(diaryService.create(userDetails.getMember().getPhoneNumber(), request));
    }

    @GetMapping("/{diaryId}/analysis")
    public ApiResponse<DiaryAnalysisResponse> getAnalysis(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long diaryId
    ) {
        return ApiResponse.onSuccess(diaryService.getAnalysis(userDetails.getMember().getPhoneNumber(), diaryId));
    }
}
