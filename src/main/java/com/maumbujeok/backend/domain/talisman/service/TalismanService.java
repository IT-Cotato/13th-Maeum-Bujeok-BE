package com.maumbujeok.backend.domain.talisman.service;

import com.maumbujeok.backend.domain.talisman.domain.Talisman;
import com.maumbujeok.backend.domain.talisman.dto.TalismanItemResponse;
import com.maumbujeok.backend.domain.talisman.dto.TalismanListResponse;
import com.maumbujeok.backend.domain.talisman.repository.TalismanRepository;
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
    public TalismanListResponse getTalismans(String memberPhoneNumber) {
        List<Talisman> talismans = talismanRepository.findAllByMemberPhoneNumberOrderByCreatedAtDesc(memberPhoneNumber);

        List<TalismanItemResponse> items = talismans.stream()
                .map(TalismanItemResponse::from)
                .collect(Collectors.toList());

        return new TalismanListResponse(items, items.size());
    }
}
