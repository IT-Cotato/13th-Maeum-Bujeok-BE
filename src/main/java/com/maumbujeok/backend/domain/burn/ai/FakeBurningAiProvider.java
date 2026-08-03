package com.maumbujeok.backend.domain.burn.ai;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
@Component @ConditionalOnProperty(name="ai.provider", havingValue="fake", matchIfMissing=true)
public class FakeBurningAiProvider implements BurningAiProvider {
 public BurningAiResult analyze(BurningAiRequest request){
  return new BurningAiResult("Let this memory go safely and care for your heart today.", "MIND_CLEAR", "CALM", "fake-burning-v1");
 }
}