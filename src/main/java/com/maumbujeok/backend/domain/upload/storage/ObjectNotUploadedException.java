package com.maumbujeok.backend.domain.upload.storage;

public class ObjectNotUploadedException extends RuntimeException {
    public ObjectNotUploadedException(String objectKey) {
        super("Object not uploaded: " + objectKey);
    }

    public ObjectNotUploadedException(String objectKey, Throwable cause) {
        super("Object not uploaded: " + objectKey, cause);
    }
}
