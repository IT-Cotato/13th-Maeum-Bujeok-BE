package com.maumbujeok.backend.global.security.oauth2;

import com.maumbujeok.backend.domain.auth.domain.RefreshToken;
import com.maumbujeok.backend.domain.auth.repository.RefreshTokenRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String providerId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String googleName = oAuth2User.getAttribute("name");

        log.info("[OAuth2 Success] ProviderId: {}, Email: {}, Name: {}", providerId, email, googleName);

        // 구글 유저 조회 없으면 즉시 자동 가입 (1-Step), 기존 유저 존재 시 DB의 name을 덮어씌우지 않음!
        Member member = memberRepository.findByProviderAndProviderId(Member.Provider.GOOGLE, providerId)
                .orElseGet(() -> {
                    LocalDateTime now = LocalDateTime.now();

                    Member newMember = Member.builder()
                            .name(googleName)
                            .email(email)
                            .phoneNumber(null)
                            .provider(Member.Provider.GOOGLE)
                            .providerId(providerId)
                            .termsAgreedAt(now)
                            .privacyAgreedAt(now)
                            .sensitiveDataAgreedAt(now)
                            .role(Member.Role.ROLE_USER)
                            .build();

                    return memberRepository.save(newMember);
                });

        String userKey = "GOOGLE_" + providerId;

        // Access Token & Refresh Token 발급
        String accessToken = jwtTokenProvider.createToken(userKey, member.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(userKey);
        LocalDateTime refreshTokenExpiry = jwtTokenProvider.getRefreshTokenExpiryDate();

        // Refresh Token DB 저장 혹은 갱신
        refreshTokenRepository.findByUserKey(userKey)
                .ifPresentOrElse(
                        existing -> existing.updateToken(refreshToken, refreshTokenExpiry),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .userKey(userKey)
                                        .token(refreshToken)
                                        .expiredAt(refreshTokenExpiry)
                                        .build()
                        )
                );

        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
