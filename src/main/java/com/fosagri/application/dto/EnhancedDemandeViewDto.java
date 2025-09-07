package com.fosagri.application.dto;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.entities.PrestationRef;

import java.util.Date;
import java.util.Map;

public class EnhancedDemandeViewDto extends DemandeViewDto {
    private Map<String, Object> jsonFields;

    public EnhancedDemandeViewDto() {
        super();
    }

    public EnhancedDemandeViewDto(Long id, String statut, Date dateDemande, Date dateTraitement, 
                         String commentaire, AdhAgent agent, PrestationRef prestation,
                         Map<String, Object> jsonFields) {
        super(id, statut, dateDemande, dateTraitement, commentaire, agent, prestation);
        this.jsonFields = jsonFields;
    }

    public Map<String, Object> getJsonFields() {
        return jsonFields;
    }

    public void setJsonFields(Map<String, Object> jsonFields) {
        this.jsonFields = jsonFields;
    }
    
    public Object getJsonField(String key) {
        return jsonFields != null ? jsonFields.get(key) : null;
    }
    
    public String getJsonFieldAsString(String key) {
        Object value = getJsonField(key);
        return value != null ? value.toString() : "";
    }
}