package com.backend.backend_boilerplate.domain.file;

import com.backend.backend_boilerplate.domain.file.storage.FileStorage;
import com.backend.backend_boilerplate.domain.file.storage.StoredFile;
import com.backend.backend_boilerplate.global.exception.BusinessException;
import com.backend.backend_boilerplate.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileService {

    private final FileStorage storage;
    private final UploadFileRepository fileRepository;

    @Transactional
    public UploadFile upload(MultipartFile file) {
        String owner = currentEmail();

        StoredFile stored = storage.save(file);

        UploadFile entity = new UploadFile(
                stored.getOriginalFilename(),
                stored.getStoredFilename(),
                stored.getContentType(),
                stored.getSize(),
                stored.getRelativePath(),
                owner
        );

        return fileRepository.save(entity);
    }

    public Resource thumbnail(String storedFilename, int w, int h) {
        UploadFile meta = fileRepository.findByStoredFilename(storedFilename)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

        if (!meta.getOwnerEmail().equals(currentEmail())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        // LocalFileStorage로 캐스팅(보일러플레이트라 단순화)
        if (!(storage instanceof com.backend.backend_boilerplate.domain.file.storage.LocalFileStorage local)) {
            throw new IllegalStateException("thumbnail은 LocalFileStorage에서만 지원됩니다.");
        }

        String thumbName = local.saveThumbnail(meta.getStoredFilename(), w, h);
        return new org.springframework.core.io.FileSystemResource(local.loadAsPath(thumbName));
    }

    public Resource download(String storedFilename) {
        UploadFile meta = fileRepository.findByStoredFilename(storedFilename)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "파일을 찾을 수 없습니다."));

        // (옵션) 본인만 다운로드 허용
        if (!meta.getOwnerEmail().equals(currentEmail())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "권한이 없습니다.");
        }

        Path path = storage.loadAsPath(meta.getStoredFilename());
        return new FileSystemResource(path);
    }

    private String currentEmail() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) throw new BusinessException(ErrorCode.INVALID_REQUEST, "인증이 필요합니다.");
        return auth.getName();
    }
}
