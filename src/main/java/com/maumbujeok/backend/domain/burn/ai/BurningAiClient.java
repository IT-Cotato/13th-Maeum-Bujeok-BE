package com.maumbujeok.backend.domain.burn.ai;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
@Component @RequiredArgsConstructor
public class BurningAiClient { private final BurningAiProvider provider; public BurningAiResult analyze(BurningAiRequest request){return provider.analyze(request);} }