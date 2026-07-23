package com.maumbujeok.backend.global.ai.emotion;

import java.util.Arrays;
import java.util.List;

public enum ReportEmotion {
    JOY("기쁨", List.of("행복", "즐거움", "즐거운", "뿌듯", "감사")),
    ANTICIPATION("기대", List.of("설렘", "희망", "기대감")),
    COMFORT("편안", List.of("평온", "안정", "차분", "평화로움")),
    EMBARRASSMENT("당황", List.of("당혹", "놀람", "혼란", "난감")),
    LETHARGY("무기력", List.of("지침", "피곤", "의욕 없음", "번아웃")),
    ANXIETY("불안", List.of("걱정", "초조", "긴장", "두려움", "공포")),
    ANGER("분노", List.of("화남", "짜증", "억울", "격분")),
    SADNESS("슬픔", List.of("우울", "외로움", "서운", "상실", "비통")),
    OTHER("기타", List.of());

    private final String label;
    private final List<String> aliases;

    ReportEmotion(String label, List<String> aliases) {
        this.label = label;
        this.aliases = aliases;
    }

    public String getLabel() {
        return label;
    }

    public static ReportEmotion fromLabel(String value) {
        if (value == null || value.isBlank()) return OTHER;
        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(emotion -> emotion.label.equals(normalized))
                .findFirst()
                .orElseGet(() -> fromFreeText(normalized));
    }

    public static ReportEmotion fromFreeText(String value) {
        if (value == null || value.isBlank()) return OTHER;
        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(emotion -> emotion != OTHER)
                .filter(emotion -> normalized.contains(emotion.label)
                        || emotion.aliases.stream().anyMatch(normalized::contains))
                .findFirst()
                .orElse(OTHER);
    }

    public static List<String> labels() {
        return Arrays.stream(values()).map(ReportEmotion::getLabel).toList();
    }
}
