package com.maumbujeok.backend.domain.upload.storage;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3ObjectStorage implements ObjectStorage {
    private final S3Client client;
    private final S3Presigner presigner;
    private final StorageProperties properties;

    public S3ObjectStorage(S3Client client, S3Presigner presigner, StorageProperties properties) {
        this.client = client;
        this.presigner = presigner;
        this.properties = properties;
    }

    @Override
    public PresignedObjectUrl prepareUpload(String key, String type, long size, Duration validity) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.getBucket()).key(key).contentType(type).contentLength(size).build();
        URI url = URI.create(presigner.presignPutObject(PutObjectPresignRequest.builder()
                        .signatureDuration(validity).putObjectRequest(request).build())
                .url().toString());
        return new PresignedObjectUrl(url, Instant.now().plus(validity));
    }

    @Override
    public StoredObject head(String key) {
        try {
            HeadObjectResponse response = client.headObject(HeadObjectRequest.builder()
                    .bucket(properties.getBucket()).key(key).build());
            return new StoredObject(response.contentType(), response.contentLength());
        } catch (S3Exception exception) {
            if (exception.statusCode() == 404) {
                throw new ObjectNotUploadedException(key, exception);
            }
            throw new ObjectStorageException("S3 object metadata lookup failed", exception);
        }
    }

    @Override
    public URI createDownloadUrl(String key, Duration validity) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(properties.getBucket()).key(key).build();
        return URI.create(presigner.presignGetObject(GetObjectPresignRequest.builder()
                        .signatureDuration(validity).getObjectRequest(request).build())
                .url().toString());
    }

    @Override
    public void delete(String key) {
        client.deleteObject(DeleteObjectRequest.builder()
                .bucket(properties.getBucket()).key(key).build());
    }
}
