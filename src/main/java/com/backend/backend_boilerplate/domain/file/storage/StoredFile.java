package com.backend.backend_boilerplate.domain.file.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredFile {
    private final String originalFilename;
    private final String storedFilename;
    private final String contentType;
    private final long size;
    private final String relativePath;
}
