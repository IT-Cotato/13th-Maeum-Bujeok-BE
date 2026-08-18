package com.maumbujeok.backend.domain.diary.application;

import com.maumbujeok.backend.domain.saju.domain.SajuAnalysis;
import com.maumbujeok.backend.domain.saju.repository.SajuAnalysisRepository;
import java.util.Comparator;
import java.util.List;
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
        Element dominant = List.of(
                        new Element("목(木)", analysis.getWoodPercentage()),
                        new Element("화(火)", analysis.getFirePercentage()),
                        new Element("토(土)", analysis.getEarthPercentage()),
                        new Element("금(金)", analysis.getMetalPercentage()),
                        new Element("수(水)", analysis.getWaterPercentage()))
                .stream()
                .max(Comparator.comparingInt(Element::percentage))
                .orElseThrow();
        return "사주 균형 참고 정보: 목 " + analysis.getWoodPercentage() + "%, 화 "
                + analysis.getFirePercentage() + "%, 토 " + analysis.getEarthPercentage() + "%, 금 "
                + analysis.getMetalPercentage() + "%, 수 " + analysis.getWaterPercentage() + "%. "
                + "상대적으로 두드러진 기운은 " + dominant.label() + " " + dominant.percentage() + "%입니다.";
    }

    private record Element(String label, int percentage) {
    }
}
