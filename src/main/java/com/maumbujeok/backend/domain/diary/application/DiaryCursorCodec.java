package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class DiaryCursorCodec {
    public DiaryCursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) return new DiaryCursor(null, null);
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":", -1);
            if (parts.length != 2) throw new IllegalArgumentException();
            return new DiaryCursor(LocalDate.parse(parts[0]), Long.parseLong(parts[1]));
        } catch (RuntimeException exception) {
            throw new CustomException(ErrorCode.INVALID_DIARY_CURSOR);
        }
    }

    public String encode(LocalDate date, Long id) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((date + ":" + id).getBytes(StandardCharsets.UTF_8));
    }

    public record DiaryCursor(LocalDate recordedDate, Long diaryId) {}
}
