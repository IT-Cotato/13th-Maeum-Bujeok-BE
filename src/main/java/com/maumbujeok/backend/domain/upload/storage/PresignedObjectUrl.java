package com.maumbujeok.backend.domain.upload.storage;

import java.net.URI;
import java.time.Instant;

public record PresignedObjectUrl(URI url, Instant expiresAt) {}
