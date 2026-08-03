package com.maumbujeok.backend.domain.burn.controller;
import com.maumbujeok.backend.domain.burn.application.BurningService;
import com.maumbujeok.backend.domain.burn.dto.*;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/burnings") @RequiredArgsConstructor
public class BurningController {
 private final BurningService service;
 @PostMapping public ApiResponse<CreateBurningResponse> create(@AuthenticationPrincipal CustomUserDetails user,@RequestBody CreateBurningRequest request){ return ApiResponse.onSuccess(service.create(user.getMember().getPhoneNumber(),request)); }
 @GetMapping public ApiResponse<BurningListResponse> list(@AuthenticationPrincipal CustomUserDetails user,@RequestParam(required=false) Long cursor,@RequestParam(required=false) Integer size){ return ApiResponse.onSuccess(service.getPage(user.getMember().getPhoneNumber(),cursor,size)); }
 @GetMapping("/{burningId}") public ApiResponse<BurningDetailResponse> get(@AuthenticationPrincipal CustomUserDetails user,@PathVariable Long burningId){ return ApiResponse.onSuccess(service.get(user.getMember().getPhoneNumber(),burningId)); }
 @GetMapping("/{burningId}/analysis") public ApiResponse<BurningAnalysisResponse> analysis(@AuthenticationPrincipal CustomUserDetails user,@PathVariable Long burningId){ return ApiResponse.onSuccess(service.getAnalysis(user.getMember().getPhoneNumber(),burningId)); }
}
