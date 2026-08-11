package com.maumbujeok.backend.domain.diary.ai;

import java.util.concurrent.Executor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableConfigurationProperties(AiAnalysisProperties.class)
public class AiAnalysisConfig {

    @Bean(name = "diaryAnalysisExecutor")
    public Executor diaryAnalysisExecutor() {
        return executor("diary-analysis-", 2, 20);
    }

    @Bean(name = "weeklyReportExecutor")
    public Executor weeklyReportExecutor() {
        return executor("weekly-report-", 2, 20);
    }

    @Bean(name = "sajuAnalysisExecutor")
    public Executor sajuAnalysisExecutor() {
        return executor("saju-analysis-", 2, 20);
    }

    private ThreadPoolTaskExecutor executor(String prefix, int poolSize, int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(prefix);
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(5);
        executor.initialize();
        return executor;
    }
}
