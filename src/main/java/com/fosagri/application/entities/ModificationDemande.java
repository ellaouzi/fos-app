package com.fosagri.application.entities;

import com.fosagri.application.model.AdhAgent;
import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "modification_demande")
public class ModificationDemande {

    public enum StatutModification {
        EN_ATTENTE,  // Pending review
        VALIDE,      // Approved and applied
        REFUSE       // Rejected
    }

    public enum TypeEntite {
        AGENT,
        CONJOINT,
        ENFANT
    }

    public enum TypeAction {
        CREATION,     // New entity creation
        MODIFICATION  // Modification of existing entity
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // The agent who owns this modification request
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", referencedColumnName = "adhagentid")
    private AdhAgent agent;

    // Type of entity being modified
    @Enumerated(EnumType.STRING)
    @Column(name = "type_entite")
    private TypeEntite typeEntite;

    // Type of action (creation or modification)
    @Enumerated(EnumType.STRING)
    @Column(name = "type_action")
    private TypeAction typeAction = TypeAction.MODIFICATION;

    // ID of the specific entity being modified (agent ID, conjoint ID, or enfant ID)
    @Column(name = "entite_id")
    private Integer entiteId;

    // Display label for the entity (e.g., "Agent: Ahmed KALOUI" or "Conjoint: Fatima KALOUI")
    @Column(name = "entite_label")
    private String entiteLabel;

    // JSON storing OLD values (for comparison)
    @Column(name = "anciennes_valeurs_json", columnDefinition = "TEXT")
    private String anciennesValeursJson;

    // JSON storing NEW values (pending changes)
    @Column(name = "nouvelles_valeurs_json", columnDefinition = "TEXT")
    private String nouvellesValeursJson;

    // JSON storing document changes (base64 or file paths for new documents)
    @Column(name = "documents_json", columnDefinition = "TEXT")
    private String documentsJson;

    // Current status
    @Enumerated(EnumType.STRING)
    private StatutModification statut = StatutModification.EN_ATTENTE;

    // Admin comment when approving/rejecting
    @Column(name = "commentaire_admin", columnDefinition = "TEXT")
    private String commentaireAdmin;

    // Audit fields
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_creation")
    private Date dateCreation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "date_traitement")
    private Date dateTraitement;

    @Column(name = "traite_par")
    private Long traitePar; // Admin user ID who processed

    @PrePersist
    protected void onCreate() {
        dateCreation = new Date();
        if (statut == null) {
            statut = StatutModification.EN_ATTENTE;
        }
    }

    // Helper method to get status display label
    public String getStatutLabel() {
        if (statut == null) return "";
        switch (statut) {
            case EN_ATTENTE: return "En attente";
            case VALIDE: return "Validee";
            case REFUSE: return "Refusee";
            default: return statut.name();
        }
    }

    // Helper method to get entity type display label
    public String getTypeEntiteLabel() {
        if (typeEntite == null) return "";
        switch (typeEntite) {
            case AGENT: return "Agent";
            case CONJOINT: return "Conjoint";
            case ENFANT: return "Enfant";
            default: return typeEntite.name();
        }
    }

    // Helper method to get action type display label
    public String getTypeActionLabel() {
        if (typeAction == null) return "Modification";
        switch (typeAction) {
            case CREATION: return "Creation";
            case MODIFICATION: return "Modification";
            default: return typeAction.name();
        }
    }

    // Check if this is a creation request
    public boolean isCreation() {
        return typeAction == TypeAction.CREATION;
    }
}
