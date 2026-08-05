package com.maumbujeok.backend.domain.member.repository;

import com.maumbujeok.backend.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByPhoneNumber(String phoneNumber);
    Optional<Member> findByProviderAndProviderId(Member.Provider provider, String providerId);
    java.util.List<Member> findAllByOnboardingCompletedAtIsNotNull();
}
