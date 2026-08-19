package com.maumbujeok.backend.domain.member.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.maumbujeok.backend.domain.auth.domain.SmsAuthCode;
import com.maumbujeok.backend.domain.auth.repository.SmsAuthCodeRepository;
import com.maumbujeok.backend.domain.home.application.HomeSummaryRefreshRequestedEvent;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.member.domain.MemberSajuProfile;
import com.maumbujeok.backend.domain.member.dto.MemberProfileUpdateRequest;
import com.maumbujeok.backend.domain.member.repository.MemberIdentityMigrationRepository;
import com.maumbujeok.backend.domain.member.repository.MemberRepository;
import com.maumbujeok.backend.domain.member.repository.MemberSajuProfileRepository;
import com.maumbujeok.backend.domain.saju.application.SajuAnalysisService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceEventTest {

    @Mock MemberRepository memberRepository;
    @Mock MemberSajuProfileRepository sajuProfileRepository;
    @Mock SmsAuthCodeRepository smsAuthCodeRepository;
    @Mock MemberIdentityMigrationRepository memberIdentityMigrationRepository;
    @Mock SajuAnalysisService sajuAnalysisService;
    @Mock ApplicationEventPublisher eventPublisher;
    @Mock EntityManager entityManager;

    @InjectMocks MemberProfileService memberProfileService;

    @Captor ArgumentCaptor<HomeSummaryRefreshRequestedEvent> eventCaptor;

    private Member member;
    private MemberSajuProfile sajuProfile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(memberProfileService, "entityManager", entityManager);

        member = Member.builder()
                .phoneNumber("01011112222")
                .name("테스터")
                .birthDate("19950515")
                .role(Member.Role.ROLE_USER)
                .provider(Member.Provider.LOCAL)
                .build();

        sajuProfile = MemberSajuProfile.builder()
                .member(member)
                .gender(MemberSajuProfile.Gender.MALE)
                .calendarType(MemberSajuProfile.CalendarType.SOLAR)
                .birthTime(LocalTime.of(14, 30))
                .build();

        when(memberRepository.findById("01011112222")).thenReturn(Optional.of(member));
        when(sajuProfileRepository.findByMember(member)).thenReturn(Optional.of(sajuProfile));
    }

    private MemberProfileUpdateRequest createRequest(String name, String birthDate, LocalTime birthTime, String phone, MemberSajuProfile.Gender gender) {
        MemberProfileUpdateRequest req = new MemberProfileUpdateRequest();
        ReflectionTestUtils.setField(req, "name", name);
        ReflectionTestUtils.setField(req, "birthDate", birthDate);
        ReflectionTestUtils.setField(req, "birthTime", birthTime);
        ReflectionTestUtils.setField(req, "phoneNumber", phone);
        ReflectionTestUtils.setField(req, "gender", gender);
        return req;
    }

    @Test
    @DisplayName("생년월일(birthDate) 변경 시 HomeSummaryRefreshRequestedEvent가 1회 발행된다")
    void updateProfile_whenBirthDateChanges_publishesHomeSummaryRefreshEvent() {
        MemberProfileUpdateRequest req = createRequest(
                "테스터", "19990101", LocalTime.of(14, 30), "01011112222", MemberSajuProfile.Gender.MALE
        );

        memberProfileService.updateMyProfile("01011112222", req);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals("01011112222", eventCaptor.getValue().memberPhoneNumber());
    }

    @Test
    @DisplayName("태어난 시간(birthTime) 변경 시 HomeSummaryRefreshRequestedEvent가 1회 발행된다")
    void updateProfile_whenBirthTimeChanges_publishesHomeSummaryRefreshEvent() {
        MemberProfileUpdateRequest req = createRequest(
                "테스터", "19950515", LocalTime.of(18, 0), "01011112222", MemberSajuProfile.Gender.MALE
        );

        memberProfileService.updateMyProfile("01011112222", req);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals("01011112222", eventCaptor.getValue().memberPhoneNumber());
    }

    @Test
    @DisplayName("성별(gender) 변경 시 HomeSummaryRefreshRequestedEvent가 1회 발행된다")
    void updateProfile_whenGenderChanges_publishesHomeSummaryRefreshEvent() {
        MemberProfileUpdateRequest req = createRequest(
                "테스터", "19950515", LocalTime.of(14, 30), "01011112222", MemberSajuProfile.Gender.FEMALE
        );

        memberProfileService.updateMyProfile("01011112222", req);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals("01011112222", eventCaptor.getValue().memberPhoneNumber());
    }

    @Test
    @DisplayName("이름(name)만 변경 시 HomeSummaryRefreshRequestedEvent가 발행되지 않는다")
    void updateProfile_whenOnlyNameChanges_doesNotPublishHomeSummaryRefreshEvent() {
        MemberProfileUpdateRequest req = createRequest(
                "새로운이름", "19950515", LocalTime.of(14, 30), "01011112222", MemberSajuProfile.Gender.MALE
        );

        memberProfileService.updateMyProfile("01011112222", req);

        verify(eventPublisher, never()).publishEvent(any(HomeSummaryRefreshRequestedEvent.class));
    }

    @Test
    @DisplayName("동일한 사주값으로 update 시 HomeSummaryRefreshRequestedEvent가 발행되지 않는다")
    void updateProfile_whenSameSajuValues_doesNotPublishHomeSummaryRefreshEvent() {
        MemberProfileUpdateRequest req = createRequest(
                "테스터", "19950515", LocalTime.of(14, 30), "01011112222", MemberSajuProfile.Gender.MALE
        );

        memberProfileService.updateMyProfile("01011112222", req);

        verify(eventPublisher, never()).publishEvent(any(HomeSummaryRefreshRequestedEvent.class));
    }

    @Test
    @DisplayName("전화번호와 생년월일이 동시에 변경된 경우 새 전화번호로 HomeSummaryRefreshRequestedEvent가 발행된다")
    void updateProfile_whenPhoneAndBirthDateChange_publishesEventWithNewPhoneNumber() {
        String newPhone = "01099998888";
        SmsAuthCode smsAuthCode = SmsAuthCode.builder()
                .phoneNumber(newPhone)
                .code("123456")
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .build();
        smsAuthCode.verify();

        Member migratedMember = Member.builder()
                .phoneNumber(newPhone)
                .name("테스터")
                .birthDate("20000101")
                .role(Member.Role.ROLE_USER)
                .provider(Member.Provider.LOCAL)
                .build();

        MemberSajuProfile migratedSajuProfile = MemberSajuProfile.builder()
                .member(migratedMember)
                .gender(MemberSajuProfile.Gender.MALE)
                .calendarType(MemberSajuProfile.CalendarType.SOLAR)
                .birthTime(LocalTime.of(14, 30))
                .build();

        when(memberRepository.findByPhoneNumber(newPhone)).thenReturn(Optional.empty());
        when(smsAuthCodeRepository.findTopByPhoneNumberOrderByCreatedAtDesc(newPhone)).thenReturn(Optional.of(smsAuthCode));
        when(memberRepository.findById(newPhone)).thenReturn(Optional.of(migratedMember));
        when(sajuProfileRepository.findByMember(migratedMember)).thenReturn(Optional.of(migratedSajuProfile));

        MemberProfileUpdateRequest req = createRequest(
                "테스터", "20000101", LocalTime.of(14, 30), newPhone, MemberSajuProfile.Gender.MALE
        );

        memberProfileService.updateMyProfile("01011112222", req);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertEquals(newPhone, eventCaptor.getValue().memberPhoneNumber());
    }
}
