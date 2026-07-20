package com.maumbujeok.backend.domain.member.repository;

import com.maumbujeok.backend.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);
    Optional<Member> findByPhoneNumber(String phoneNumber);
}
