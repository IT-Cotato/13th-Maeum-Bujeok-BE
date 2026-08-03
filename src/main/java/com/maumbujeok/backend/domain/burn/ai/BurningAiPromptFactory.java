package com.maumbujeok.backend.domain.burn.ai;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
@Component
public class BurningAiPromptFactory { private final ObjectMapper mapper; public BurningAiPromptFactory(ObjectMapper mapper){this.mapper=mapper;} public String instructions(){return "???????リ옇?ｅ젆???熬곣뫁夷??濡ル츎 嶺뚯쉧猷? ?袁⑤?筌?筌? ???リ섣??????亦???遊붋????쒌늾?袁ㅻご?JSON??怨쀬Ŧ ??諛댁뎽??類ｋ펲. ??????잙갭梨????꾩룇瑗???? ???낅츎??";} public String input(BurningAiRequest request){try{return mapper.writeValueAsString(Map.of("burningContent",request.sourceContent()));}catch(Exception e){throw new IllegalArgumentException("Burning AI input could not be serialized",e);}} }