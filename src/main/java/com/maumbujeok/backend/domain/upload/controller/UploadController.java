package com.maumbujeok.backend.domain.upload.controller;

import com.maumbujeok.backend.domain.upload.application.UploadService;
import com.maumbujeok.backend.domain.upload.dto.PresignedUrlRequest;
import com.maumbujeok.backend.domain.upload.dto.PresignedUrlResponse;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
@Tag(name = "일기 이미지 업로드", description = "비공개 일기 이미지 업로드 URL 발급 및 업로드 삭제 API")
@SecurityRequirement(name = "JWT_TOKEN")
public class UploadController {
    private final UploadService uploadService;

    @Operation(
            summary = "일기 이미지 업로드 URL 발급",
            description = "JPEG, PNG, WebP 파일에 대해 10분간 유효한 PUT URL을 발급합니다. 파일은 최대 10MB이며, 실제 PUT의 Content-Type과 바이트 크기는 요청값과 일치해야 합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "발급 성공", content = @Content(schema = @Schema(implementation = UploadSwaggerSchemas.PresignedUrlApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "지원하지 않는 형식 또는 파일 크기 오류", content = @Content(schema = @Schema(implementation = UploadSwaggerSchemas.UploadErrorApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 실패")
    })
    @PostMapping("/presigned-url")
    public ApiResponse<PresignedUrlResponse> issue(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PresignedUrlRequest request
    ) {
        return ApiResponse.onSuccess(uploadService.issue(
                userDetails.getMember().getPhoneNumber(), request));
    }

    @Operation(
            summary = "업로드 이미지 삭제",
            description = "본인이 발급한 업로드를 삭제합니다. 일기에 연결된 이미지도 삭제할 수 있으며 같은 요청을 반복해도 204를 반환합니다. 객체 저장소 삭제 실패 시 재시도 대상으로 남깁니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 요청 처리 완료", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "인증 실패")
    })
    @DeleteMapping("/{uploadId}")
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "업로드 ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true) @PathVariable UUID uploadId
    ) {
        uploadService.delete(userDetails.getMember().getPhoneNumber(), uploadId);
        return ResponseEntity.noContent().build();
    }
}
