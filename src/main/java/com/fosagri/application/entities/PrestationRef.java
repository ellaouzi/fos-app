package com.fosagri.application.entities;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "prestation_ref")
public class PrestationRef {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String label;
    private String type;
    private String valeurs;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String is_adh;
    private boolean open;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Temporal(TemporalType.DATE)
    private Date dateDu;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Temporal(TemporalType.DATE)
    private Date dateAu;
    
    private String statut;
    private int nombreLimit;
    private boolean isarabic;
    private boolean isattached;
    
    // Form schema JSON - stores the dynamic form structure
    @Column(columnDefinition = "TEXT")
    private String formSchemaJson;
    
    //------------------------- Secure Logs---
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;
    
    private Long createdBy;
    private Long updatedBy;
    //------------------------- Secure Logs---
    
    @PrePersist
    protected void onCreate() {
        created = new Date();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }
}