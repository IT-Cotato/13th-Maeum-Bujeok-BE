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
import java.util.Optional;

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

        log.info("[OAuth2 Success] ProviderId: {}, Email: {}", providerId, email);

        Optional<Member> memberOptional = memberRepository.findByProviderAndProviderId(Member.Provider.GOOGLE, providerId);

        if (memberOptional.isPresent()) {
            // [기존 가입자] -> Access Token 및 Refresh Token 발급 후 프론트 콜백 URL로 리다이렉트
            Member member = memberOptional.get();

            String accessToken = jwtTokenProvider.createToken(member.getLoginId(), member.getRole().name());
            String refreshToken = jwtTokenProvider.createRefreshToken(member.getLoginId());
            LocalDateTime refreshTokenExpiry = jwtTokenProvider.getRefreshTokenExpiryDate();

            refreshTokenRepository.findByLoginId(member.getLoginId())
                    .ifPresentOrElse(
                            existing -> existing.updateToken(refreshToken, refreshTokenExpiry),
                            () -> refreshTokenRepository.save(
                                    RefreshToken.builder()
                                            .loginId(member.getLoginId())
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
        } else {
            // [신규 가입자] -> 2-Step 가입용 Register Token 발급 후 추가 정보 입력 페이지로 리다이렉트
            String registerToken = jwtTokenProvider.createRegisterToken("GOOGLE", providerId, email);

            String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth/signup")
                    .queryParam("registerToken", registerToken)
                    .build().toUriString();

            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        }
    }
}
