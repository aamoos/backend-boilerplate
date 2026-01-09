package com.backend.backend_boilerplate.domain.file.storage;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorage {
    StoredFile save(MultipartFile file);
    Path loadAsPath(String storedFilename);
}
