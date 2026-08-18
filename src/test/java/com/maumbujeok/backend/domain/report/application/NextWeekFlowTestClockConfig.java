package com.maumbujeok.backend.domain.report.application;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration(proxyBeanMethods = false)
public class NextWeekFlowTestClockConfig {
    @Bean
    @Primary
    Clock nextWeekFlowTestClock() {
        return Clock.fixed(Instant.parse("2026-07-15T03:00:00Z"), ZoneId.of("Asia/Seoul"));
    }
}
