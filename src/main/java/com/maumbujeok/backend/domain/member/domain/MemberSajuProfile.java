package com.maumbujeok.backend.domain.member.domain;

import com.maumbujeok.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "member_saju_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberSajuProfile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "saju_profile_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "calendar_type", nullable = false, length = 20)
    private CalendarType calendarType;

    @Column(name = "birth_time")
    private LocalTime birthTime;

    @Builder
    public MemberSajuProfile(Member member, Gender gender, CalendarType calendarType, LocalTime birthTime) {
        this.member = member;
        this.gender = gender;
        this.calendarType = calendarType;
        this.birthTime = birthTime;
    }

    public enum Gender {
        MALE, FEMALE, NONE
    }

    public enum CalendarType {
        SOLAR, LUNAR, LUNAR_LEAP
    }
}
