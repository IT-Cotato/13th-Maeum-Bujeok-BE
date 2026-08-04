package com.maumbujeok.backend.domain.auth.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maumbujeok.backend.domain.auth.domain.RefreshToken;
import com.maumbujeok.backend.domain.auth.domain.SmsAuthCode;
import com.maumbujeok.backend.domain.auth.dto.LoginRequest;
import com.maumbujeok.backend.domain.auth.dto.TokenReissueRequest;
import com.maumbujeok.backend.domain.auth.dto.SignUpRequest;
import com.maumbujeok.backend.domain.auth.repository.RefreshTokenRepository;
import com.maumbujeok.backend.domain.auth.repository.SmsAuthCodeRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    @Autowired MemberRepository memberRepository;
    @Autowired SmsAuthCodeRepository smsAuthCodeRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        smsAuthCodeRepository.deleteAllInBatch();
    }

    @Test
    void duplicateSignUpShouldFail() throws Exception {
        // Given: 이미 가입된 회원 존재
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .name("홍길동")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Member.Role.ROLE_USER)
                .build();
        memberRepository.save(member);

        // SMS 인증도 완료 처리
        SmsAuthCode smsAuthCode = SmsAuthCode.builder()
                .phoneNumber("01012345678")
                .code("123456")
                .expiredAt(LocalDateTime.now().plusMinutes(3))
                .build();
        smsAuthCode.verify();
        smsAuthCodeRepository.save(smsAuthCode);

        SignUpRequest request = SignUpRequest.builder()
                .phoneNumber("01012345678")
                .name("홍길동")
                .password("Password123!")
                .birthDate("19990101")
                .termsAgreed(true)
                .privacyAgreed(true)
                .sensitiveDataAgreed(true)
                .marketingAgreed(false)
                .build();

        // When & Then: 중복된 번호로 가입 시 400 Bad Request 및 AUTH_004 리턴 검증
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("AUTH_004"));
    }

    @Test
    void signUpWithoutSmsVerificationShouldBeBlocked() throws Exception {
        // Given: SMS 인증 완료되지 않은 번호
        SmsAuthCode smsAuthCode = SmsAuthCode.builder()
                .phoneNumber("01012345679")
                .code("123456")
                .expiredAt(LocalDateTime.now().plusMinutes(3))
                .build();
        smsAuthCodeRepository.save(smsAuthCode);

        SignUpRequest request = SignUpRequest.builder()
                .phoneNumber("01012345679")
                .name("홍길동")
                .password("Password123!")
                .birthDate("19990101")
                .termsAgreed(true)
                .privacyAgreed(true)
                .sensitiveDataAgreed(true)
                .marketingAgreed(false)
                .build();

        // When & Then: 가입 시 400 Bad Request 및 SMS_003 리턴 검증
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SMS_003"));
    }

    @Test
    void loginWithWrongPasswordOrNonExistingNumberShouldFail() throws Exception {
        // Given: 가입된 회원 존재
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .name("홍길동")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Member.Role.ROLE_USER)
                .build();
        memberRepository.save(member);

        // Scenario 1: 틀린 비밀번호
        LoginRequest wrongPwRequest = new LoginRequest("01012345678", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPwRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_002"));

        // Scenario 2: 존재하지 않는 번호
        LoginRequest nonExistRequest = new LoginRequest("01099999999", "Password123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(nonExistRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AUTH_001"));
    }

    @Test
    void signUpWithMandatoryTermsFalseShouldBeBlockedByValidation() throws Exception {
        SignUpRequest request = SignUpRequest.builder()
                .phoneNumber("01012345678")
                .name("홍길동")
                .password("Password123!")
                .birthDate("19990101")
                .termsAgreed(false) // 필수 동의 미동의
                .privacyAgreed(true)
                .sensitiveDataAgreed(true)
                .marketingAgreed(false)
                .build();

        // When & Then: @Valid에 의해 400 Bad Request 반환 검증
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reissueWithLoggedOutRefreshTokenShouldBeBlocked() throws Exception {
        // Given: 유효한 Refresh Token 및 DB 존재
        String userKey = "01012345678";
        String refreshToken = jwtTokenProvider.createRefreshToken(userKey);
        LocalDateTime expiry = jwtTokenProvider.getRefreshTokenExpiryDate();

        RefreshToken dbToken = RefreshToken.builder()
                .userKey(userKey)
                .token(refreshToken)
                .expiredAt(expiry)
                .build();
        refreshTokenRepository.save(dbToken);

        // When: 로그아웃 수행 (DB에서 RefreshToken 제거)
        refreshTokenRepository.deleteByUserKey(userKey);

        // Then: 로그아웃된 토큰으로 reissue 요청 시 401 Unauthorized 및 AUTH_005 반환 검증
        TokenReissueRequest request = new TokenReissueRequest(refreshToken);

        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_005"));
    }

    @Test
    void smsSendSignupForNewNumberSucceedsWithBypass() throws Exception {
        mockMvc.perform(post("/api/auth/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"01099999999\",\"purpose\":\"SIGNUP\"}"))
                .andExpect(status().isOk());

        SmsAuthCode code = smsAuthCodeRepository.findTopByPhoneNumberOrderByCreatedAtDesc("01099999999")
                .orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("123456", code.getCode());
    }

    @Test
    void smsSendSignupForAlreadyRegisteredNumberFails() throws Exception {
        Member member = Member.builder()
                .phoneNumber("01099999999")
                .name("홍길동")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Member.Role.ROLE_USER)
                .build();
        memberRepository.save(member);

        mockMvc.perform(post("/api/auth/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"01099999999\",\"purpose\":\"SIGNUP\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_009"));
    }

    @Test
    void smsSendPasswordResetForUnregisteredNumberFails() throws Exception {
        mockMvc.perform(post("/api/auth/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"01099999999\",\"purpose\":\"PASSWORD_RESET\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("AUTH_010"));
    }

    @Test
    void smsSendPasswordResetForRegisteredNumberSucceedsWithBypass() throws Exception {
        Member member = Member.builder()
                .phoneNumber("01099999999")
                .name("홍길동")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Member.Role.ROLE_USER)
                .build();
        memberRepository.save(member);

        mockMvc.perform(post("/api/auth/sms/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phoneNumber\":\"01099999999\",\"purpose\":\"PASSWORD_RESET\"}"))
                .andExpect(status().isOk());

        SmsAuthCode code = smsAuthCodeRepository.findTopByPhoneNumberOrderByCreatedAtDesc("01099999999")
                .orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("123456", code.getCode());
    }

    @Test
    void googleLoginStartsOAuthAuthorization() throws Exception {
        mockMvc.perform(get("/api/auth/google"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/oauth2/authorization/google"));
    }}
