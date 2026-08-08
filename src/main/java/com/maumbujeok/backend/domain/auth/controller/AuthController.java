package com.maumbujeok.backend.domain.auth.controller;

import com.maumbujeok.backend.domain.auth.domain.RefreshToken;
import com.maumbujeok.backend.domain.auth.domain.SmsAuthCode;
import com.maumbujeok.backend.domain.auth.dto.*;
import com.maumbujeok.backend.domain.auth.repository.RefreshTokenRepository;
import com.maumbujeok.backend.domain.auth.repository.SmsAuthCodeRepository;
import com.maumbujeok.backend.domain.auth.service.SmsService;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Tag(name = "인증 API", description = "SMS 인증, 회원가입, 로그인, 토큰 재발급, 로그아웃, 비밀번호 재설정 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final MemberRepository memberRepository;
        private final SmsAuthCodeRepository smsAuthCodeRepository;
        private final RefreshTokenRepository refreshTokenRepository;
        private final SmsService smsService;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;

        @Operation(summary = "SMS 인증번호 발송 API", description = "입력한 전화번호로 6자리 인증번호를 생성하여 Mock 발송하고 3분간 저장합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "인증번호 발송 성공", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthStringApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "COMMON_400: 잘못된 요청 또는 전화번호 형식 오류", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthErrorApiResponse.class)))
        })
        @PostMapping("/sms/send")
        public ApiResponse<String> sendSmsCode(@jakarta.validation.Valid @RequestBody SmsSendRequest request) {
                smsService.sendVerificationCode(request.getPhoneNumber(), request.getPurpose());
                return ApiResponse.onSuccess("인증번호가 발송되었습니다.");
        }

        @Operation(summary = "SMS 인증번호 검증 API", description = "발송된 인증번호의 유효성을 검증합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "전화번호 인증 성공", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthStringApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUTH_003: 인증번호 불일치 또는 만료", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthErrorApiResponse.class)))
        })
        @PostMapping("/sms/verify")
        public ApiResponse<String> verifySmsCode(@RequestBody SmsVerifyRequest request) {
                smsService.verifyCode(request.getPhoneNumber(), request.getCode());
                return ApiResponse.onSuccess("전화번호 인증이 완료되었습니다.");
        }

        @Operation(summary = "회원가입 API", description = "전화번호 본인 인증을 완료한 사용자의 이름과 비밀번호로 계정을 생성합니다. 생년월일, 사주 정보 및 약관 동의는 로그인 후 온보딩 API에서 입력합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthStringApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUTH_002: 전화번호 중복 또는 인증되지 않은 번호", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthErrorApiResponse.class)))
        })
        @Transactional
        @PostMapping("/signup")
        public ApiResponse<String> signup(@jakarta.validation.Valid @RequestBody SignUpRequest request) {
                // 1. 전화번호 중복 체크
                if (memberRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
                        throw new CustomException(ErrorCode.DUPLICATE_PHONE_NUMBER);
                }

                // 2. 전화번호 본인 인증 여부 체크
                SmsAuthCode smsAuthCode = getVerifiedSmsAuthCode(request.getPhoneNumber());

                // 3. 회원 저장
                Member member = Member.builder()
                                .name(request.getName())
                                .phoneNumber(request.getPhoneNumber())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .provider(Member.Provider.LOCAL)
                                .role(Member.Role.ROLE_USER)
                                .build();

                memberRepository.save(member);
                smsAuthCodeRepository.delete(smsAuthCode);
                return ApiResponse.onSuccess("회원가입이 완료되었습니다.");
        }

        @Operation(summary = "로그인 API", description = "전화번호와 비밀번호로 로그인하여 Access Token 및 Refresh Token을 발급받습니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthTokenApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUTH_001: 유저를 찾을 수 없거나 비밀번호 불일치 또는 소셜 전용 계정", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthErrorApiResponse.class)))
        })
        @Transactional
        @PostMapping("/login")
        public ApiResponse<TokenResponse> login(@jakarta.validation.Valid @RequestBody LoginRequest request) {
                Member member = memberRepository.findByPhoneNumber(request.getPhoneNumber())
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                // 소셜 전용 계정일 경우 일반 로그인 방어
                if (member.getProvider() != Member.Provider.LOCAL) {
                        throw new CustomException(ErrorCode.SOCIAL_USER_MUST_LOGIN_WITH_OAUTH);
                }

                if (member.getPasswordHash() == null
                                || !passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
                        throw new CustomException(ErrorCode.INVALID_PASSWORD);
                }

                String userKey = member.getPhoneNumber();
                String accessToken = jwtTokenProvider.createToken(userKey, member.getRole().name());
                String refreshToken = jwtTokenProvider.createRefreshToken(userKey);
                LocalDateTime refreshTokenExpiry = jwtTokenProvider.getRefreshTokenExpiryDate();

                // Refresh Token DB 저장 혹은 갱신
                refreshTokenRepository.findByUserKey(userKey)
                                .ifPresentOrElse(
                                                existingToken -> existingToken.updateToken(refreshToken,
                                                                refreshTokenExpiry),
                                                () -> refreshTokenRepository.save(
                                                                RefreshToken.builder()
                                                                                .userKey(userKey)
                                                                                .token(refreshToken)
                                                                                .expiredAt(refreshTokenExpiry)
                                                                                .build()));

                return ApiResponse.onSuccess(new TokenResponse(accessToken, refreshToken));
        }

        @Operation(summary = "토큰 재발급 API", description = "유효한 Refresh Token을 제출하여 새로운 Access Token 및 Refresh Token을 발급(RTR)받습니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "토큰 재발급 성공", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthTokenApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUTH_004: Refresh Token이 유효하지 않거나 만료됨", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthErrorApiResponse.class)))
        })
        @Transactional
        @PostMapping("/reissue")
        public ApiResponse<TokenResponse> reissue(@RequestBody TokenReissueRequest request) {
                String refreshTokenStr = request.getRefreshToken();

                // 1. 토큰 유효성 검증
                if (!jwtTokenProvider.validateToken(refreshTokenStr)) {
                        throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
                }

                // 2. DB 내 Refresh Token 조회
                RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshTokenStr)
                                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REFRESH_TOKEN));

                if (refreshTokenEntity.isExpired()) {
                        refreshTokenRepository.delete(refreshTokenEntity);
                        throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
                }

                // 3. 사용자 조회
                String userKey = refreshTokenEntity.getUserKey();
                Member member = memberRepository.findByPhoneNumber(userKey)
                                .orElseGet(() -> {
                                        String providerId = userKey.startsWith("GOOGLE_") ? userKey.substring(7)
                                                        : userKey;
                                        return memberRepository
                                                        .findByProviderAndProviderId(Member.Provider.GOOGLE, providerId)
                                                        .orElseThrow(() -> new CustomException(
                                                                        ErrorCode.USER_NOT_FOUND));
                                });

                // 4. 토큰 재발급 (RTR)
                String newAccessToken = jwtTokenProvider.createToken(userKey, member.getRole().name());
                String newRefreshToken = jwtTokenProvider.createRefreshToken(userKey);
                LocalDateTime newRefreshTokenExpiry = jwtTokenProvider.getRefreshTokenExpiryDate();

                refreshTokenEntity.updateToken(newRefreshToken, newRefreshTokenExpiry);

                return ApiResponse.onSuccess(new TokenResponse(newAccessToken, newRefreshToken));
        }

        @Operation(summary = "로그아웃 API", description = "제출된 Refresh Token을 DB에서 삭제하여 무효화 처리합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "로그아웃 성공", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthStringApiResponse.class)))
        })
        @Transactional
        @PostMapping("/logout")
        public ApiResponse<String> logout(@RequestBody LogoutRequest request) {
                if (request.getRefreshToken() != null) {
                        refreshTokenRepository.findByToken(request.getRefreshToken())
                                        .ifPresent(refreshTokenRepository::delete);
                }
                return ApiResponse.onSuccess("로그아웃 되었습니다.");
        }

        @Operation(summary = "비밀번호 재설정 API", description = "SMS 본인 인증 완료 후 전화번호와 새 비밀번호를 받아 비밀번호를 변경합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "비밀번호 재설정 성공", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthStringApiResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "AUTH_003: 본인 인증 미완료 또는 소셜 로그인 사용자", content = @Content(schema = @Schema(implementation = AuthSwaggerSchemas.AuthErrorApiResponse.class)))
        })
        @Transactional
        @PostMapping("/password/reset")
        public ApiResponse<String> resetPassword(@RequestBody PasswordResetRequest request) {
                // 1. 전화번호 본인 인증 여부 체크
                SmsAuthCode smsAuthCode = getVerifiedSmsAuthCode(request.getPhoneNumber());

                // 2. 유저 조회
                Member member = memberRepository.findByPhoneNumber(request.getPhoneNumber())
                                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

                // 2-1. 소셜 로그인 가입자 재설정 방어
                if (member.getProvider() != Member.Provider.LOCAL) {
                        throw new CustomException(ErrorCode.SOCIAL_USER_CANNOT_RESET_PASSWORD);
                }

                // 3. 비밀번호 업데이트 (Dirty Checking)
                member.updatePasswordHash(passwordEncoder.encode(request.getNewPassword()));
                smsAuthCodeRepository.delete(smsAuthCode);

                return ApiResponse.onSuccess("비밀번호가 성공적으로 재설정되었습니다.");
        }

        private SmsAuthCode getVerifiedSmsAuthCode(String phoneNumber) {
                SmsAuthCode smsAuthCode = smsAuthCodeRepository
                                .findTopByPhoneNumberOrderByCreatedAtDesc(phoneNumber)
                                .orElseThrow(() -> new CustomException(ErrorCode.SMS_CODE_NOT_VERIFIED));

                if (!Boolean.TRUE.equals(smsAuthCode.getIsVerified()) || smsAuthCode.isExpired()) {
                        throw new CustomException(ErrorCode.SMS_CODE_NOT_VERIFIED);
                }

                return smsAuthCode;
        }

        @Operation(summary = "Google 로그인 및 회원가입 시작", description = """
                        Google OAuth 인증을 시작하는 브라우저 이동용 엔드포인트입니다.

                        1. 프런트엔드에서 `window.location.assign('{API_BASE_URL}/api/auth/google')` 또는 브라우저 주소 이동으로 호출합니다.
                        2. 이 요청은 Google 동의 화면으로 302 리다이렉트됩니다. Swagger의 **Execute** 또는 `fetch`로 호출하지 마세요.
                        3. 인증에 성공하면 프런트엔드의 `/oauth/callback`으로 리다이렉트되며, `accessToken`과 `refreshToken`이 query parameter로 전달됩니다.
                        4. 최초 Google 로그인은 계정만 생성합니다. 토큰을 받은 뒤 `POST /api/members/onboarding`으로 생년월일·사주·약관 동의를 저장해야 합니다.
                        """)
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "302", description = "Google OAuth 인증 화면으로 리다이렉트"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Google OAuth client 설정 누락 또는 서버 설정 오류", content = @Content)
        })
        @GetMapping("/google")
        public void startGoogleLogin(HttpServletResponse response) throws java.io.IOException {
                response.sendRedirect("/oauth2/authorization/google");
        }
}
