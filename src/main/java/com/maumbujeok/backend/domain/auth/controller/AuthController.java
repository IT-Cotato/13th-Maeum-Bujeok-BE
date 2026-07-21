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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("/sms/send")
    public ApiResponse<String> sendSmsCode(@RequestBody SmsSendRequest request) {
        smsService.sendVerificationCode(request.getPhoneNumber());
        return ApiResponse.onSuccess("인증번호가 발송되었습니다.");
    }

    @Operation(summary = "SMS 인증번호 검증 API", description = "발송된 인증번호의 유효성을 검증합니다.")
    @PostMapping("/sms/verify")
    public ApiResponse<String> verifySmsCode(@RequestBody SmsVerifyRequest request) {
        smsService.verifyCode(request.getPhoneNumber(), request.getCode());
        return ApiResponse.onSuccess("전화번호 인증이 완료되었습니다.");
    }

    @Operation(summary = "회원가입 API", description = "전화번호 본인 인증 완료 후 회원 정보(약관 동의 내역, 아이디, 비밀번호, 생년월일 등)를 받아 가입 처리합니다.")
    @PostMapping("/signup")
    public ApiResponse<String> signup(@RequestBody SignUpRequest request) {
        // 1. 아이디 중복 체크
        if (memberRepository.findByLoginId(request.getLoginId()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        // 2. 전화번호 중복 체크
        if (memberRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_PHONE_NUMBER);
        }

        // 3. 전화번호 본인 인증 여부 체크
        SmsAuthCode smsAuthCode = smsAuthCodeRepository.findTopByPhoneNumberOrderByCreatedAtDesc(request.getPhoneNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.SMS_CODE_NOT_VERIFIED));

        if (!Boolean.TRUE.equals(smsAuthCode.getIsVerified())) {
            throw new CustomException(ErrorCode.SMS_CODE_NOT_VERIFIED);
        }

        // 4. 회원 저장
        LocalDateTime now = LocalDateTime.now();
        Member member = Member.builder()
                .loginId(request.getLoginId())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .birthDate(request.getBirthDate())
                .termsAgreedAt(Boolean.TRUE.equals(request.getTermsAgreed()) ? now : null)
                .privacyAgreedAt(Boolean.TRUE.equals(request.getPrivacyAgreed()) ? now : null)
                .sensitiveDataAgreedAt(Boolean.TRUE.equals(request.getSensitiveDataAgreed()) ? now : null)
                .marketingAgreedAt(Boolean.TRUE.equals(request.getMarketingAgreed()) ? now : null)
                .role(Member.Role.ROLE_USER)
                .build();

        memberRepository.save(member);
        return ApiResponse.onSuccess("회원가입이 완료되었습니다.");
    }

    @Operation(summary = "로그인 API", description = "아이디와 비밀번호로 로그인하여 Access Token 및 Refresh Token을 발급받습니다.")
    @Transactional
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        Member member = memberRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), member.getPasswordHash())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.createToken(member.getLoginId(), member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getLoginId());
        LocalDateTime refreshTokenExpiry = jwtTokenProvider.getRefreshTokenExpiryDate();

        // Refresh Token DB 저장 혹은 갱신
        refreshTokenRepository.findByLoginId(member.getLoginId())
                .ifPresentOrElse(
                        existingToken -> existingToken.updateToken(refreshToken, refreshTokenExpiry),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .loginId(member.getLoginId())
                                        .token(refreshToken)
                                        .expiredAt(refreshTokenExpiry)
                                        .build()
                        )
                );

        return ApiResponse.onSuccess(new TokenResponse(accessToken, refreshToken));
    }

    @Operation(summary = "토큰 재발급 API", description = "유효한 Refresh Token을 제출하여 새로운 Access Token 및 Refresh Token을 발급(RTR)받습니다.")
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
        Member member = memberRepository.findByLoginId(refreshTokenEntity.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 4. 토큰 재발급 (RTR)
        String newAccessToken = jwtTokenProvider.createToken(member.getLoginId(), member.getRole().name());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(member.getLoginId());
        LocalDateTime newRefreshTokenExpiry = jwtTokenProvider.getRefreshTokenExpiryDate();

        refreshTokenEntity.updateToken(newRefreshToken, newRefreshTokenExpiry);

        return ApiResponse.onSuccess(new TokenResponse(newAccessToken, newRefreshToken));
    }

    @Operation(summary = "로그아웃 API", description = "제출된 Refresh Token을 DB에서 삭제하여 무효화 처리합니다.")
    @Transactional
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestBody LogoutRequest request) {
        if (request.getRefreshToken() != null) {
            refreshTokenRepository.findByToken(request.getRefreshToken())
                    .ifPresent(refreshTokenRepository::delete);
        }
        return ApiResponse.onSuccess("로그아웃 되었습니다.");
    }

    @Operation(summary = "비밀번호 재설정 API", description = "SMS 본인 인증 완료 후 아이디, 전화번호, 새 비밀번호를 받아 비밀번호를 변경합니다.")
    @Transactional
    @PostMapping("/password/reset")
    public ApiResponse<String> resetPassword(@RequestBody PasswordResetRequest request) {
        // 1. 전화번호 본인 인증 여부 체크
        SmsAuthCode smsAuthCode = smsAuthCodeRepository.findTopByPhoneNumberOrderByCreatedAtDesc(request.getPhoneNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.SMS_CODE_NOT_VERIFIED));

        if (!Boolean.TRUE.equals(smsAuthCode.getIsVerified())) {
            throw new CustomException(ErrorCode.SMS_CODE_NOT_VERIFIED);
        }

        // 2. 유저 조회
        Member member = memberRepository.findByLoginId(request.getLoginId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 3. 전화번호 일치 여부 체크
        if (!member.getPhoneNumber().equals(request.getPhoneNumber())) {
            throw new CustomException(ErrorCode.PHONE_NUMBER_MISMATCH);
        }

        // 4. 비밀번호 업데이트 (Dirty Checking)
        member.updatePasswordHash(passwordEncoder.encode(request.getNewPassword()));

        return ApiResponse.onSuccess("비밀번호가 성공적으로 재설정되었습니다.");
    }
}
