package com.backend.backend_boilerplate.domain.file.storage;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;
import java.io.InputStream;
import java.nio.file.Files;

@Component
public class LocalFileStorage implements FileStorage {

    private final Path rootDir;

    // 기본 화이트리스트(원하면 늘려)
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "application/pdf"
    );

    public LocalFileStorage(@Value("${app.upload.dir}") String dir) {
        this.rootDir = Paths.get(dir).toAbsolutePath().normalize();
    }

    @Override
    public StoredFile save(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("파일이 비어있습니다.");

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다: " + contentType);
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());

        // UUID로 저장 파일명 생성(확장자는 원본에서 추출)
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot > -1 && dot < original.length() - 1) ext = original.substring(dot);

        String stored = UUID.randomUUID().toString().replace("-", "") + ext;

        try {
            Files.createDirectories(rootDir);
            Path target = rootDir.resolve(stored).normalize();

            // 경로 탐색 방지
            if (!target.startsWith(rootDir)) {
                throw new IllegalArgumentException("잘못된 파일 경로입니다.");
            }

            // 덮어쓰기 방지
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFile(original, stored, contentType, file.getSize(), stored);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @Override
    public Path loadAsPath(String storedFilename) {
        Path p = rootDir.resolve(storedFilename).normalize();
        if (!p.startsWith(rootDir)) throw new IllegalArgumentException("잘못된 파일 경로입니다.");
        return p;
    }

    public String saveThumbnail(String storedFilename, int width, int height) {
        try {
            Path original = loadAsPath(storedFilename);

            String thumbName = "thumb_" + width + "x" + height + "_" + storedFilename;
            Path thumb = rootDir.resolve(thumbName).normalize();
            if (!thumb.startsWith(rootDir)) throw new IllegalArgumentException("잘못된 파일 경로입니다.");

            // 이미 만들어져 있으면 재생성 안 함
            if (Files.exists(thumb)) return thumbName;

            try (InputStream in = Files.newInputStream(original)) {
                Thumbnails.of(in)
                        .size(width, height)
                        .crop(net.coobird.thumbnailator.geometry.Positions.CENTER) // 중앙 크롭(정사각형 썸네일)
                        .outputQuality(0.85)
                        .toFile(thumb.toFile());
            }

            return thumbName;
        } catch (Exception e) {
            throw new RuntimeException("썸네일 생성 실패", e);
        }
    }
}
