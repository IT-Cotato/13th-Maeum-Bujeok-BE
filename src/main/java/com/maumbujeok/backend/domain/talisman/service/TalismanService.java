package com.maumbujeok.backend.domain.talisman.service;

import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import com.maumbujeok.backend.domain.talisman.dto.TalismanItemResponse;
import com.maumbujeok.backend.domain.talisman.dto.TalismanListResponse;
import com.maumbujeok.backend.domain.talisman.repository.TalismanRepository;
import com.maumbujeok.backend.global.error.CustomException;
import com.maumbujeok.backend.global.error.ErrorCode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TalismanService {
    private final TalismanRepository talismanRepository;

    @Transactional(readOnly = true)
    public TalismanListResponse getTalismans(String memberPhoneNumber, Long cursor, int size) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate sunday = monday.plusDays(6);

        List<Talisman> talismans = talismanRepository.findTalismansWithCursor(
                memberPhoneNumber,
                cursor,
                monday,
                sunday,
                size
        );

        boolean hasNext = talismans.size() > size;
        List<Talisman> subset = hasNext ? talismans.subList(0, size) : talismans;

        List<TalismanItemResponse> items = subset.stream()
                .map(TalismanItemResponse::from)
                .collect(Collectors.toList());

        Long nextCursor = null;
        if (!items.isEmpty() && hasNext) {
            nextCursor = items.get(items.size() - 1).talismanId();
        }

        return new TalismanListResponse(items, items.size(), hasNext, nextCursor);
    }
    @Transactional(readOnly = true)
    public TalismanItemResponse getTalisman(String memberPhoneNumber, Long talismanId) {
        return TalismanItemResponse.from(findOwned(memberPhoneNumber, talismanId));
    }

    @Transactional
    public void deleteTalisman(String memberPhoneNumber, Long talismanId) {
        talismanRepository.delete(findOwned(memberPhoneNumber, talismanId));
    }

    private Talisman findOwned(String memberPhoneNumber, Long talismanId) {
        return talismanRepository.findByIdAndMemberPhoneNumber(talismanId, memberPhoneNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.TALISMAN_NOT_FOUND));
    }}
