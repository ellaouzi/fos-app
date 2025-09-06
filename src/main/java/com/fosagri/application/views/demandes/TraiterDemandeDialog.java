package com.fosagri.application.views.demandes;

import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.services.DemandePrestationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TraiterDemandeDialog extends Dialog {
    
    private final DemandePrestation demande;
    private final DemandePrestationService service;
    private final List<SaveListener> saveListeners = new ArrayList<>();
    
    private Select<String> nouveauStatutSelect;
    private TextArea commentaireArea;
    
    public TraiterDemandeDialog(DemandePrestation demande, DemandePrestationService service) {
        this.demande = demande;
        this.service = service;
        
        setHeaderTitle("Traiter la demande #" + demande.getId());
        setWidth("700px");
        setHeight("500px");
        setModal(true);
        setResizable(true);
        
        createContent();
    }
    
    private void createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        
        // Résumé de la demande
        layout.add(createDemandeResume());
        
        // Section traitement
        layout.add(createTraitementSection());
        
        // Boutons
        createButtons(layout);
        
        add(layout);
    }
    
    private VerticalLayout createDemandeResume() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        
        H3 title = new H3("Résumé de la demande");
        section.add(title);
        
        AdhAgent agent = demande.getAgent();
        if (agent != null) {
            Div agentDiv = new Div();
            agentDiv.setText("Agent: " + agent.getNOM_AG() + " " + agent.getPR_AG() + " (CIN: " + agent.getCIN_AG() + ")");
            section.add(agentDiv);
        }
        
        PrestationRef prestation = demande.getPrestation();
        if (prestation != null) {
            Div prestationDiv = new Div();
            prestationDiv.setText("Prestation: " + prestation.getLabel());
            section.add(prestationDiv);
        }
        
        Div statutActuelDiv = new Div();
        statutActuelDiv.setText("Statut actuel: " + getStatutLabel(demande.getStatut()));
        section.add(statutActuelDiv);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (demande.getDateDemande() != null) {
            Div dateDiv = new Div();
            dateDiv.setText("Date de demande: " + sdf.format(demande.getDateDemande()));
            section.add(dateDiv);
        }
        
        if (demande.getCommentaire() != null && !demande.getCommentaire().trim().isEmpty()) {
            Div commentaireActuelDiv = new Div();
            commentaireActuelDiv.setText("Commentaire actuel: " + demande.getCommentaire());
            commentaireActuelDiv.getStyle().set("font-style", "italic");
            section.add(commentaireActuelDiv);
        }
        
        return section;
    }
    
    private VerticalLayout createTraitementSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);
        
        H3 title = new H3("Traitement");
        section.add(title);
        
        // Sélection du nouveau statut
        nouveauStatutSelect = new Select<>();
        nouveauStatutSelect.setLabel("Nouveau statut");
        nouveauStatutSelect.setWidthFull();
        
        // Définir les statuts possibles selon le statut actuel
        List<String> statutsPossibles = new ArrayList<>();
        String statutActuel = demande.getStatut();
        
        switch (statutActuel) {
            case "SOUMISE":
                statutsPossibles.add("EN_COURS");
                statutsPossibles.add("ACCEPTEE");
                statutsPossibles.add("REFUSEE");
                break;
            case "EN_COURS":
                statutsPossibles.add("ACCEPTEE");
                statutsPossibles.add("REFUSEE");
                statutsPossibles.add("TERMINEE");
                break;
            case "ACCEPTEE":
                statutsPossibles.add("TERMINEE");
                statutsPossibles.add("EN_COURS"); // Retour possible
                break;
            default:
                // Pour les statuts finaux, permettre le retour en cours si nécessaire
                if (!statutActuel.equals("TERMINEE")) {
                    statutsPossibles.add("EN_COURS");
                }
                break;
        }
        
        nouveauStatutSelect.setItems(statutsPossibles);
        nouveauStatutSelect.setItemLabelGenerator(this::getStatutLabel);
        nouveauStatutSelect.setRequiredIndicatorVisible(true);
        section.add(nouveauStatutSelect);
        
        // Zone de commentaire
        commentaireArea = new TextArea("Commentaire");
        commentaireArea.setWidthFull();
        commentaireArea.setMinHeight("120px");
        commentaireArea.setPlaceholder("Ajoutez un commentaire sur le traitement de cette demande...");
        section.add(commentaireArea);
        
        return section;
    }
    
    private void createButtons(VerticalLayout layout) {
        Button saveButton = new Button("Enregistrer", e -> saveTreatment());
        saveButton.getStyle().set("background-color", "var(--lumo-primary-color)");
        saveButton.getStyle().set("color", "white");
        
        Button cancelButton = new Button("Annuler", e -> close());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        layout.add(buttonLayout);
    }
    
    private void saveTreatment() {
        String nouveauStatut = nouveauStatutSelect.getValue();
        if (nouveauStatut == null || nouveauStatut.trim().isEmpty()) {
            Notification.show("Veuillez sélectionner un nouveau statut");
            return;
        }
        
        String commentaire = commentaireArea.getValue();
        if (commentaire == null) {
            commentaire = "";
        }
        
        try {
            // TODO: Récupérer l'ID de l'utilisateur connecté
            Long traitePar = 1L; // Pour l'instant, utiliser un ID fixe
            
            service.updateStatut(demande.getId(), nouveauStatut, commentaire, traitePar);
            
            // Mettre à jour l'objet local pour les listeners
            demande.setStatut(nouveauStatut);
            demande.setCommentaire(commentaire);
            demande.setTraitePar(traitePar);
            
            saveListeners.forEach(listener -> listener.onSave(demande));
            
            Notification.show("Demande mise à jour avec succès");
            close();
            
        } catch (Exception e) {
            Notification.show("Erreur lors de la mise à jour: " + e.getMessage());
        }
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
    
    public void addSaveListener(SaveListener listener) {
        saveListeners.add(listener);
    }
    
    @FunctionalInterface
    public interface SaveListener {
        void onSave(DemandePrestation demande);
    }
}