package com.fosagri.application.entities;

import com.fosagri.application.model.AdhAgent;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "demande_prestation")
public class DemandePrestation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "prestation_id")
    private PrestationRef prestation;
    
    @ManyToOne
    @JoinColumn(name = "agent_id")
    private AdhAgent agent;
    
    @Column(columnDefinition = "TEXT")
    private String reponseJson; // Stockage des réponses du formulaire
    
    private String statut; // SOUMISE, EN_COURS, ACCEPTEE, REFUSEE, TERMINEE
    
    @Column(columnDefinition = "TEXT")
    private String commentaire;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDemande;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTraitement;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFinalisation;
    
    private Long traitePar; // ID de l'utilisateur qui traite la demande
    
    // Documents attachés - stored as JSON in TEXT field to avoid BLOB issues
    @Column(columnDefinition = "TEXT")
    private String documentsJson; // JSON array of file objects with base64 content
    
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
        if (dateDemande == null) {
            dateDemande = new Date();
        }
        if (statut == null) {
            statut = "SOUMISE";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }
    
    public String getDocumentsJson() {
        return documentsJson;
    }
    
    public void setDocumentsJson(String documentsJson) {
        this.documentsJson = documentsJson;
    }
}