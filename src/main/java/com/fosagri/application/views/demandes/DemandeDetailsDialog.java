package com.fosagri.application.views.demandes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.entities.PrestationRef;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.text.SimpleDateFormat;
import java.util.Map;

public class DemandeDetailsDialog extends Dialog {
    
    private final DemandePrestation demande;
    
    public DemandeDetailsDialog(DemandePrestation demande) {
        this.demande = demande;
        
        setHeaderTitle("Détails de la demande #" + demande.getId());
        setWidth("800px");
        setHeight("600px");
        setModal(true);
        setResizable(true);
        
        createContent();
    }
    
    private void createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        
        // Informations générales
        layout.add(createGeneralInfo());
        
        // Informations sur la prestation
        layout.add(createPrestationInfo());
        
        // Réponses du formulaire
        layout.add(createFormResponses());
        
        // Historique des traitements
        layout.add(createTreatmentHistory());
        
        // Bouton fermer
        Button closeButton = new Button("Fermer", e -> close());
        HorizontalLayout buttonLayout = new HorizontalLayout(closeButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        layout.add(buttonLayout);
        
        add(layout);
    }
    
    private VerticalLayout createGeneralInfo() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        
        H3 title = new H3("Informations générales");
        section.add(title);
        
        AdhAgent agent = demande.getAgent();
        if (agent != null) {
            Div agentInfo = new Div();
            agentInfo.setText("Agent: " + agent.getNOM_AG() + " " + agent.getPR_AG() + " (CIN: " + agent.getCIN_AG() + ")");
            section.add(agentInfo);
        }
        
        Div statutDiv = new Div();
        statutDiv.setText("Statut: " + getStatutLabel(demande.getStatut()));
        section.add(statutDiv);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        if (demande.getDateDemande() != null) {
            Div dateDiv = new Div();
            dateDiv.setText("Date de demande: " + sdf.format(demande.getDateDemande()));
            section.add(dateDiv);
        }
        
        if (demande.getDateTraitement() != null) {
            Div dateTraitementDiv = new Div();
            dateTraitementDiv.setText("Date de traitement: " + sdf.format(demande.getDateTraitement()));
            section.add(dateTraitementDiv);
        }
        
        if (demande.getDateFinalisation() != null) {
            Div dateFinalisationDiv = new Div();
            dateFinalisationDiv.setText("Date de finalisation: " + sdf.format(demande.getDateFinalisation()));
            section.add(dateFinalisationDiv);
        }
        
        return section;
    }
    
    private VerticalLayout createPrestationInfo() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        
        H3 title = new H3("Prestation demandée");
        section.add(title);
        
        PrestationRef prestation = demande.getPrestation();
        if (prestation != null) {
            Div labelDiv = new Div();
            labelDiv.setText("Label: " + prestation.getLabel());
            section.add(labelDiv);
            
            if (prestation.getType() != null) {
                Div typeDiv = new Div();
                typeDiv.setText("Type: " + prestation.getType());
                section.add(typeDiv);
            }
            
            if (prestation.getDescription() != null && !prestation.getDescription().trim().isEmpty()) {
                Div descDiv = new Div();
                descDiv.setText("Description: " + prestation.getDescription());
                section.add(descDiv);
            }
        }
        
        return section;
    }
    
    private VerticalLayout createFormResponses() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        
        H3 title = new H3("Réponses du formulaire");
        section.add(title);
        
        if (demande.getReponseJson() != null && !demande.getReponseJson().trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Object jsonObject = mapper.readValue(demande.getReponseJson(), Object.class);
                String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
                
                TextArea jsonArea = new TextArea();
                jsonArea.setValue(prettyJson);
                jsonArea.setWidthFull();
                jsonArea.setMinHeight("200px");
                jsonArea.setReadOnly(true);
                section.add(jsonArea);
                
                // Affichage plus lisible si possible
                if (jsonObject instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> responses = (Map<String, Object>) jsonObject;
                    
                    VerticalLayout readableResponses = new VerticalLayout();
                    readableResponses.setPadding(false);
                    readableResponses.setSpacing(false);
                    
                    H4 readableTitle = new H4("Réponses (format lisible)");
                    readableResponses.add(readableTitle);
                    
                    for (Map.Entry<String, Object> entry : responses.entrySet()) {
                        Div responseDiv = new Div();
                        responseDiv.setText(entry.getKey() + ": " + (entry.getValue() != null ? entry.getValue().toString() : ""));
                        responseDiv.getStyle().set("margin-bottom", "0.5rem");
                        readableResponses.add(responseDiv);
                    }
                    
                    section.add(readableResponses);
                }
                
            } catch (Exception e) {
                TextArea errorArea = new TextArea();
                errorArea.setValue("Erreur lors du décodage des réponses: " + e.getMessage() + "\n\nDonnées brutes:\n" + demande.getReponseJson());
                errorArea.setWidthFull();
                errorArea.setMinHeight("150px");
                errorArea.setReadOnly(true);
                section.add(errorArea);
            }
        } else {
            Div noResponseDiv = new Div();
            noResponseDiv.setText("Aucune réponse de formulaire disponible");
            noResponseDiv.getStyle().set("font-style", "italic");
            section.add(noResponseDiv);
            
            // Debug information
            Div debugDiv = new Div();
            debugDiv.setText("Debug: reponseJson = " + (demande.getReponseJson() == null ? "null" : "'" + demande.getReponseJson() + "'"));
            debugDiv.getStyle().set("font-size", "0.8em")
                      .set("color", "var(--lumo-secondary-text-color)")
                      .set("margin-top", "0.5rem");
            section.add(debugDiv);
        }
        
        return section;
    }
    
    private VerticalLayout createTreatmentHistory() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        
        H3 title = new H3("Historique du traitement");
        section.add(title);
        
        if (demande.getCommentaire() != null && !demande.getCommentaire().trim().isEmpty()) {
            TextArea commentaireArea = new TextArea("Commentaire");
            commentaireArea.setValue(demande.getCommentaire());
            commentaireArea.setWidthFull();
            commentaireArea.setReadOnly(true);
            section.add(commentaireArea);
        }
        
        if (demande.getTraitePar() != null) {
            Div traiteParDiv = new Div();
            traiteParDiv.setText("Traité par l'utilisateur ID: " + demande.getTraitePar());
            section.add(traiteParDiv);
        }
        
        return section;
    }
    
    private String getStatutLabel(String statut) {
        if (statut == null) return "Inconnu";
        switch (statut) {
            case "SOUMISE": return "Soumise";
            case "EN_COURS": return "En cours";
            case "ACCEPTEE": return "Acceptée";
            case "REFUSEE": return "Refusée";
            case "TERMINEE": return "Terminée";
            default: return statut;
        }
    }
}