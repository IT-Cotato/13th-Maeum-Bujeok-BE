package com.maumbujeok.backend.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class TimeUtilsTest {

    @Test
    @DisplayName("UTC LocalDateTime이 KST(+09:00) OffsetDateTime으로 올바르게 변환된다")
    void toSeoulOffset_success() {
        // given: 2026-08-14 10:24:00 UTC (실제 KST 19:24:00)
        LocalDateTime utcDateTime = LocalDateTime.of(2026, 8, 14, 10, 24, 0);

        // when
        OffsetDateTime seoulOffset = TimeUtils.toSeoulOffset(utcDateTime);

        // then
        assertThat(seoulOffset).isNotNull();
        assertThat(seoulOffset.getYear()).isEqualTo(2026);
        assertThat(seoulOffset.getMonthValue()).isEqualTo(8);
        assertThat(seoulOffset.getDayOfMonth()).isEqualTo(14);
        assertThat(seoulOffset.getHour()).isEqualTo(19);
        assertThat(seoulOffset.getMinute()).isEqualTo(24);
        assertThat(seoulOffset.getSecond()).isEqualTo(0);
        assertThat(seoulOffset.getOffset()).isEqualTo(ZoneOffset.ofHours(9));
        assertThat(seoulOffset).isEqualTo(OffsetDateTime.of(2026, 8, 14, 19, 24, 0, 0, ZoneOffset.ofHours(9)));
    }

    @Test
    @DisplayName("자정 경계 시간(16:00 UTC)이 익일(01:00 KST)로 올바르게 날짜가 변경된다")
    void toSeoulOffset_dateBoundary() {
        // given: 2026-08-14 16:00:00 UTC (KST 2026-08-15 01:00:00)
        LocalDateTime utcDateTime = LocalDateTime.of(2026, 8, 14, 16, 0, 0);

        // when
        OffsetDateTime seoulOffset = TimeUtils.toSeoulOffset(utcDateTime);

        // then
        assertThat(seoulOffset).isNotNull();
        assertThat(seoulOffset.getYear()).isEqualTo(2026);
        assertThat(seoulOffset.getMonthValue()).isEqualTo(8);
        assertThat(seoulOffset.getDayOfMonth()).isEqualTo(15);
        assertThat(seoulOffset.getHour()).isEqualTo(1);
        assertThat(seoulOffset.getMinute()).isEqualTo(0);
        assertThat(seoulOffset.getOffset()).isEqualTo(ZoneOffset.ofHours(9));
        assertThat(seoulOffset).isEqualTo(OffsetDateTime.of(2026, 8, 15, 1, 0, 0, 0, ZoneOffset.ofHours(9)));
    }

    @Test
    @DisplayName("null 입력 시 null을 반환하여 NPE가 발생하지 않는다")
    void toSeoulOffset_nullSafe() {
        // when
        OffsetDateTime result = TimeUtils.toSeoulOffset(null);

        // then
        assertThat(result).isNull();
    }
}
