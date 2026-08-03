package com.maumbujeok.backend.domain.burn.ai;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
@Component
public class BurningAiPromptFactory { private final ObjectMapper mapper; public BurningAiPromptFactory(ObjectMapper mapper){this.mapper=mapper;} public String instructions(){return "?뚭컖??湲곗뼲???꾨줈?섎뒗 吏㏃? 肄붾찘?몄? ??湲???대궡??遺??臾멸뎄瑜?JSON?쇰줈 ?앹꽦?쒕떎. ?먮Ц??洹몃?濡?諛섎났?섏? ?딅뒗??";} public String input(BurningAiRequest request){try{return mapper.writeValueAsString(Map.of("burningContent",request.sourceContent()));}catch(Exception e){throw new IllegalArgumentException("Burning AI input could not be serialized",e);}} }