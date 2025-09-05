package com.fosagri.application.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

public class FileUploadComponent extends VerticalLayout {
    
    private final String label;
    private final String acceptedTypes;
    private final int maxFileSize; // in MB
    private final boolean showPreview;
    private final boolean isAvatar;
    
    private MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private Upload upload;
    private Image preview;
    private Div fileInfo;
    private Button removeButton;
    
    private byte[] currentFileData;
    private String currentFileName;
    private String currentContentType;
    
    private Consumer<FileUploadData> onFileUploaded;
    private Runnable onFileRemoved;
    
    public FileUploadComponent(String label, String acceptedTypes, int maxFileSizeMB, boolean showPreview) {
        this(label, acceptedTypes, maxFileSizeMB, showPreview, false);
    }
    
    public FileUploadComponent(String label, String acceptedTypes, int maxFileSizeMB, boolean showPreview, boolean isAvatar) {
        this.label = label;
        this.acceptedTypes = acceptedTypes;
        this.maxFileSize = maxFileSizeMB;
        this.showPreview = showPreview;
        this.isAvatar = isAvatar;
        
        initComponent();
    }
    
    private void initComponent() {
        setSpacing(true);
        setPadding(false);
        
        upload = new Upload(buffer);
        upload.setAcceptedFileTypes(acceptedTypes.split(","));
        upload.setMaxFileSize(maxFileSize * 1024 * 1024); // Convert MB to bytes
        upload.setMaxFiles(1);
        upload.setDropAllowed(true);
        
        // Create upload area with title
        H4 titleLabel = new H4(label);
        titleLabel.getStyle().set("margin", "0 0 8px 0");
        titleLabel.getStyle().set("font-size", "14px");
        titleLabel.getStyle().set("font-weight", "bold");
        titleLabel.getStyle().set("color", "#333");
        
        Div uploadArea = new Div();
        upload.setUploadButton(new Button("Choisir fichier", VaadinIcon.UPLOAD.create()));
        
        upload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            String contentType = event.getMIMEType();
            
            try {
                InputStream inputStream = buffer.getInputStream(fileName);
                currentFileData = inputStream.readAllBytes();
                currentFileName = fileName;
                currentContentType = contentType;
                
                showFileInfo();
                if (showPreview && isImage(contentType)) {
                    showImagePreview();
                }
                
                if (onFileUploaded != null) {
                    onFileUploaded.accept(new FileUploadData(currentFileData, currentFileName, currentContentType));
                }
                
                showSuccessNotification("Fichier uploadé avec succès: " + fileName);
                
            } catch (IOException e) {
                showErrorNotification("Erreur lors du téléchargement: " + e.getMessage());
            }
        });
        
        upload.addFileRejectedListener(event -> {
            showErrorNotification("Fichier rejeté: " + event.getErrorMessage());
        });
        
        upload.addFailedListener(event -> {
            showErrorNotification("Échec du téléchargement: " + event.getReason().getMessage());
        });
        
        fileInfo = new Div();
        fileInfo.setVisible(false);
        
        preview = new Image();
        preview.setVisible(false);
        
        if (isAvatar) {
            // Avatar style - circular
            preview.setWidth("80px");
            preview.setHeight("80px");
            preview.getStyle().set("border-radius", "50%");
            preview.getStyle().set("object-fit", "cover");
            preview.getStyle().set("border", "2px solid #e0e0e0");
            preview.getStyle().set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        } else {
            // Document preview style - rectangular
            preview.setMaxWidth("200px");
            preview.setMaxHeight("150px");
            preview.getStyle().set("border", "1px solid #ccc");
            preview.getStyle().set("border-radius", "4px");
        }
        
        removeButton = new Button("Supprimer", VaadinIcon.TRASH.create());
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.setVisible(false);
        removeButton.addClickListener(e -> removeFile());
        
        add(titleLabel, upload, fileInfo, preview, removeButton);
    }
    
    private void showFileInfo() {
        fileInfo.removeAll();
        
        if (isAvatar) {
            // For avatars, show a more subtle file info
            Span fileNameSpan = new Span(currentFileName);
            fileNameSpan.getStyle().set("font-size", "12px");
            fileNameSpan.getStyle().set("color", "#666");
            fileInfo.add(fileNameSpan);
        } else {
            // For documents, show file icon and name
            fileInfo.add(new Span("📎 " + currentFileName));
        }
        
        fileInfo.setVisible(true);
        removeButton.setVisible(true);
    }
    
    private void showImagePreview() {
        if (currentFileData != null) {
            StreamResource streamResource = new StreamResource(currentFileName, 
                () -> new ByteArrayInputStream(currentFileData));
            preview.setSrc(streamResource);
            preview.setAlt(isAvatar ? "Photo de profil" : "Aperçu du document");
            preview.setVisible(true);
        }
    }
    
    private void removeFile() {
        currentFileData = null;
        currentFileName = null;
        currentContentType = null;
        
        fileInfo.setVisible(false);
        preview.setVisible(false);
        removeButton.setVisible(false);
        
        if (onFileRemoved != null) {
            onFileRemoved.run();
        }
        
        // Clear the upload component
        upload.clearFileList();
    }
    
    private boolean isImage(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
    
    public void setExistingFile(byte[] fileData, String fileName, String contentType) {
        this.currentFileData = fileData;
        this.currentFileName = fileName;
        this.currentContentType = contentType;
        
        if (fileData != null && fileName != null) {
            showFileInfo();
            if (showPreview && isImage(contentType)) {
                showImagePreview();
            }
        }
    }
    
    public FileUploadData getCurrentFile() {
        if (currentFileData != null) {
            return new FileUploadData(currentFileData, currentFileName, currentContentType);
        }
        return null;
    }
    
    public void setOnFileUploaded(Consumer<FileUploadData> callback) {
        this.onFileUploaded = callback;
    }
    
    public void setOnFileRemoved(Runnable callback) {
        this.onFileRemoved = callback;
    }
    
    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
    
    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
    
    public static class FileUploadData {
        private final byte[] data;
        private final String fileName;
        private final String contentType;
        
        public FileUploadData(byte[] data, String fileName, String contentType) {
            this.data = data;
            this.fileName = fileName;
            this.contentType = contentType;
        }
        
        public byte[] getData() { return data; }
        public String getFileName() { return fileName; }
        public String getContentType() { return contentType; }
    }
}