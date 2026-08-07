package com.maumbujeok.backend.domain.member.service;

import com.maumbujeok.backend.domain.auth.domain.SmsAuthCode;
import com.maumbujeok.backend.domain.auth.repository.SmsAuthCodeRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.dto.MemberProfileResponse;
import com.maumbujeok.backend.domain.member.dto.MemberProfileUpdateRequest;
import com.maumbujeok.backend.domain.member.dto.MemberProfileUpdateResponse;
import com.maumbujeok.backend.domain.member.repository.MemberIdentityMigrationRepository;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.saju.application.SajuAnalysisService;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MemberProfileService {

    private final MemberRepository memberRepository;
    private final MemberSajuProfileRepository sajuProfileRepository;
    private final SmsAuthCodeRepository smsAuthCodeRepository;
    private final MemberIdentityMigrationRepository memberIdentityMigrationRepository;
    private final SajuAnalysisService sajuAnalysisService;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public MemberProfileResponse getMyProfile(String authenticatedPhoneNumber) {
        Member member = getMember(authenticatedPhoneNumber);
        return MemberProfileResponse.from(member, getRequiredSajuProfile(member));
    }

    @Transactional
    public MemberProfileUpdateResponse updateMyProfile(String authenticatedPhoneNumber, MemberProfileUpdateRequest request) {
        Member member = getMember(authenticatedPhoneNumber);
        MemberSajuProfile sajuProfile = getRequiredSajuProfile(member);

        String newPhoneNumber = request.normalizedPhoneNumber();
        boolean phoneNumberChanged = !member.getPhoneNumber().equals(newPhoneNumber);

        validatePhoneNumberChange(member.getPhoneNumber(), newPhoneNumber, phoneNumberChanged);

        if (phoneNumberChanged) {
            migrateMemberIdentity(member, request, sajuProfile, newPhoneNumber);
            boolean reauthenticationRequired = member.getProvider() == Member.Provider.LOCAL;
            return buildResponse(newPhoneNumber, reauthenticationRequired);
        }

        member.updateProfile(request.trimmedName(), request.getBirthDate());
        sajuProfile.update(request.getGender(), sajuProfile.getCalendarType(), request.getBirthTime());
        sajuAnalysisService.refreshLatestAnalysis(member, sajuProfile);

        return MemberProfileUpdateResponse.of(
                MemberProfileResponse.from(member, sajuProfile),
                false
        );
    }

    private void validatePhoneNumberChange(String currentPhoneNumber, String newPhoneNumber, boolean phoneNumberChanged) {
        if (!phoneNumberChanged) {
            return;
        }

        memberRepository.findByPhoneNumber(newPhoneNumber)
                .ifPresent(existingMember -> {
                    if (!existingMember.getPhoneNumber().equals(currentPhoneNumber)) {
                        throw new CustomException(ErrorCode.ALREADY_REGISTERED_PHONE);
                    }
                });

        SmsAuthCode smsAuthCode = smsAuthCodeRepository.findTopByPhoneNumberOrderByCreatedAtDesc(newPhoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.SMS_CODE_NOT_VERIFIED));

        if (!Boolean.TRUE.equals(smsAuthCode.getIsVerified()) || smsAuthCode.isExpired()) {
            throw new CustomException(ErrorCode.SMS_CODE_NOT_VERIFIED);
        }
    }

    private void migrateMemberIdentity(Member member, MemberProfileUpdateRequest request, MemberSajuProfile sajuProfile,
                                       String newPhoneNumber) {
        LocalDateTime now = LocalDateTime.now();

        memberIdentityMigrationRepository.migratePhoneNumber(
                member.getPhoneNumber(),
                newPhoneNumber,
                request.trimmedName(),
                request.getBirthDate(),
                now
        );

        entityManager.clear();

        Member migratedMember = memberRepository.findById(newPhoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        MemberSajuProfile migratedSajuProfile = sajuProfileRepository.findByMember(migratedMember)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_SAJU_PROFILE_NOT_FOUND));

        migratedSajuProfile.update(request.getGender(), sajuProfile.getCalendarType(), request.getBirthTime());
        sajuAnalysisService.refreshLatestAnalysis(migratedMember, migratedSajuProfile);
    }

    private MemberProfileUpdateResponse buildResponse(String phoneNumber, boolean reauthenticationRequired) {
        Member updatedMember = getMember(phoneNumber);
        MemberSajuProfile updatedSajuProfile = getRequiredSajuProfile(updatedMember);

        return MemberProfileUpdateResponse.of(
                MemberProfileResponse.from(updatedMember, updatedSajuProfile),
                reauthenticationRequired
        );
    }

    private Member getMember(String phoneNumber) {
        return memberRepository.findById(phoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private MemberSajuProfile getRequiredSajuProfile(Member member) {
        return sajuProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_SAJU_PROFILE_NOT_FOUND));
    }
}
