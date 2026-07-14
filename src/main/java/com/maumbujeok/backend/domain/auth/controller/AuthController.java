package com.maumbujeok.backend.domain.auth.controller;

import com.maumbujeok.backend.domain.auth.dto.LoginRequest;
import com.maumbujeok.backend.domain.auth.dto.SignUpRequest;
import com.maumbujeok.backend.domain.auth.dto.TokenResponse;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.common.ApiResponse;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증 API", description = "로그인 및 회원가입 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "회원가입 API", description = "새로운 회원을 등록합니다.")
    @PostMapping("/signup")
    public ApiResponse<String> signup(@RequestBody SignUpRequest request) {
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            return ApiResponse.onFailure("400", "이미 가입된 이메일입니다.", null);
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Member.Role.ROLE_USER)
                .build();

        memberRepository.save(member);
        return ApiResponse.onSuccess("회원가입이 완료되었습니다.");
    }

    @Operation(summary = "로그인 API", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            return ApiResponse.onFailure("400", "비밀번호가 일치하지 않습니다.", null);
        }

        String token = jwtTokenProvider.createToken(member.getEmail(), member.getRole().name());
        return ApiResponse.onSuccess(new TokenResponse(token));
    }
}
