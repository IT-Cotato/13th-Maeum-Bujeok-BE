package com.maumbujeok.backend.domain.upload.storage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class S3StorageConfig {
    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
    S3Client s3Client() { return S3Client.create(); }

    @Bean
    @ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
    S3Presigner s3Presigner() { return S3Presigner.create(); }
}
