package com.backend.backend_boilerplate.domain.file;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UploadFileRepository extends JpaRepository<UploadFile, Long> {
    Optional<UploadFile> findByStoredFilename(String storedFilename);
}
