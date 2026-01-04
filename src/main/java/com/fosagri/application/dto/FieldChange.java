package com.fosagri.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldChange {
    private String fieldName;
    private String fieldLabel;  // Human-readable label in French
    private Object oldValue;
    private Object newValue;
    private boolean isDocument;

    public FieldChange(String fieldName, String fieldLabel, Object oldValue, Object newValue) {
        this.fieldName = fieldName;
        this.fieldLabel = fieldLabel;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.isDocument = false;
    }

    public boolean hasChanged() {
        if (oldValue == null && newValue == null) return false;
        if (oldValue == null || newValue == null) return true;
        return !oldValue.equals(newValue);
    }
}
