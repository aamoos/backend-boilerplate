package com.backend.backend_boilerplate.domain.file;

import com.backend.backend_boilerplate.global.api.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UploadResponse> upload(@RequestPart("file") MultipartFile file) {
        UploadFile saved = fileService.upload(file);
        return ApiResponse.ok(new UploadResponse(saved.getStoredFilename(), saved.getOriginalFilename()));
    }

    @GetMapping("/{storedFilename}")
    public ResponseEntity<Resource> download(@PathVariable String storedFilename) {
        Resource resource = fileService.download(storedFilename);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + storedFilename + "\"")
                .body(resource);
    }

    @GetMapping("/thumb/{storedFilename}")
    public ResponseEntity<Resource> thumbnail(
            @PathVariable String storedFilename,
            @RequestParam(defaultValue = "300") int w,
            @RequestParam(defaultValue = "300") int h
    ) {
        // 간단 방어 (너무 큰 썸네일 요청 방지)
        w = Math.min(Math.max(w, 50), 1000);
        h = Math.min(Math.max(h, 50), 1000);

        Resource resource = fileService.thumbnail(storedFilename, w, h);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"thumb_" + storedFilename + "\"")
                .contentType(MediaType.IMAGE_JPEG) // 보통 썸네일은 jpg로 통일(원하면 타입 유지)
                .body(resource);
    }

    @Getter
    @AllArgsConstructor
    public static class UploadResponse {
        private String storedFilename;
        private String originalFilename;
    }
}
