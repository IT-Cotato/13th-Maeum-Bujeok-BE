package com.maumbujeok.backend.domain.burn.ai;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
@Component
public class BurningAiPromptFactory { private final ObjectMapper mapper; public BurningAiPromptFactory(ObjectMapper mapper){this.mapper=mapper;} public String instructions(){return "???퍟??疫꿸퀣堉???袁⑥쨮??롫뮉 筌욁룂? ?꾨뗀李?紐? ??疫꼲????沅???봔???얜㈇?꾤몴?JSON??곗쨮 ??밴쉐??뺣뼄. ?癒???域밸챶?嚥?獄쏆꼶???? ??낅뮉??";} public String input(BurningAiRequest request){try{return mapper.writeValueAsString(Map.of("burningContent",request.sourceContent()));}catch(Exception e){throw new IllegalArgumentException("Burning AI input could not be serialized",e);}} }