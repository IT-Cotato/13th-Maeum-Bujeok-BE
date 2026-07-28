package com.maumbujeok.backend.domain.upload.storage;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalObjectStorage implements ObjectStorage {
    private final Path root;
    private final Map<String, StoredObject> objects = new ConcurrentHashMap<>();
    private final Map<UUID, UploadGrant> uploadGrants = new ConcurrentHashMap<>();
    private final Map<UUID, DownloadGrant> downloadGrants = new ConcurrentHashMap<>();

    public LocalObjectStorage(StorageProperties properties) {
        this.root = Path.of(properties.getLocalDirectory()).toAbsolutePath().normalize();
    }

    @Override
    public PresignedObjectUrl prepareUpload(String key, String type, long size, Duration validity) {
        UUID token = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(validity);
        uploadGrants.put(token, new UploadGrant(key, type, size, expiresAt));
        return new PresignedObjectUrl(
                URI.create("http://localhost:8080/api/local-uploads/" + token), expiresAt);
    }

    public void upload(UUID token, String contentType, byte[] content) {
        UploadGrant grant = uploadGrants.remove(token);
        if (grant == null || grant.expiresAt().isBefore(Instant.now())
                || !grant.contentType().equals(contentType) || grant.fileSize() != content.length) {
            throw new IllegalArgumentException("Invalid or expired local upload grant");
        }
        try {
            Files.createDirectories(root);
            Files.write(pathFor(grant.objectKey()), content);
            objects.put(grant.objectKey(), new StoredObject(contentType, content.length));
        } catch (IOException exception) {
            throw new ObjectStorageException("Local object write failed", exception);
        }
    }

    public LocalDownload download(UUID token) {
        DownloadGrant grant = downloadGrants.remove(token);
        if (grant == null || grant.expiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Invalid or expired local download grant");
        }
        StoredObject metadata = head(grant.objectKey());
        try {
            return new LocalDownload(Files.readAllBytes(pathFor(grant.objectKey())), metadata.contentType());
        } catch (IOException exception) {
            throw new ObjectStorageException("Local object read failed", exception);
        }
    }

    @Override
    public StoredObject head(String key) {
        StoredObject object = objects.get(key);
        if (object == null || !Files.exists(pathFor(key))) {
            throw new ObjectNotUploadedException(key);
        }
        return object;
    }

    @Override
    public URI createDownloadUrl(String key, Duration validity) {
        head(key);
        UUID token = UUID.randomUUID();
        downloadGrants.put(token, new DownloadGrant(key, Instant.now().plus(validity)));
        return URI.create("http://localhost:8080/api/local-uploads/content/" + token);
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(pathFor(key));
            objects.remove(key);
        } catch (IOException exception) {
            throw new ObjectStorageException("Local object delete failed", exception);
        }
    }

    private Path pathFor(String objectKey) {
        return root.resolve(objectKey.replace('/', '_')).normalize();
    }

    private record UploadGrant(String objectKey, String contentType, long fileSize, Instant expiresAt) {}
    private record DownloadGrant(String objectKey, Instant expiresAt) {}
    public record LocalDownload(byte[] content, String contentType) {}
}