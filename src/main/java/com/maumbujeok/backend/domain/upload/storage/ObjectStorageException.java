package com.maumbujeok.backend.domain.upload.storage;

public class ObjectStorageException extends RuntimeException {
    public ObjectStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
