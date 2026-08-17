package com.maumbujeok.backend.global.util;

import com.maumbujeok.backend.domain.diary.domain.Diary;
import com.maumbujeok.backend.domain.diary.domain.DiaryAnalysis;
import com.maumbujeok.backend.domain.diary.domain.DiaryEmotion;
import com.maumbujeok.backend.domain.diary.dto.DiaryAnalysisResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryDetailResponse;
import com.maumbujeok.backend.domain.diary.dto.DiaryResponse;
import com.maumbujeok.backend.domain.member.domain.Member;
import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import com.maumbujeok.backend.domain.talisman.domain.TalismanGenerationStatus;
import com.maumbujeok.backend.domain.talisman.dto.TalismanItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class TimezoneResponseDtoTest {

    @Test
    @DisplayName("DiaryDetailResponse 변환 시 UTC createdAt/updatedAt이 KST OffsetDateTime으로 변환된다")
    void diaryDetailResponse_timezoneConversion() {
        // given
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .name("테스터")
                .build();

        Diary diary = new Diary(member, "일기 내용", DiaryEmotion.HAPPY, LocalDate.of(2026, 8, 14));
        LocalDateTime utcCreatedAt = LocalDateTime.of(2026, 8, 14, 10, 24, 0);
        LocalDateTime utcUpdatedAt = LocalDateTime.of(2026, 8, 14, 10, 30, 0);
        ReflectionTestUtils.setField(diary, "id", 1L);
        ReflectionTestUtils.setField(diary, "createdAt", utcCreatedAt);
        ReflectionTestUtils.setField(diary, "updatedAt", utcUpdatedAt);

        DiaryAnalysis analysis = new DiaryAnalysis(diary, "v1", "v1");
        ReflectionTestUtils.setField(analysis, "createdAt", utcCreatedAt);

        // when
        DiaryDetailResponse response = DiaryDetailResponse.from(diary, analysis, Collections.emptyList());

        // then
        assertThat(response.createdAt()).isEqualTo(OffsetDateTime.of(2026, 8, 14, 19, 24, 0, 0, ZoneOffset.ofHours(9)));
        assertThat(response.updatedAt()).isEqualTo(OffsetDateTime.of(2026, 8, 14, 19, 30, 0, 0, ZoneOffset.ofHours(9)));
    }

    @Test
    @DisplayName("DiaryResponse 변환 시 UTC createdAt/updatedAt이 KST OffsetDateTime으로 변환된다")
    void diaryResponse_timezoneConversion() {
        // given
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .name("테스터")
                .build();

        Diary diary = new Diary(member, "일기 내용", DiaryEmotion.HAPPY, LocalDate.of(2026, 8, 14));
        LocalDateTime utcCreatedAt = LocalDateTime.of(2026, 8, 14, 10, 24, 0);
        LocalDateTime utcUpdatedAt = LocalDateTime.of(2026, 8, 14, 10, 30, 0);
        ReflectionTestUtils.setField(diary, "id", 1L);
        ReflectionTestUtils.setField(diary, "createdAt", utcCreatedAt);
        ReflectionTestUtils.setField(diary, "updatedAt", utcUpdatedAt);

        // when
        DiaryResponse response = DiaryResponse.from(diary);

        // then
        assertThat(response.createdAt()).isEqualTo(OffsetDateTime.of(2026, 8, 14, 19, 24, 0, 0, ZoneOffset.ofHours(9)));
        assertThat(response.updatedAt()).isEqualTo(OffsetDateTime.of(2026, 8, 14, 19, 30, 0, 0, ZoneOffset.ofHours(9)));
    }

    @Test
    @DisplayName("자정 직후(KST 01:00, UTC 16:00) 작성된 DiaryAnalysis의 amuletDate가 KST 기준 당일(익일) 날짜로 포맷팅된다")
    void diaryAnalysisResponse_amuletDateBoundary() {
        // given: 2026-08-14 16:00:00 UTC (KST 2026-08-15 01:00:00)
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .name("테스터")
                .build();

        Diary diary = new Diary(member, "심야 일기", DiaryEmotion.COMFORTABLE, LocalDate.of(2026, 8, 15));
        DiaryAnalysis analysis = new DiaryAnalysis(diary, "v1", "v1");
        LocalDateTime utcCreatedAt = LocalDateTime.of(2026, 8, 14, 16, 0, 0);
        ReflectionTestUtils.setField(analysis, "createdAt", utcCreatedAt);

        // when
        DiaryAnalysisResponse response = DiaryAnalysisResponse.from(analysis);

        // then: UTC 날짜(2026.08.14)가 아닌 KST 날짜(2026.08.15)로 출력되어야 함
        assertThat(response.createdAt()).isEqualTo("2026.08.15");
    }

    @Test
    @DisplayName("TalismanItemResponse 변환 시 UTC createdAt이 KST(+09:00)로 올바르게 9시간 더해져 변환된다")
    void talismanItemResponse_timezoneConversion() {
        // given
        Member member = Member.builder()
                .phoneNumber("01012345678")
                .name("테스터")
                .build();

        Talisman talisman = Talisman.builder()
                .member(member)
                .generationStatus(TalismanGenerationStatus.COMPLETED)
                .recordedAt(LocalDate.of(2026, 8, 14))
                .build();
        LocalDateTime utcCreatedAt = LocalDateTime.of(2026, 8, 14, 10, 24, 0);
        ReflectionTestUtils.setField(talisman, "id", 100L);
        ReflectionTestUtils.setField(talisman, "createdAt", utcCreatedAt);

        // when
        TalismanItemResponse response = TalismanItemResponse.from(talisman);

        // then: 10:24 UTC -> 19:24+09:00 (10:24+09:00이 아님)
        assertThat(response.createdAt()).isEqualTo(OffsetDateTime.of(2026, 8, 14, 19, 24, 0, 0, ZoneOffset.ofHours(9)));
    }
}
