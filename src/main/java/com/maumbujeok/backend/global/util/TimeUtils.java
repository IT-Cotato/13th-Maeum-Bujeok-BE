package com.maumbujeok.backend.global.util;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class TimeUtils {

    public static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");
    public static final ZoneOffset SEOUL_OFFSET = ZoneOffset.ofHours(9);

    private TimeUtils() {
    }

    public static OffsetDateTime toSeoulOffset(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }

        return utcDateTime
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(SEOUL_ZONE)
                .toOffsetDateTime();
    }
}
