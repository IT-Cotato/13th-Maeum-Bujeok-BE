package com.maumbujeok.backend.global.security;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String userKey) throws UsernameNotFoundException {
        Member member = memberRepository.findByPhoneNumber(userKey)
                .orElseGet(() -> {
                    String providerId = userKey.startsWith("GOOGLE_") ? userKey.substring(7) : userKey;
                    return memberRepository.findByProviderAndProviderId(Member.Provider.GOOGLE, providerId)
                            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userKey));
                });
        return new CustomUserDetails(member);
    }
}
