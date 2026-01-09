package com.backend.backend_boilerplate.domain.file;

import com.backend.backend_boilerplate.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "upload_files", indexes = {
        @Index(name = "idx_upload_files_owner", columnList = "ownerEmail")
})
public class UploadFile extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false, unique = true, length = 64)
    private String storedFilename; // UUID 기반

    @Column(nullable = false)
    private String contentType;

    private long size;

    @Column(nullable = false)
    private String path; // 저장 경로(상대경로 추천)

    // 업로드한 사용자 식별(간단히 email)
    @Column(nullable = false)
    private String ownerEmail;

    public UploadFile(String originalFilename, String storedFilename, String contentType,
                      long size, String path, String ownerEmail) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.contentType = contentType;
        this.size = size;
        this.path = path;
        this.ownerEmail = ownerEmail;
    }
}
