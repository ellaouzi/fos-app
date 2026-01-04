package com.fosagri.application.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;

import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;

/**
 * Secure File Upload Component with:
 * - Content-based file type validation (magic bytes)
 * - File size limits
 * - Spam/malware pattern detection
 * - Upload progress indicator
 * - Multiple file support with management
 */
public class SecureFileUploadComponent extends VerticalLayout {

    // File type magic bytes for content validation
    private static final Map<String, byte[]> FILE_SIGNATURES = new HashMap<>();
    static {
        // PDF: %PDF
        FILE_SIGNATURES.put("application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46});
        // JPEG: FFD8FF
        FILE_SIGNATURES.put("image/jpeg", new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF});
        // PNG: 89504E47
        FILE_SIGNATURES.put("image/png", new byte[]{(byte)0x89, 0x50, 0x4E, 0x47});
        // GIF: GIF87a or GIF89a
        FILE_SIGNATURES.put("image/gif", new byte[]{0x47, 0x49, 0x46, 0x38});
        // DOCX/XLSX/PPTX (ZIP-based): PK
        FILE_SIGNATURES.put("application/zip", new byte[]{0x50, 0x4B, 0x03, 0x04});
        // DOC: D0CF11E0
        FILE_SIGNATURES.put("application/msword", new byte[]{(byte)0xD0, (byte)0xCF, 0x11, (byte)0xE0});
    }

    // Dangerous patterns in files (basic spam/malware detection)
    private static final List<String> DANGEROUS_PATTERNS = Arrays.asList(
        "<script", "javascript:", "vbscript:", "onclick=", "onerror=",
        "eval(", "document.write", "window.location", ".exe", ".bat", ".cmd",
        "powershell", "cmd.exe", "base64_decode", "<?php"
    );

    private final List<Map<String, Object>> uploadedFiles = new ArrayList<>();
    private final Div filesDisplayContainer;
    private final ProgressBar progressBar;
    private final Span progressLabel;
    private final Upload upload;
    private final int maxFiles;
    private final long maxFileSize;
    private Consumer<List<Map<String, Object>>> onFilesChanged;

    public SecureFileUploadComponent() {
        this(5, 10 * 1024 * 1024); // Default: 5 files, 10MB each
    }

    public SecureFileUploadComponent(int maxFiles, long maxFileSize) {
        this.maxFiles = maxFiles;
        this.maxFileSize = maxFileSize;

        setPadding(false);
        setSpacing(true);
        setWidthFull();

        // Header
        H4 title = new H4("Pièces jointes");
        title.getStyle().set("margin", "0");
        add(title);

        // Info text
        Span info = new Span(String.format(
            "Formats acceptés: PDF, Word, Images (JPG, PNG) | Taille max: %s | Limite: %d fichier(s)",
            formatFileSize(maxFileSize), maxFiles));
        info.getStyle()
            .set("font-size", "12px")
            .set("color", "var(--lumo-secondary-text-color)");
        add(info);

        // Progress bar container
        Div progressContainer = new Div();
        progressContainer.setWidthFull();
        progressContainer.getStyle().set("margin-top", "8px");

        progressBar = new ProgressBar();
        progressBar.setWidthFull();
        progressBar.setVisible(false);

        progressLabel = new Span();
        progressLabel.getStyle()
            .set("font-size", "12px")
            .set("color", "var(--lumo-secondary-text-color)");
        progressLabel.setVisible(false);

        progressContainer.add(progressBar, progressLabel);
        add(progressContainer);

        // Files display
        filesDisplayContainer = new Div();
        filesDisplayContainer.setWidthFull();
        add(filesDisplayContainer);

        // Upload component
        MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
        upload = new Upload(buffer);
        configureUpload();

        // Upload event handlers
        upload.addStartedListener(event -> {
            progressBar.setVisible(true);
            progressBar.setIndeterminate(false);
            progressBar.setValue(0);
            progressLabel.setVisible(true);
            progressLabel.setText("Téléchargement de " + event.getFileName() + "...");
        });

        upload.addProgressListener(event -> {
            if (event.getContentLength() > 0) {
                double progress = (double) event.getReadBytes() / event.getContentLength();
                progressBar.setValue(progress);
                progressLabel.setText(String.format("Téléchargement: %s - %d%%",
                    event.getFileName(), (int)(progress * 100)));
            } else {
                progressBar.setIndeterminate(true);
            }
        });

        upload.addSucceededListener(event -> {
            progressBar.setValue(1.0);
            progressLabel.setText("Validation du fichier...");

            if (uploadedFiles.size() >= maxFiles) {
                showError("Nombre maximum de fichiers atteint (" + maxFiles + ")");
                hideProgress();
                return;
            }

            try {
                InputStream inputStream = buffer.getInputStream(event.getFileName());
                byte[] fileBytes = inputStream.readAllBytes();
                inputStream.close();

                // Validate file
                ValidationResult validation = validateFile(
                    event.getFileName(),
                    event.getMIMEType(),
                    fileBytes
                );

                if (!validation.isValid()) {
                    showError(validation.getMessage());
                    hideProgress();
                    return;
                }

                // File is valid - add to list
                Map<String, Object> fileInfo = new LinkedHashMap<>();
                fileInfo.put("filename", event.getFileName());
                fileInfo.put("contentType", event.getMIMEType());
                fileInfo.put("size", fileBytes.length);
                fileInfo.put("base64Content", Base64.getEncoder().encodeToString(fileBytes));
                fileInfo.put("uploadedAt", new Date().toString());

                uploadedFiles.add(fileInfo);
                updateFilesDisplay();
                notifyFilesChanged();

                progressLabel.setText("Fichier ajouté avec succès!");
                showSuccess(event.getFileName() + " a été ajouté");

                // Check if max files reached
                if (uploadedFiles.size() >= maxFiles) {
                    upload.setVisible(false);
                }

            } catch (Exception e) {
                showError("Erreur lors du traitement: " + e.getMessage());
            } finally {
                hideProgress();
            }
        });

        upload.addFailedListener(event -> {
            showError("Échec du téléchargement: " + event.getReason().getMessage());
            hideProgress();
        });

        upload.addFileRejectedListener(event -> {
            showError(event.getErrorMessage());
            hideProgress();
        });

        add(upload);
    }

    private void configureUpload() {
        upload.setAcceptedFileTypes(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png",
            "image/gif",
            ".pdf", ".doc", ".docx", ".jpg", ".jpeg", ".png", ".gif"
        );
        upload.setMaxFileSize((int) maxFileSize);
        upload.setMaxFiles(1); // Process one at a time for validation

        Button uploadButton = new Button("Ajouter un fichier");
        uploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        upload.setUploadButton(uploadButton);

        upload.setDropAllowed(true);
        upload.setDropLabel(new Span("Glisser-déposer ici"));
    }

    private ValidationResult validateFile(String filename, String mimeType, byte[] content) {
        // 1. Check file size
        if (content.length > maxFileSize) {
            return ValidationResult.invalid(
                "Fichier trop volumineux. Taille max: " + formatFileSize(maxFileSize));
        }

        // 2. Check file extension
        String extension = getFileExtension(filename).toLowerCase();
        if (!isAllowedExtension(extension)) {
            return ValidationResult.invalid(
                "Extension de fichier non autorisée: " + extension);
        }

        // 3. Validate content type matches magic bytes
        if (!validateMagicBytes(content, mimeType, extension)) {
            return ValidationResult.invalid(
                "Le contenu du fichier ne correspond pas à son extension. " +
                "Veuillez vérifier que le fichier n'est pas corrompu.");
        }

        // 4. Scan for dangerous patterns (basic malware/spam check)
        String contentSample = extractTextContent(content, 8192); // Check first 8KB
        for (String pattern : DANGEROUS_PATTERNS) {
            if (contentSample.toLowerCase().contains(pattern.toLowerCase())) {
                return ValidationResult.invalid(
                    "Le fichier contient du contenu non autorisé et a été rejeté pour des raisons de sécurité.");
            }
        }

        // 5. Check for suspicious file names
        if (isSuspiciousFilename(filename)) {
            return ValidationResult.invalid(
                "Nom de fichier suspect. Veuillez renommer le fichier.");
        }

        return ValidationResult.valid();
    }

    private boolean validateMagicBytes(byte[] content, String mimeType, String extension) {
        if (content.length < 4) {
            return false; // File too small
        }

        // Get expected signatures based on extension
        byte[] expectedSignature = null;

        switch (extension) {
            case "pdf":
                expectedSignature = FILE_SIGNATURES.get("application/pdf");
                break;
            case "jpg":
            case "jpeg":
                expectedSignature = FILE_SIGNATURES.get("image/jpeg");
                break;
            case "png":
                expectedSignature = FILE_SIGNATURES.get("image/png");
                break;
            case "gif":
                expectedSignature = FILE_SIGNATURES.get("image/gif");
                break;
            case "doc":
                expectedSignature = FILE_SIGNATURES.get("application/msword");
                break;
            case "docx":
            case "xlsx":
            case "pptx":
                expectedSignature = FILE_SIGNATURES.get("application/zip");
                break;
            default:
                return true; // Unknown extension, skip magic byte check
        }

        if (expectedSignature == null) {
            return true;
        }

        // Compare magic bytes
        for (int i = 0; i < expectedSignature.length && i < content.length; i++) {
            if (content[i] != expectedSignature[i]) {
                return false;
            }
        }

        return true;
    }

    private boolean isAllowedExtension(String extension) {
        return Arrays.asList("pdf", "doc", "docx", "jpg", "jpeg", "png", "gif")
            .contains(extension.toLowerCase());
    }

    private boolean isSuspiciousFilename(String filename) {
        String lowerName = filename.toLowerCase();
        // Check for double extensions (e.g., file.pdf.exe)
        if (lowerName.matches(".*\\.(exe|bat|cmd|ps1|vbs|js|jar|msi|com|scr)$")) {
            return true;
        }
        // Check for hidden files or null bytes
        if (lowerName.startsWith(".") || filename.contains("\0")) {
            return true;
        }
        // Check for very long filenames
        if (filename.length() > 255) {
            return true;
        }
        return false;
    }

    private String extractTextContent(byte[] content, int maxLength) {
        int length = Math.min(content.length, maxLength);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            char c = (char) (content[i] & 0xFF);
            if (c >= 32 && c < 127) { // Printable ASCII
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDot + 1);
    }

    private void updateFilesDisplay() {
        filesDisplayContainer.removeAll();

        if (uploadedFiles.isEmpty()) {
            Span noFiles = new Span("Aucun fichier ajouté");
            noFiles.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-style", "italic");
            filesDisplayContainer.add(noFiles);
            return;
        }

        for (int i = 0; i < uploadedFiles.size(); i++) {
            Map<String, Object> file = uploadedFiles.get(i);
            final int index = i;

            Div fileItem = new Div();
            fileItem.getStyle()
                .set("display", "flex")
                .set("align-items", "center")
                .set("justify-content", "space-between")
                .set("padding", "10px 12px")
                .set("margin-bottom", "6px")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius)")
                .set("border-left", "4px solid var(--lumo-primary-color)");

            // File info
            Div fileInfo = new Div();
            fileInfo.getStyle().set("display", "flex").set("align-items", "center").set("gap", "8px");

            // File icon
            Span icon = new Span();
            icon.add(getFileIcon(file.get("filename").toString()));
            fileInfo.add(icon);

            // File name and size
            Div nameAndSize = new Div();
            Span fileName = new Span(file.get("filename").toString());
            fileName.getStyle().set("font-weight", "500");

            long size = ((Number) file.get("size")).longValue();
            Span fileSize = new Span(" (" + formatFileSize(size) + ")");
            fileSize.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "12px");

            nameAndSize.add(fileName, fileSize);
            fileInfo.add(nameAndSize);

            // Remove button
            Button removeBtn = new Button(VaadinIcon.TRASH.create());
            removeBtn.addThemeVariants(
                ButtonVariant.LUMO_SMALL,
                ButtonVariant.LUMO_TERTIARY,
                ButtonVariant.LUMO_ERROR
            );
            removeBtn.getElement().setAttribute("title", "Supprimer");
            removeBtn.addClickListener(e -> {
                uploadedFiles.remove(index);
                updateFilesDisplay();
                notifyFilesChanged();
                if (uploadedFiles.size() < maxFiles) {
                    upload.setVisible(true);
                }
            });

            fileItem.add(fileInfo, removeBtn);
            filesDisplayContainer.add(fileItem);
        }
    }

    private com.vaadin.flow.component.icon.Icon getFileIcon(String filename) {
        String ext = getFileExtension(filename).toLowerCase();
        switch (ext) {
            case "pdf":
                return VaadinIcon.FILE_TEXT.create();
            case "doc":
            case "docx":
                return VaadinIcon.FILE_TEXT_O.create();
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                return VaadinIcon.FILE_PICTURE.create();
            default:
                return VaadinIcon.FILE_O.create();
        }
    }

    private void hideProgress() {
        getUI().ifPresent(ui -> ui.access(() -> {
            try {
                Thread.sleep(1500); // Show success message briefly
            } catch (InterruptedException ignored) {}
            progressBar.setVisible(false);
            progressLabel.setVisible(false);
        }));
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.MIDDLE);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void notifyFilesChanged() {
        if (onFilesChanged != null) {
            onFilesChanged.accept(new ArrayList<>(uploadedFiles));
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    // Public API

    public void setOnFilesChanged(Consumer<List<Map<String, Object>>> callback) {
        this.onFilesChanged = callback;
    }

    public List<Map<String, Object>> getUploadedFiles() {
        return new ArrayList<>(uploadedFiles);
    }

    public String getFilesAsJson() {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.writeValueAsString(uploadedFiles);
        } catch (Exception e) {
            return "[]";
        }
    }

    public boolean hasFiles() {
        return !uploadedFiles.isEmpty();
    }

    public void clear() {
        uploadedFiles.clear();
        updateFilesDisplay();
        upload.setVisible(true);
    }

    // Validation result helper class
    private static class ValidationResult {
        private final boolean valid;
        private final String message;

        private ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        static ValidationResult invalid(String message) {
            return new ValidationResult(false, message);
        }

        boolean isValid() {
            return valid;
        }

        String getMessage() {
            return message;
        }
    }
}
