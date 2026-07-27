package com.maumbujeok.backend.domain.upload.storage;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String bucket = "";
    private String localDirectory = "build/local-uploads";
    private Duration uploadUrlValidity = Duration.ofMinutes(10);
    private Duration downloadUrlValidity = Duration.ofMinutes(10);
    private Duration orphanRetention = Duration.ofHours(24);

    public String getBucket() { return bucket; }
    public void setBucket(String value) { this.bucket = value; }
    public String getLocalDirectory() { return localDirectory; }
    public void setLocalDirectory(String value) { this.localDirectory = value; }
    public Duration getUploadUrlValidity() { return uploadUrlValidity; }
    public void setUploadUrlValidity(Duration value) { this.uploadUrlValidity = value; }
    public Duration getDownloadUrlValidity() { return downloadUrlValidity; }
    public void setDownloadUrlValidity(Duration value) { this.downloadUrlValidity = value; }
    public Duration getOrphanRetention() { return orphanRetention; }
    public void setOrphanRetention(Duration value) { this.orphanRetention = value; }
}