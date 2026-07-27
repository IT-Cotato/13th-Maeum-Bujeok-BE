package com.maumbujeok.backend.global.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimeConfig {
    @Bean
    public Clock serviceClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
