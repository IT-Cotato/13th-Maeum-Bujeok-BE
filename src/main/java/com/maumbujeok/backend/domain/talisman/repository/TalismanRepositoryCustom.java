package com.maumbujeok.backend.domain.talisman.repository;

import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import java.time.LocalDate;
import java.util.List;

public interface TalismanRepositoryCustom {
    List<Talisman> findTalismansWithCursor(
            String memberPhoneNumber,
            Long cursor,
            LocalDate startOfWeek,
            LocalDate endOfWeek,
            int size
    );
}
