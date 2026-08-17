package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SajuContextProvider {
    private final SajuAnalysisRepository sajuAnalysisRepository;

    public String getContext(String memberPhoneNumber) {
        if (memberPhoneNumber == null || memberPhoneNumber.isBlank()) {
            return "";
        }
        return sajuAnalysisRepository.findAllByMemberPhoneNumberOrderByCreatedAtDescIdDesc(memberPhoneNumber).stream()
                .filter(SajuAnalysis::hasVisibleResult)
                .findFirst()
                .map(this::format)
                .orElse("");
    }

    private String format(SajuAnalysis analysis) {
        return "오행 균형 참고 정보: 목 " + analysis.getWoodPercentage() + "%, 화 "
                + analysis.getFirePercentage() + "%, 토 " + analysis.getEarthPercentage() + "%, 금 "
                + analysis.getMetalPercentage() + "%, 수 " + analysis.getWaterPercentage() + "%";
    }
}
