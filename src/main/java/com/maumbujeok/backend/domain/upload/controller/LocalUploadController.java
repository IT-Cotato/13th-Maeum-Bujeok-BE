package com.maumbujeok.backend.domain.upload.controller;

import com.maumbujeok.backend.domain.upload.storage.LocalObjectStorage;
import io.swagger.v3.oas.annotations.Hidden;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequestMapping("/api/local-uploads")
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalUploadController {
    private final LocalObjectStorage storage;

    public LocalUploadController(LocalObjectStorage storage) {
        this.storage = storage;
    }

    @PutMapping("/{token}")
    public ResponseEntity<Void> upload(
            @PathVariable UUID token,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody byte[] content
    ) {
        MediaType mediaType = MediaType.parseMediaType(contentType);
        storage.upload(token, mediaType.getType() + "/" + mediaType.getSubtype(), content);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/content/{token}")
    public ResponseEntity<byte[]> download(@PathVariable UUID token) {
        LocalObjectStorage.LocalDownload download = storage.download(token);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.contentType()))
                .body(download.content());
    }
}
