package com.maumbujeok.backend.domain.member.controller;

import com.maumbujeok.backend.domain.member.dto.NotificationDaysResponse;
import com.maumbujeok.backend.domain.member.dto.NotificationDaysUpdateRequest;
import com.maumbujeok.backend.domain.member.dto.NotificationSettingsResponse;
import com.maumbujeok.backend.domain.member.dto.NotificationSettingsUpdateRequest;
import com.maumbujeok.backend.domain.member.service.NotificationService;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "마이페이지 알림 API", description = "마이페이지 알림 설정 관련 API")
@RestController
@RequestMapping("/api/mypage/notifications")
@RequiredArgsConstructor
public class MypageNotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "원하는 알림 수정 API",
            description = "로그인된 회원의 감정 기록 알림과 개운 지침 알림 설정을 수정합니다.",
            security = @SecurityRequirement(name = "JWT_TOKEN")
    )
    @PatchMapping
    public ApiResponse<NotificationSettingsResponse> updateNotificationSettings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NotificationSettingsUpdateRequest request
    ) {
        return ApiResponse.onSuccess(
                notificationService.updateNotificationSettings(userDetails.getMember().getPhoneNumber(), request)
        );
    }

    @Operation(
            summary = "요일별 알림 설정 수정 API",
            description = "로그인된 회원의 요일별 알림 수신 여부를 수정합니다.",
            security = @SecurityRequirement(name = "JWT_TOKEN")
    )
    @PatchMapping("/days")
    public ApiResponse<NotificationDaysResponse> updateNotificationDays(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NotificationDaysUpdateRequest request
    ) {
        return ApiResponse.onSuccess(
                notificationService.updateNotificationDays(userDetails.getMember().getPhoneNumber(), request)
        );
    }
}
