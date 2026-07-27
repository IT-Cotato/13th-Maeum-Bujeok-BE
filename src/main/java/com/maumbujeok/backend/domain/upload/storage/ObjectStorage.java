package com.maumbujeok.backend.domain.upload.storage;

import java.net.URI;
import java.time.Duration;

public interface ObjectStorage {
    PresignedObjectUrl prepareUpload(String objectKey, String contentType, long fileSize, Duration validity);
    StoredObject head(String objectKey);
    URI createDownloadUrl(String objectKey, Duration validity);
    void delete(String objectKey);
}
