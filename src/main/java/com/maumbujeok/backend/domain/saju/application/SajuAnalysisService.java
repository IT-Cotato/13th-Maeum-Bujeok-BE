package com.maumbujeok.backend.domain.saju.application;

import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.saju.ai.SajuAiPromptVersion;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.dto.CreateSajuAnalysisResponse;
import com.maumbujeok.backend.domain.saju.dto.SajuAnalysisResponse;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SajuAnalysisService {
    private static final String START_MESSAGE = "사주 분석 요청이 생성되었습니다.";

    private final MemberRepository memberRepository;
    private final MemberSajuProfileRepository sajuProfileRepository;
    private final SajuAnalysisRepository sajuAnalysisRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public CreateSajuAnalysisResponse create(String memberPhoneNumber) {
        Member member = memberRepository.findById(memberPhoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        MemberSajuProfile sajuProfile = sajuProfileRepository.findByMember(member)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_SAJU_PROFILE_NOT_FOUND));
        SajuAnalysis analysis = refreshLatestAnalysis(member, sajuProfile, true);

        return CreateSajuAnalysisResponse.from(analysis, START_MESSAGE);
    }

    @Transactional(readOnly = true)
    public SajuAnalysisResponse get(String memberPhoneNumber, Long analysisId) {
        SajuAnalysis analysis = sajuAnalysisRepository.findByIdAndMemberPhoneNumber(analysisId, memberPhoneNumber)
                .orElseThrow(() -> new SajuAnalysisRequestException(
                        ErrorCode.SAJU_ANALYSIS_NOT_FOUND,
                        "사주 분석 요청을 찾을 수 없습니다."
                ));
        return SajuAnalysisResponse.from(analysis);
    }

    @Transactional(readOnly = true)
    public SajuAnalysisResponse getLatest(String memberPhoneNumber) {
        SajuAnalysis analysis = selectPrimaryAnalysis(
                sajuAnalysisRepository.findAllByMemberPhoneNumberOrderByCreatedAtDescIdDesc(memberPhoneNumber)
        );
        if (analysis == null) {
            throw new SajuAnalysisRequestException(
                    ErrorCode.SAJU_ANALYSIS_NOT_FOUND,
                    "사주 분석 요청을 찾을 수 없습니다."
            );
        }
        return SajuAnalysisResponse.from(analysis);
    }

    @Transactional
    public void refreshLatestAnalysis(Member member, MemberSajuProfile sajuProfile) {
        refreshLatestAnalysis(member, sajuProfile, false);
    }

    @Transactional
    public SajuAnalysis refreshLatestAnalysis(Member member, MemberSajuProfile sajuProfile, boolean force) {
        validateProfile(member, sajuProfile);

        List<SajuAnalysis> analyses = sajuAnalysisRepository.findAllByMemberPhoneNumberForUpdate(member.getPhoneNumber());
        SajuAnalysis analysis = selectPrimaryAnalysis(analyses);
        if (analysis == null) {
            analysis = sajuAnalysisRepository.save(new SajuAnalysis(
                    member,
                    member.getBirthDate(),
                    sajuProfile.getGender(),
                    sajuProfile.getCalendarType(),
                    sajuProfile.getBirthTime(),
                    SajuAiPromptVersion.VALUE
            ));
            publishAnalysisRequested(analysis, analysis.getRequestSequence());
            return analysis;
        }

        if (shouldRefresh(analysis, member, sajuProfile, force)) {
            long requestSequence = analysis.requestAnalysis(
                    member.getBirthDate(),
                    sajuProfile.getGender(),
                    sajuProfile.getCalendarType(),
                    sajuProfile.getBirthTime(),
                    SajuAiPromptVersion.VALUE
            );
            publishAnalysisRequested(analysis, requestSequence);
        }
        return analysis;
    }

    private void validateProfile(Member member, MemberSajuProfile sajuProfile) {
        if (!StringUtils.hasText(member.getBirthDate()) || !member.getBirthDate().matches("\\d{8}")) {
            throw new SajuAnalysisRequestException(
                    ErrorCode.INVALID_SAJU_ANALYSIS_REQUEST,
                    "사주 분석을 위해 yyyyMMdd 형식의 생년월일이 필요합니다."
            );
        }
        if (sajuProfile.getGender() == null || sajuProfile.getCalendarType() == null) {
            throw new SajuAnalysisRequestException(
                    ErrorCode.INVALID_SAJU_ANALYSIS_REQUEST,
                    "사주 분석에 필요한 성별 또는 달력 유형 정보가 누락되었습니다."
            );
        }
    }

    private boolean shouldRefresh(
            SajuAnalysis analysis,
            Member member,
            MemberSajuProfile sajuProfile,
            boolean force
    ) {
        return force
                || !analysis.matchesInput(
                        member.getBirthDate(),
                        sajuProfile.getGender(),
                        sajuProfile.getCalendarType(),
                        sajuProfile.getBirthTime()
                )
                || !analysis.hasVisibleResult();
    }

    private SajuAnalysis selectPrimaryAnalysis(List<SajuAnalysis> analyses) {
        if (analyses.isEmpty()) {
            return null;
        }

        return analyses.stream()
                .filter(SajuAnalysis::hasVisibleResult)
                .max(Comparator.comparing(
                                SajuAnalysis::getAnalyzedAt,
                                Comparator.nullsLast(Comparator.naturalOrder())
                        )
                        .thenComparing(SajuAnalysis::getId))
                .orElse(analyses.get(0));
    }

    private void publishAnalysisRequested(SajuAnalysis analysis, long requestSequence) {
        eventPublisher.publishEvent(new SajuAnalysisRequestedEvent(analysis.getId(), requestSequence));
    }
}
