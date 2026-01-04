package com.fosagri.application.controllers;

import com.fosagri.application.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Controller for serving uploaded files.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Download a file by its stored path.
     * The path should be URL-encoded.
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFile(@RequestParam String path) {
        try {
            // Security check: ensure path doesn't escape the uploads directory
            Path filePath = Paths.get(path).normalize();
            if (!filePath.toString().startsWith("uploads")) {
                return ResponseEntity.badRequest().build();
            }

            byte[] content = fileStorageService.readFile(path);
            Map<String, Object> fileInfo = fileStorageService.getFileInfo(path);

            String filename = (String) fileInfo.get("filename");
            String contentType = (String) fileInfo.get("contentType");

            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            ByteArrayResource resource = new ByteArrayResource(content);

            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replace("+", "%20");

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + filename + "\"; filename*=UTF-8''" + encodedFilename)
                .contentLength(content.length)
                .body(resource);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * View a file inline (for images and PDFs).
     */
    @GetMapping("/view")
    public ResponseEntity<Resource> viewFile(@RequestParam String path) {
        try {
            // Security check
            Path filePath = Paths.get(path).normalize();
            if (!filePath.toString().startsWith("uploads")) {
                return ResponseEntity.badRequest().build();
            }

            byte[] content = fileStorageService.readFile(path);
            Map<String, Object> fileInfo = fileStorageService.getFileInfo(path);

            String contentType = (String) fileInfo.get("contentType");
            if (contentType == null) {
                contentType = Files.probeContentType(filePath);
            }
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            ByteArrayResource resource = new ByteArrayResource(content);

            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentLength(content.length)
                .body(resource);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get file info without downloading.
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getFileInfo(@RequestParam String path) {
        try {
            // Security check
            Path filePath = Paths.get(path).normalize();
            if (!filePath.toString().startsWith("uploads")) {
                return ResponseEntity.badRequest().build();
            }

            Map<String, Object> info = fileStorageService.getFileInfo(path);
            return ResponseEntity.ok(info);

        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
