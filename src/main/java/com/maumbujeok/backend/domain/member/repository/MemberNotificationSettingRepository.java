package com.maumbujeok.backend.domain.member.repository;

import com.maumbujeok.backend.domain.member.domain.MemberNotificationSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberNotificationSettingRepository extends JpaRepository<MemberNotificationSetting, Long> {
    Optional<MemberNotificationSetting> findByMemberPhoneNumber(String phoneNumber);
}
