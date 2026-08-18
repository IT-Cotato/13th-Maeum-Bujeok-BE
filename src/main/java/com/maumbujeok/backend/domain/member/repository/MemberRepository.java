package com.maumbujeok.backend.domain.member.repository;

import com.maumbujeok.backend.domain.member.domain.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, String> {
    Optional<Member> findByPhoneNumber(String phoneNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.phoneNumber = :phoneNumber")
    Optional<Member> findByPhoneNumberForUpdate(@Param("phoneNumber") String phoneNumber);

    Optional<Member> findByProviderAndProviderId(Member.Provider provider, String providerId);
    Page<Member> findAllByOnboardingCompletedAtIsNotNull(Pageable pageable);
}
