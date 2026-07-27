package com.maumbujeok.backend.domain.talisman.repository;

import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import java.time.LocalDateTime;
import java.util.List;

public interface TalismanRepositoryCustom {
    List<Talisman> findTalismansWithCursor(
            String memberPhoneNumber,
            Long cursor,
            LocalDateTime startOfWeek,
            LocalDateTime endOfWeek,
            int size
    );
}
