package com.fosagri.application.dto;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.entities.PrestationRef;

import java.util.Date;

public class DemandeViewDto {
    private Long id;
    private String statut;
    private Date dateDemande;
    private Date dateTraitement;
    private String commentaire;
    private AdhAgent agent;
    private PrestationRef prestation;

    public DemandeViewDto() {
    }

    public DemandeViewDto(Long id, String statut, Date dateDemande, Date dateTraitement, 
                         String commentaire, AdhAgent agent, PrestationRef prestation) {
        this.id = id;
        this.statut = statut;
        this.dateDemande = dateDemande;
        this.dateTraitement = dateTraitement;
        this.commentaire = commentaire;
        this.agent = agent;
        this.prestation = prestation;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Date getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(Date dateDemande) {
        this.dateDemande = dateDemande;
    }

    public Date getDateTraitement() {
        return dateTraitement;
    }

    public void setDateTraitement(Date dateTraitement) {
        this.dateTraitement = dateTraitement;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public AdhAgent getAgent() {
        return agent;
    }

    public void setAgent(AdhAgent agent) {
        this.agent = agent;
    }

    public PrestationRef getPrestation() {
        return prestation;
    }

    public void setPrestation(PrestationRef prestation) {
        this.prestation = prestation;
    }
}