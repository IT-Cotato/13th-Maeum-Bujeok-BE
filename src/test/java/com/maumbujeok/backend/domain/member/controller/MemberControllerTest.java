package com.maumbujeok.backend.domain.member.controller;

import com.maumbujeok.backend.domain.auth.domain.RefreshToken;
import com.maumbujeok.backend.domain.auth.domain.SmsAuthCode;
import com.maumbujeok.backend.domain.auth.repository.RefreshTokenRepository;
import com.maumbujeok.backend.domain.auth.repository.SmsAuthCodeRepository;
import com.maumbujeok.backend.domain.burn.domain.Burning;
import com.maumbujeok.backend.domain.burn.domain.BurningSourceType;
import com.maumbujeok.backend.domain.burn.repository.BurningRepository;
import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryEmotion;
import com.maumbujeok.backend.domain.diary.repository.DiaryRepository;
import com.maumbujeok.backend.domain.home.domain.HomeSummary;
import com.maumbujeok.backend.domain.home.domain.PrimaryElement;
import com.maumbujeok.backend.domain.home.repository.HomeSummaryRepository;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberNotificationSetting;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.repository.MemberNotificationSettingRepository;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.report.domain.EmotionReport;
import com.maumbujeok.backend.domain.report.domain.EmotionReportType;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlow;
import com.maumbujeok.backend.domain.report.domain.NextWeekFlowGenerationStatus;
import com.maumbujeok.backend.domain.report.repository.EmotionReportRepository;
import com.maumbujeok.backend.domain.report.repository.NextWeekFlowRepository;
import com.maumbujeok.backend.domain.saju.ai.SajuAiPromptVersion;
import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import com.maumbujeok.backend.domain.talisman.domain.TalismanGenerationStatus;
import com.maumbujeok.backend.domain.talisman.repository.TalismanRepository;
import com.maumbujeok.backend.domain.upload.domain.DiaryUpload;
import com.maumbujeok.backend.domain.upload.repository.DiaryUploadRepository;
import com.maumbujeok.backend.global.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerTest {

    private static final String ORIGINAL_PHONE = "01012345678";
    private static final String UPDATED_PHONE = "01055556666";

    @Autowired MockMvc mockMvc;
    @Autowired MemberRepository memberRepository;
    @Autowired MemberSajuProfileRepository sajuProfileRepository;
    @Autowired MemberNotificationSettingRepository notificationSettingRepository;
    @Autowired DiaryRepository diaryRepository;
    @Autowired DiaryUploadRepository diaryUploadRepository;
    @Autowired BurningRepository burningRepository;
    @Autowired TalismanRepository talismanRepository;
    @Autowired EmotionReportRepository emotionReportRepository;
    @Autowired NextWeekFlowRepository nextWeekFlowRepository;
    @Autowired HomeSummaryRepository homeSummaryRepository;
    @Autowired SajuAnalysisRepository sajuAnalysisRepository;
    @Autowired RefreshTokenRepository refreshTokenRepository;
    @Autowired SmsAuthCodeRepository smsAuthCodeRepository;
    @Autowired JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        sajuAnalysisRepository.deleteAllInBatch();
        nextWeekFlowRepository.deleteAllInBatch();
        emotionReportRepository.deleteAllInBatch();
        talismanRepository.deleteAllInBatch();
        burningRepository.deleteAllInBatch();
        diaryUploadRepository.deleteAllInBatch();
        diaryRepository.deleteAllInBatch();
        homeSummaryRepository.deleteAllInBatch();
        notificationSettingRepository.deleteAllInBatch();
        sajuProfileRepository.deleteAllInBatch();
        refreshTokenRepository.deleteAllInBatch();
        smsAuthCodeRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    void onboardingStoresBirthDateSajuAndConsent() throws Exception {
        Member member = memberRepository.save(Member.builder()
                .phoneNumber(ORIGINAL_PHONE)
                .name("홍길동")
                .provider(Member.Provider.LOCAL)
                .role(Member.Role.ROLE_USER)
                .build());

        mockMvc.perform(post("/api/members/onboarding")
                        .header("Authorization", "Bearer " + token(member.getPhoneNumber(), member.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "19990101",
                                  "gender": "FEMALE",
                                  "calendarType": "SOLAR",
                                  "birthTime": "14:30",
                                  "termsAgreed": true,
                                  "privacyAgreed": true,
                                  "sensitiveDataAgreed": true,
                                  "marketingAgreed": false
                                }
                                """))
                .andExpect(status().isOk());

        Member savedMember = memberRepository.findById(member.getPhoneNumber()).orElseThrow();
        assertEquals("19990101", savedMember.getBirthDate());
        assertNotNull(savedMember.getTermsAgreedAt());
        assertNotNull(savedMember.getPrivacyAgreedAt());
        assertNotNull(savedMember.getSensitiveDataAgreedAt());
        assertNull(savedMember.getMarketingAgreedAt());

        MemberSajuProfile sajuProfile = sajuProfileRepository.findByMember(savedMember).orElseThrow();
        assertEquals(MemberSajuProfile.Gender.FEMALE, sajuProfile.getGender());
        assertEquals(MemberSajuProfile.CalendarType.SOLAR, sajuProfile.getCalendarType());
        assertEquals("14:30", sajuProfile.getBirthTime().toString());
    }

    @Test
    void onboardingRejectsNonexistentBirthDate() throws Exception {
        Member member = memberRepository.save(Member.builder()
                .phoneNumber("01098765432")
                .name("tester")
                .provider(Member.Provider.LOCAL)
                .role(Member.Role.ROLE_USER)
                .build());

        mockMvc.perform(post("/api/members/onboarding")
                        .header("Authorization", "Bearer " + token(member.getPhoneNumber(), member.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "birthDate": "19990230",
                                  "gender": "FEMALE",
                                  "calendarType": "SOLAR",
                                  "termsAgreed": true,
                                  "privacyAgreed": true
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400"));
    }

    @Test
    void getProfileReturnsMemberAndSajuProfile() throws Exception {
        Member member = saveMember("조회 사용자", ORIGINAL_PHONE);
        sajuProfileRepository.save(MemberSajuProfile.builder()
                .member(member)
                .gender(MemberSajuProfile.Gender.FEMALE)
                .calendarType(MemberSajuProfile.CalendarType.SOLAR)
                .birthTime(java.time.LocalTime.of(14, 30))
                .build());

        mockMvc.perform(get("/api/mypage/profile")
                        .header("Authorization", "Bearer " + token(member.getPhoneNumber(), member.getRole())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("조회 사용자"))
                .andExpect(jsonPath("$.data.phoneNumber").value(ORIGINAL_PHONE))
                .andExpect(jsonPath("$.data.birthDate").value("19900101"))
                .andExpect(jsonPath("$.data.gender").value("FEMALE"))
                .andExpect(jsonPath("$.data.birthTime").value("14:30:00"));
    }

    @Test
    void updateProfileMovesReferencesWhenPhoneNumberChanges() throws Exception {
        Member member = saveMember("기존 이름", ORIGINAL_PHONE);
        sajuProfileRepository.save(MemberSajuProfile.builder()
                .member(member)
                .gender(MemberSajuProfile.Gender.MALE)
                .calendarType(MemberSajuProfile.CalendarType.SOLAR)
                .birthTime(java.time.LocalTime.of(9, 15))
                .build());
        notificationSettingRepository.save(MemberNotificationSetting.create(member));

        Diary diary = diaryRepository.save(new Diary(
                member,
                "오늘의 기록",
                DiaryEmotion.HAPPY,
                LocalDate.of(2026, 8, 1)
        ));
        DiaryUpload upload = diaryUploadRepository.save(new DiaryUpload(
                UUID.randomUUID(),
                member,
                "uploads/profile-test.png",
                "image/png",
                1_024L
        ));
        Burning burning = burningRepository.save(new Burning(
                member,
                BurningSourceType.DIARY,
                diary.getId(),
                "태운 기록",
                LocalDateTime.of(2026, 8, 2, 10, 0)
        ));
        Talisman talisman = talismanRepository.save(Talisman.builder()
                .member(member)
                .burnRitualId(burning.getId())
                .designType("sun")
                .title("테스트 부적")
                .message("행운이 오길")
                .imageUrl("https://example.com/talisman.png")
                .usedSaju("갑자년")
                .generationStatus(TalismanGenerationStatus.COMPLETED)
                .recordedAt(LocalDate.of(2026, 8, 2))
                .build());
        EmotionReport report = emotionReportRepository.save(new EmotionReport(
                member,
                EmotionReportType.WEEKLY,
                LocalDate.of(2026, 7, 27),
                LocalDate.of(2026, 8, 2),
                "weekly-v1"
        ));
        NextWeekFlow nextWeekFlow = nextWeekFlowRepository.save(NextWeekFlow.builder()
                .member(member)
                .emotionReport(report)
                .weekStart(LocalDate.of(2026, 8, 3))
                .periodStart(LocalDate.of(2026, 7, 27))
                .periodEnd(LocalDate.of(2026, 8, 2))
                .generationStatus(NextWeekFlowGenerationStatus.PROCESSING)
                .build());
        homeSummaryRepository.save(HomeSummary.builder()
                .member(member)
                .summaryDate(LocalDate.of(2026, 8, 7))
                .primaryElement(PrimaryElement.FIRE)
                .todayLuck("좋은 소식이 찾아옵니다.")
                .todayEnergy("차분하게 정리하는 힘이 강합니다.")
                .modelName("gpt-test")
                .reportVersion("home-v1")
                .build());
        SajuAnalysis sajuAnalysis = sajuAnalysisRepository.save(new SajuAnalysis(
                member,
                member.getBirthDate(),
                MemberSajuProfile.Gender.MALE,
                MemberSajuProfile.CalendarType.SOLAR,
                java.time.LocalTime.of(9, 15),
                SajuAiPromptVersion.VALUE
        ));
        refreshTokenRepository.save(RefreshToken.builder()
                .userKey(member.getPhoneNumber())
                .token("refresh-token-for-profile-update")
                .expiredAt(LocalDateTime.now().plusDays(7))
                .build());

        SmsAuthCode smsAuthCode = SmsAuthCode.builder()
                .phoneNumber(UPDATED_PHONE)
                .code("123456")
                .expiredAt(LocalDateTime.now().plusMinutes(3))
                .build();
        smsAuthCode.verify();
        smsAuthCodeRepository.save(smsAuthCode);

        mockMvc.perform(patch("/api/mypage/profile")
                        .header("Authorization", "Bearer " + token(member.getPhoneNumber(), member.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 이름",
                                  "birthDate": "19951231",
                                  "birthTime": "18:45",
                                  "phoneNumber": "010-5555-6666",
                                  "gender": "여성"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profile.name").value("새 이름"))
                .andExpect(jsonPath("$.data.profile.birthDate").value("19951231"))
                .andExpect(jsonPath("$.data.profile.phoneNumber").value(UPDATED_PHONE))
                .andExpect(jsonPath("$.data.profile.gender").value("FEMALE"))
                .andExpect(jsonPath("$.data.reauthenticationRequired").value(true));

        assertFalse(memberRepository.findById(ORIGINAL_PHONE).isPresent());

        Member migratedMember = memberRepository.findById(UPDATED_PHONE).orElseThrow();
        assertEquals("새 이름", migratedMember.getName());
        assertEquals("19951231", migratedMember.getBirthDate());

        MemberSajuProfile migratedSajuProfile = sajuProfileRepository.findByMember(migratedMember).orElseThrow();
        assertEquals(MemberSajuProfile.Gender.FEMALE, migratedSajuProfile.getGender());
        assertEquals(MemberSajuProfile.CalendarType.SOLAR, migratedSajuProfile.getCalendarType());
        assertEquals("18:45", migratedSajuProfile.getBirthTime().toString());

        assertTrue(notificationSettingRepository.findByMemberPhoneNumber(UPDATED_PHONE).isPresent());
        assertEquals(1, diaryRepository.findAllByMemberPhoneNumberOrderByRecordedDateDescCreatedAtDescIdDesc(UPDATED_PHONE).size());
        assertTrue(diaryUploadRepository.findByIdAndMemberPhoneNumber(upload.getId(), UPDATED_PHONE).isPresent());
        assertTrue(burningRepository.findByIdAndMemberPhoneNumber(burning.getId(), UPDATED_PHONE).isPresent());
        assertTrue(talismanRepository.findByIdAndMemberPhoneNumber(talisman.getId(), UPDATED_PHONE).isPresent());
        assertTrue(emotionReportRepository.findByMemberPhoneNumberAndReportTypeAndPeriodStart(
                UPDATED_PHONE, EmotionReportType.WEEKLY, LocalDate.of(2026, 7, 27)
        ).isPresent());
        assertTrue(nextWeekFlowRepository.findByMemberPhoneNumberAndWeekStart(
                UPDATED_PHONE, nextWeekFlow.getWeekStart()
        ).isPresent());
        assertTrue(homeSummaryRepository.findByMemberPhoneNumberAndSummaryDate(
                UPDATED_PHONE, LocalDate.of(2026, 8, 7)
        ).isPresent());
        assertTrue(sajuAnalysisRepository.findByIdAndMemberPhoneNumber(sajuAnalysis.getId(), UPDATED_PHONE).isPresent());
        assertFalse(sajuAnalysisRepository.findByIdAndMemberPhoneNumber(sajuAnalysis.getId(), ORIGINAL_PHONE).isPresent());
        assertTrue(refreshTokenRepository.findByUserKey(UPDATED_PHONE).isPresent());
        assertFalse(refreshTokenRepository.findByUserKey(ORIGINAL_PHONE).isPresent());

        mockMvc.perform(get("/api/mypage/profile")
                        .header("Authorization", "Bearer " + token(ORIGINAL_PHONE, member.getRole())))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/mypage/profile")
                        .header("Authorization", "Bearer " + token(UPDATED_PHONE, member.getRole())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phoneNumber").value(UPDATED_PHONE));
    }

    @Test
    void updateProfileRejectsDuplicatePhoneNumber() throws Exception {
        Member member = saveMember("수정 대상", ORIGINAL_PHONE);
        saveSajuProfile(member);
        saveMember("기존 가입자", UPDATED_PHONE);

        mockMvc.perform(patch("/api/mypage/profile")
                        .header("Authorization", "Bearer " + token(member.getPhoneNumber(), member.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 이름",
                                  "birthDate": "19951231",
                                  "birthTime": "18:45",
                                  "phoneNumber": "01055556666",
                                  "gender": "FEMALE"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("AUTH_009"));
    }

    @Test
    void updateProfileRequiresVerifiedSmsCodeWhenPhoneNumberChanges() throws Exception {
        Member member = saveMember("수정 대상", ORIGINAL_PHONE);
        saveSajuProfile(member);

        mockMvc.perform(patch("/api/mypage/profile")
                        .header("Authorization", "Bearer " + token(member.getPhoneNumber(), member.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 이름",
                                  "birthDate": "19951231",
                                  "birthTime": "18:45",
                                  "phoneNumber": "01055556666",
                                  "gender": "FEMALE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SMS_003"));
    }

    @Test
    void updateProfileRejectsInvalidBirthTimeFormat() throws Exception {
        Member member = saveMember("수정 대상", ORIGINAL_PHONE);
        saveSajuProfile(member);

        mockMvc.perform(patch("/api/mypage/profile")
                        .header("Authorization", "Bearer " + token(member.getPhoneNumber(), member.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 이름",
                                  "birthDate": "19951231",
                                  "birthTime": "25:61",
                                  "phoneNumber": "01012345678",
                                  "gender": "FEMALE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400"))
                .andExpect(jsonPath("$.message").value("태어난 시간은 HH:mm 형식이어야 합니다."));
    }

    @Test
    void updateProfileRejectsInvalidGenderValue() throws Exception {
        Member member = saveMember("수정 대상", ORIGINAL_PHONE);
        saveSajuProfile(member);

        mockMvc.perform(patch("/api/mypage/profile")
                        .header("Authorization", "Bearer " + token(member.getPhoneNumber(), member.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 이름",
                                  "birthDate": "19951231",
                                  "birthTime": "18:45",
                                  "phoneNumber": "01012345678",
                                  "gender": "UNKNOWN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400"))
                .andExpect(jsonPath("$.message").value("성별은 여성, 남성, 선택안함 또는 FEMALE, MALE, NONE 중 하나여야 합니다."));
    }

    @Test
    void updateProfileFailsWhenSajuProfileDoesNotExist() throws Exception {
        Member member = saveMember("수정 대상", ORIGINAL_PHONE);

        mockMvc.perform(patch("/api/mypage/profile")
                        .header("Authorization", "Bearer " + token(member.getPhoneNumber(), member.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 이름",
                                  "birthDate": "19951231",
                                  "birthTime": "18:45",
                                  "phoneNumber": "01012345678",
                                  "gender": "FEMALE"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MEMBER_003"));
    }

    @Test
    void updateProfileRejectsInvalidPhoneNumberFormat() throws Exception {
        Member member = saveMember("수정 대상", ORIGINAL_PHONE);
        saveSajuProfile(member);

        mockMvc.perform(patch("/api/mypage/profile")
                        .header("Authorization", "Bearer " + token(member.getPhoneNumber(), member.getRole()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "새 이름",
                                  "birthDate": "19951231",
                                  "birthTime": "18:45",
                                  "phoneNumber": "010-12",
                                  "gender": "FEMALE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COMMON_400"))
                .andExpect(jsonPath("$.message").value("전화번호는 하이픈 포함 또는 제외한 10~11자리 숫자여야 합니다."));
    }

    private Member saveMember(String name, String phoneNumber) {
        return memberRepository.save(Member.builder()
                .name(name)
                .phoneNumber(phoneNumber)
                .passwordHash("encoded-password")
                .birthDate("19900101")
                .provider(Member.Provider.LOCAL)
                .role(Member.Role.ROLE_USER)
                .build());
    }

    private MemberSajuProfile saveSajuProfile(Member member) {
        return sajuProfileRepository.save(MemberSajuProfile.builder()
                .member(member)
                .gender(MemberSajuProfile.Gender.MALE)
                .calendarType(MemberSajuProfile.CalendarType.SOLAR)
                .birthTime(java.time.LocalTime.of(8, 0))
                .build());
    }

    private String token(String phoneNumber, Member.Role role) {
        return jwtTokenProvider.createToken(phoneNumber, role.name());
    }
}
