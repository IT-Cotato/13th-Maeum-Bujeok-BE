package com.maumbujeok.backend.domain.member.repository;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberSajuProfileRepository extends JpaRepository<MemberSajuProfile, Long> {
    Optional<MemberSajuProfile> findByMember(Member member);
}
