package com.fosagri.application.entities;

import com.fosagri.application.model.AdhAgent;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "reclamations")
public class Reclamation {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private AdhAgent agent;
    
    @Column(nullable = false, length = 200)
    private String objet; // Subject/Object of the complaint
    
    @Column(columnDefinition = "TEXT")
    private String detail; // Detailed description
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeReclamation type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReclamation statut;
    
    // File attachment - stored as JSON to avoid BLOB issues like in DemandePrestation
    @Column(columnDefinition = "TEXT")
    private String fichierAttache; // JSON with file data
    
    @Column(columnDefinition = "TEXT")
    private String reponseOrganisation; // Organization's response
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTraitement;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCloture;
    
    private Long traitePar; // ID of user who processes the complaint
    
    // Priority level
    @Enumerated(EnumType.STRING)
    private PrioriteReclamation priorite;
    
    // Secure logs
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;
    
    private Long createdBy;
    private Long updatedBy;
    
    @PrePersist
    protected void onCreate() {
        created = new Date();
        dateCreation = new Date();
        if (statut == null) {
            statut = StatutReclamation.NOUVELLE;
        }
        if (priorite == null) {
            priorite = PrioriteReclamation.NORMALE;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }
    
    // Enums for type, status and priority
    public enum TypeReclamation {
        ADMINISTRATIVE("Administrative"),
        FINANCIERE("Financière"),
        TECHNIQUE("Technique"),
        PRESTATIONS("Prestations"),
        AUTRE("Autre");
        
        private final String label;
        
        TypeReclamation(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    public enum StatutReclamation {
        NOUVELLE("Nouvelle"),
        EN_COURS("En cours de traitement"),
        RESOLUE("Résolue"),
        FERMEE("Fermée"),
        REJETEE("Rejetée");
        
        private final String label;
        
        StatutReclamation(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    public enum PrioriteReclamation {
        FAIBLE("Faible"),
        NORMALE("Normale"),
        HAUTE("Haute"),
        URGENTE("Urgente");
        
        private final String label;
        
        PrioriteReclamation(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
    }
    
    // Custom equals and hashCode to avoid lazy loading issues
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reclamation that = (Reclamation) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}