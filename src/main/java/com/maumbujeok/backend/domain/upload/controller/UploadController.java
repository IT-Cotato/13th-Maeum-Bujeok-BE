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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/uploads", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "\uC77C\uAE30 \uC774\uBBF8\uC9C0 \uC5C5\uB85C\uB4DC API", description = "\uC77C\uAE30 \uC774\uBBF8\uC9C0\uC758 \uC5C5\uB85C\uB4DC URL \uBC1C\uAE09\uACFC \uC5C5\uB85C\uB4DC \uD30C\uC77C \uC815\uB9AC API")
@SecurityRequirement(name = "JWT_TOKEN")
public class UploadController {
    private final UploadService uploadService;

    @Operation(
            summary = "일기 이미지 업로드 URL 발급",
            description = """
                    이미지는 multipart/form-data로 이 API에 직접 전송하지 않습니다. 아래 2단계를 순서대로 수행하세요.

                    1. **업로드 URL 발급**: `POST {API_BASE_URL}/api/uploads/presigned-url`에 Bearer 토큰과 이미지의 실제 MIME 타입·바이트 크기를 JSON으로 보냅니다.
                    ```bash
                    curl -X POST "{API_BASE_URL}/api/uploads/presigned-url" \
                      -H "Authorization: Bearer {ACCESS_TOKEN}" \
                      -H "Content-Type: application/json" \
                      -d '{"contentType":"image/png","fileSize":1024}'
                    ```
                    2. **파일 바이트 PUT**: 응답 `result.uploadUrl`에 파일 원본을 PUT합니다. 이 URL은 S3 등의 외부 스토리지 URL이거나 로컬 개발 서버 URL일 수 있으므로, API 기본 URL을 덧붙이지 말고 응답값을 그대로 사용합니다. 발급 요청에 사용한 것과 같은 `Content-Type`을 반드시 설정합니다.
                    ```bash
                    curl -X PUT "{uploadUrl}" \
                      -H "Content-Type: image/png" \
                      --data-binary "@image.png"
                    ```
                    3. PUT이 성공한 경우에만 응답 `result.uploadId`를 일기 생성·수정 요청의 `imageUploadIds`에 넣습니다. URL은 발급 후 10분 동안 유효합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "\uC5C5\uB85C\uB4DC URL \uBC1C\uAE09 \uC131\uACF5",
                    content = @Content(schema = @Schema(implementation = UploadSwaggerSchemas.PresignedUrlApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "\uC9C0\uC6D0\uD558\uC9C0 \uC54A\uB294 \uD0C0\uC785\uC774\uAC70\uB098 \uD30C\uC77C \uD06C\uAE30\uAC00 \uD55C\uB3C4\uB97C \uCD08\uACFC",
                    content = @Content(schema = @Schema(implementation = UploadSwaggerSchemas.UploadErrorApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694")
    })
    @PostMapping("/presigned-url")
    public ApiResponse<PresignedUrlResponse> issue(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody PresignedUrlRequest request
    ) {
        return ApiResponse.onSuccess(uploadService.issue(userDetails.getMember().getPhoneNumber(), request));
    }

    @Operation(
            summary = "\uC774\uBBF8\uC9C0 \uC5C5\uB85C\uB4DC \uC2DD\uBCC4\uC790 \uC0AD\uC81C",
            description = "\uBCF8\uC778\uC774 \uBC1C\uAE09\uD55C uploadId\uB97C \uC0AD\uC81C\uD569\uB2C8\uB2E4. \uC774\uBBF8 \uC0AD\uC81C\uB41C \uC5C5\uB85C\uB4DC\uC5D0 \uB300\uD55C \uC694\uCCAD\uC740 \uC911\uBCF5\uC5D0\uB3C4 204\uB97C \uBC18\uD658\uD569\uB2C8\uB2E4."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "\uC0AD\uC81C \uCC98\uB9AC \uC644\uB8CC", content = @Content),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "\uC778\uC99D \uD544\uC694")
    })
    @DeleteMapping("/{uploadId}")
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "\uC5C5\uB85C\uB4DC ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
            @PathVariable UUID uploadId
    ) {
        uploadService.delete(userDetails.getMember().getPhoneNumber(), uploadId);
        return ResponseEntity.noContent().build();
    }
}