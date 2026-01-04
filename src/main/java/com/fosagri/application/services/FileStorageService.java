package com.fosagri.application.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for secure file storage on disk.
 * Files are organized by date and demande ID for easy management.
 */
@Service
public class FileStorageService {

    @Value("${app.file-storage.base-path:uploads}")
    private String basePath;

    @Value("${app.file-storage.max-file-size:10485760}")
    private long maxFileSize; // 10MB default

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "pdf", "doc", "docx", "jpg", "jpeg", "png", "gif"
    );

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "image/jpeg",
        "image/png",
        "image/gif"
    );

    @PostConstruct
    public void init() {
        try {
            Path uploadPath = Paths.get(basePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("Created upload directory: " + uploadPath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + basePath, e);
        }
    }

    /**
     * Stores a file for a demande and returns the file info with path.
     *
     * @param demandeId The demande ID (can be null for new demandes)
     * @param agentId The agent ID
     * @param filename Original filename
     * @param contentType MIME type
     * @param content File content as bytes
     * @return Map containing file info including stored path
     */
    public Map<String, Object> storeFile(Long demandeId, Integer agentId, String filename,
                                         String contentType, byte[] content) throws IOException {
        // Validate file
        validateFile(filename, contentType, content);

        // Generate storage path: uploads/YYYY/MM/agent_{id}/demande_{id}/
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String agentFolder = "agent_" + (agentId != null ? agentId : "unknown");
        String demandeFolder = demandeId != null ? "demande_" + demandeId : "pending_" + UUID.randomUUID().toString().substring(0, 8);

        Path storagePath = Paths.get(basePath, datePath, agentFolder, demandeFolder);
        Files.createDirectories(storagePath);

        // Generate safe filename
        String safeFilename = generateSafeFilename(filename);
        Path filePath = storagePath.resolve(safeFilename);

        // Ensure no overwrite - add suffix if needed
        int counter = 1;
        while (Files.exists(filePath)) {
            String nameWithoutExt = safeFilename.substring(0, safeFilename.lastIndexOf('.'));
            String ext = safeFilename.substring(safeFilename.lastIndexOf('.'));
            safeFilename = nameWithoutExt + "_" + counter + ext;
            filePath = storagePath.resolve(safeFilename);
            counter++;
        }

        // Write file
        Files.write(filePath, content);

        // Create file info
        Map<String, Object> fileInfo = new LinkedHashMap<>();
        fileInfo.put("originalFilename", filename);
        fileInfo.put("storedFilename", safeFilename);
        fileInfo.put("contentType", contentType);
        fileInfo.put("size", content.length);
        fileInfo.put("storedPath", filePath.toString());
        fileInfo.put("relativePath", storagePath.relativize(Paths.get(basePath).resolve(filePath.toString().replace(basePath + "/", ""))).toString());
        fileInfo.put("uploadedAt", new Date().toString());

        System.out.println("File stored: " + filePath.toAbsolutePath());
        return fileInfo;
    }

    /**
     * Stores multiple files from JSON format (from SecureFileUploadComponent)
     */
    public String storeFilesFromJson(String documentsJson, Long demandeId, Integer agentId) {
        if (documentsJson == null || documentsJson.trim().isEmpty()) {
            return null;
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> files = mapper.readValue(documentsJson,
                new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {});

            List<Map<String, Object>> storedFiles = new ArrayList<>();

            for (Map<String, Object> file : files) {
                String filename = (String) file.get("filename");
                String contentType = (String) file.get("contentType");
                String base64Content = (String) file.get("base64Content");

                if (base64Content != null && !base64Content.isEmpty()) {
                    byte[] content = Base64.getDecoder().decode(base64Content);

                    Map<String, Object> storedFile = storeFile(demandeId, agentId, filename, contentType, content);
                    storedFiles.add(storedFile);
                }
            }

            return mapper.writeValueAsString(storedFiles);

        } catch (Exception e) {
            System.err.println("Error storing files: " + e.getMessage());
            e.printStackTrace();
            return documentsJson; // Return original if storage fails
        }
    }

    /**
     * Reads a stored file
     */
    public byte[] readFile(String storedPath) throws IOException {
        Path path = Paths.get(storedPath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + storedPath);
        }
        return Files.readAllBytes(path);
    }

    /**
     * Deletes a stored file
     */
    public boolean deleteFile(String storedPath) {
        try {
            Path path = Paths.get(storedPath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            System.err.println("Error deleting file: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets file info from stored path
     */
    public Map<String, Object> getFileInfo(String storedPath) throws IOException {
        Path path = Paths.get(storedPath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + storedPath);
        }

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("storedPath", storedPath);
        info.put("filename", path.getFileName().toString());
        info.put("size", Files.size(path));
        info.put("contentType", Files.probeContentType(path));
        info.put("lastModified", Files.getLastModifiedTime(path).toString());

        return info;
    }

    private void validateFile(String filename, String contentType, byte[] content) {
        // Check size
        if (content.length > maxFileSize) {
            throw new IllegalArgumentException("File too large. Maximum size: " + formatFileSize(maxFileSize));
        }

        // Check extension
        String extension = getFileExtension(filename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("File type not allowed: " + extension);
        }

        // Check content type
        if (contentType != null && !isContentTypeAllowed(contentType)) {
            throw new IllegalArgumentException("Content type not allowed: " + contentType);
        }

        // Validate magic bytes
        if (!validateMagicBytes(content, extension)) {
            throw new IllegalArgumentException("File content does not match its extension");
        }
    }

    private boolean isContentTypeAllowed(String contentType) {
        return ALLOWED_CONTENT_TYPES.stream()
            .anyMatch(allowed -> contentType.toLowerCase().contains(allowed.toLowerCase()));
    }

    private boolean validateMagicBytes(byte[] content, String extension) {
        if (content.length < 4) return false;

        switch (extension) {
            case "pdf":
                // PDF: %PDF
                return content[0] == 0x25 && content[1] == 0x50 && content[2] == 0x44 && content[3] == 0x46;
            case "jpg":
            case "jpeg":
                // JPEG: FF D8 FF
                return (content[0] & 0xFF) == 0xFF && (content[1] & 0xFF) == 0xD8 && (content[2] & 0xFF) == 0xFF;
            case "png":
                // PNG: 89 50 4E 47
                return (content[0] & 0xFF) == 0x89 && content[1] == 0x50 && content[2] == 0x4E && content[3] == 0x47;
            case "gif":
                // GIF: GIF8
                return content[0] == 0x47 && content[1] == 0x49 && content[2] == 0x46 && content[3] == 0x38;
            case "doc":
                // DOC: D0 CF 11 E0
                return (content[0] & 0xFF) == 0xD0 && (content[1] & 0xFF) == 0xCF &&
                       content[2] == 0x11 && (content[3] & 0xFF) == 0xE0;
            case "docx":
                // DOCX (ZIP): PK
                return content[0] == 0x50 && content[1] == 0x4B && content[2] == 0x03 && content[3] == 0x04;
            default:
                return true; // Allow unknown types
        }
    }

    private String generateSafeFilename(String originalFilename) {
        // Remove path components
        String filename = originalFilename;
        if (filename.contains("/")) {
            filename = filename.substring(filename.lastIndexOf('/') + 1);
        }
        if (filename.contains("\\")) {
            filename = filename.substring(filename.lastIndexOf('\\') + 1);
        }

        // Get extension
        String extension = "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            extension = filename.substring(lastDot);
            filename = filename.substring(0, lastDot);
        }

        // Sanitize filename (remove special characters)
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");

        // Limit length
        if (filename.length() > 100) {
            filename = filename.substring(0, 100);
        }

        // Add timestamp for uniqueness
        String timestamp = String.valueOf(System.currentTimeMillis());
        return filename + "_" + timestamp.substring(timestamp.length() - 6) + extension;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
