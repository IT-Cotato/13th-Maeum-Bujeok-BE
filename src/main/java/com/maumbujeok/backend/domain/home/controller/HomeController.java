package com.maumbujeok.backend.domain.home.controller;

import com.maumbujeok.backend.domain.home.application.HomeSummaryService;
import com.maumbujeok.backend.domain.home.dto.HomeSummaryResponse;
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
import org.springframework.web.bind.annotation.RestController;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.home.dto.HomeResponse;
import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import com.maumbujeok.backend.domain.talisman.dto.TalismanItemResponse;
import com.maumbujeok.backend.domain.talisman.repository.TalismanRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Tag(name = "홈 화면 API", description = "홈 화면 사주 기반 요약(오행 기운, 오늘의 행운, 오행 감정 분석) 관련 API")
@SecurityRequirement(name = "JWT_TOKEN")
public class HomeController {

    private final HomeSummaryService homeSummaryService;
    private final DiaryRepository diaryRepository;
    private final TalismanRepository talismanRepository;
    private final java.time.Clock serviceClock;

    @Operation(
            summary = "통합 홈 화면 데이터 조회",
            description = "홈 화면 렌더링에 필요한 모든 데이터(사주 요약, 일기 작성 여부, 최근 부적, 사용자 이름, 오늘의 프롬프트 가이드)를 한 번에 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "COMMON_401: 인증 정보가 올바르지 않습니다."),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "MEMBER_003: 사주 프로필 정보를 찾을 수 없습니다. (온보딩 미완료)")
    })
    @GetMapping
    public ApiResponse<HomeResponse> getHomeIntegratedData(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Member member = userDetails.getMember();
        String phoneNumber = member.getPhoneNumber();
        LocalDate today = LocalDate.now(serviceClock);

        // 1. HomeSummaryService 호출 (온보딩 안 된 경우 예외 발생)
        HomeSummaryResponse summary = homeSummaryService.getTodaySummary(phoneNumber);

        // 2. 오늘 활성 일기 작성 여부 (소각된 일기는 제외)
        boolean isDiaryWritten = diaryRepository.existsByMemberPhoneNumberAndRecordedDateAndBurnedAtIsNull(
                phoneNumber, today);

        // 3. 가장 최근 부적
        List<Talisman> talismans = talismanRepository.findAllByMemberPhoneNumberOrderByCreatedAtDesc(phoneNumber);
        TalismanItemResponse activeTalisman = talismans.isEmpty() ? null : TalismanItemResponse.from(talismans.get(0));

        // 4. 오늘의 프롬프트 가이드
        String promptGuide = "오늘은 부정적인 감정을 종이에 적어 살풀이해보는 건 어떨까요?";

        return ApiResponse.onSuccess(new HomeResponse(
                member.getName(),
                isDiaryWritten,
                summary,
                activeTalisman,
                promptGuide
        ));
    }

    @Operation(
            summary = "오늘의 홈 화면 요약 조회",
            description = "로그인한 사용자의 사주 데이터(연/월/일/시)를 기반으로 생성된 오늘의 오행 기운, 행운 팁(1문장), 오행 감정 분석(2문장) 요약 정보를 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = HomeSwaggerSchemas.HomeSummaryApiResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "COMMON_401: 인증 정보가 올바르지 않거나 인증 토큰이 존재하지 않습니다."
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "COMMON_403: 접근 권한이 없거나 유효하지 않은 토큰 요청"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "AUTH_001: 가입되지 않은 회원의 요청입니다. / MEMBER_003: 사주 프로필 정보를 찾을 수 없습니다. (온보딩 미완료)",
                    content = @Content(schema = @Schema(implementation = HomeSwaggerSchemas.HomeErrorApiResponse.class))
            )
    })
    @GetMapping("/summary")
    public ApiResponse<HomeSummaryResponse> getTodayHomeSummary(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(
                homeSummaryService.getTodaySummary(userDetails.getMember().getPhoneNumber())
        );
    }
}
