package com.maumbujeok.backend.domain.burn.ai;

import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class BurningAiPromptFactory {
    private final ObjectMapper mapper;

    public BurningAiPromptFactory(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String instructions() {
        return """
                당신은 마음부적 서비스의 소각 기록 분석 AI입니다.
                소각된 기억을 비난하지 말고 따뜻하고 짧은 한국어 comment를 작성하세요.
                talismanType은 감정 단어가 아니라 아래 부적 번호 중 하나만 정수로 반환하세요.
                1=기억정리, 2=평온, 3=자책해소, 4=용기, 5=슬픔해소,
                6=미련정리, 7=유대감, 8=자기확신, 9=보호, 10=분노진정,
                11=활력, 12=희망, 13=침착.
                평온은 마음을 고요하게 하는 경우, 침착은 조급함·충동을 가라앉히는 경우에 선택하세요.
                용기는 두려움을 넘을 힘이 필요한 경우, 활력은 무기력에서 에너지를 회복하는 경우에 선택하세요.
                talismanText는 공백과 문장부호가 없는 정확히 4글자의 한국어 문구를 반환하세요.
                반드시 JSON 객체만 반환하세요.
                """;
    }

    public String input(BurningAiRequest request) {
        try {
            return mapper.writeValueAsString(Map.of("burningContent", request.sourceContent()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Burning AI input could not be serialized", e);
        }
    }
}