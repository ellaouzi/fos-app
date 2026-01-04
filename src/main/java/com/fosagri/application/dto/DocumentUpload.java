package com.fosagri.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUpload {
    private String fieldName;      // Field name in the entity (e.g., "agent_photo", "cin_image")
    private String filename;
    private String contentType;
    private byte[] data;
    private long size;

    public DocumentUpload(String fieldName, String filename, String contentType, byte[] data) {
        this.fieldName = fieldName;
        this.filename = filename;
        this.contentType = contentType;
        this.data = data;
        this.size = data != null ? data.length : 0;
    }

    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isPdf() {
        return contentType != null && contentType.equals("application/pdf");
    }
}
