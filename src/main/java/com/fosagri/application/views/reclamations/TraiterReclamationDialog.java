package com.fosagri.application.views.reclamations;

import com.fosagri.application.entities.Reclamation;
import com.fosagri.application.entities.Reclamation.StatutReclamation;
import com.fosagri.application.entities.Reclamation.PrioriteReclamation;
import com.fosagri.application.services.ReclamationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.util.ArrayList;
import java.util.List;

public class TraiterReclamationDialog extends Dialog {
    
    private final Reclamation reclamation;
    private final ReclamationService reclamationService;
    private final List<SaveListener> saveListeners = new ArrayList<>();
    
    private ComboBox<StatutReclamation> statutCombo;
    private ComboBox<PrioriteReclamation> prioriteCombo;
    private TextArea responseArea;
    
    public TraiterReclamationDialog(Reclamation reclamation, ReclamationService reclamationService) {
        this.reclamation = reclamation;
        this.reclamationService = reclamationService;
        
        setHeaderTitle("Traiter la réclamation #" + reclamation.getId());
        setWidth("700px");
        setHeight("600px");
        setModal(true);
        setResizable(true);
        
        createContent();
    }
    
    private void createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        
        // Current information
        H3 currentTitle = new H3("Informations actuelles");
        
        // Get agent info safely without triggering LOB loading
        String agentInfo = reclamationService.getAgentNameForReclamation(reclamation.getId());
        addInfoRow(layout, "Agent", agentInfo);
        
        addInfoRow(layout, "Objet", reclamation.getObjet());
        addInfoRow(layout, "Type", reclamation.getType().getLabel());
        addInfoRow(layout, "Statut actuel", createStatusBadge());
        
        // Detail (readonly)
        if (reclamation.getDetail() != null && !reclamation.getDetail().trim().isEmpty()) {
            H4 detailTitle = new H4("Détail de la réclamation");
            layout.add(detailTitle);
            
            TextArea detailArea = new TextArea();
            detailArea.setValue(reclamation.getDetail());
            detailArea.setWidthFull();
            detailArea.setHeight("80px");
            detailArea.setReadOnly(true);
            layout.add(detailArea);
        }
        
        // Processing section
        H4 processingTitle = new H4("Traitement");
        layout.add(processingTitle);
        
        // Status update
        statutCombo = new ComboBox<>("Nouveau statut");
        statutCombo.setItems(StatutReclamation.values());
        statutCombo.setItemLabelGenerator(StatutReclamation::getLabel);
        statutCombo.setValue(reclamation.getStatut());
        statutCombo.setWidthFull();
        
        // Priority update
        prioriteCombo = new ComboBox<>("Priorité");
        prioriteCombo.setItems(PrioriteReclamation.values());
        prioriteCombo.setItemLabelGenerator(PrioriteReclamation::getLabel);
        prioriteCombo.setValue(reclamation.getPriorite() != null ? 
                               reclamation.getPriorite() : PrioriteReclamation.NORMALE);
        prioriteCombo.setWidthFull();
        
        // Response area
        responseArea = new TextArea("Réponse de l'organisation");
        responseArea.setPlaceholder("Saisissez la réponse ou les actions entreprises...");
        responseArea.setWidthFull();
        responseArea.setHeight("150px");
        if (reclamation.getReponseOrganisation() != null) {
            responseArea.setValue(reclamation.getReponseOrganisation());
        }
        
        layout.add(statutCombo, prioriteCombo, responseArea);
        
        // Buttons
        Button saveButton = new Button("Enregistrer");
        saveButton.addClickListener(e -> saveChanges());
        saveButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("Annuler");
        cancelButton.addClickListener(e -> close());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        layout.add(buttonLayout);
        add(layout);
    }
    
    private void saveChanges() {
        try {
            boolean statusChanged = !statutCombo.getValue().equals(reclamation.getStatut());
            boolean priorityChanged = !prioriteCombo.getValue().equals(reclamation.getPriorite());
            String response = responseArea.getValue();
            boolean hasResponse = response != null && !response.trim().isEmpty();
            
            // Update the reclamation entity with new values
            reclamation.setPriorite(prioriteCombo.getValue());
            
            // Handle different scenarios
            if (hasResponse) {
                // If there's a response, use addOrganizationResponse which handles status updates
                reclamationService.addOrganizationResponse(
                    reclamation.getId(), 
                    response.trim(), 
                    1L // TODO: Get current user ID
                );
                
                // If status was changed and is different from what addOrganizationResponse sets
                if (statusChanged && !statutCombo.getValue().equals(reclamation.getStatut())) {
                    reclamationService.updateStatut(reclamation.getId(), statutCombo.getValue());
                }
            } else {
                // No response provided, handle status and priority changes
                if (statusChanged) {
                    reclamationService.updateStatut(reclamation.getId(), statutCombo.getValue());
                }
                
                // Save priority change if needed
                if (priorityChanged || statusChanged) {
                    // Refresh the entity to get latest changes from updateStatut
                    Reclamation refreshedReclamation = reclamationService.findById(reclamation.getId()).orElse(reclamation);
                    refreshedReclamation.setPriorite(prioriteCombo.getValue());
                    reclamationService.save(refreshedReclamation);
                }
            }
            
            // Get final updated reclamation
            Reclamation updatedReclamation = reclamationService.findById(reclamation.getId()).orElse(reclamation);
            
            // Notify listeners
            saveListeners.forEach(listener -> listener.onSave(updatedReclamation));
            
            close();
            
            Notification.show("Réclamation mise à jour avec succès");
            
        } catch (Exception e) {
            Notification.show("Erreur lors de la sauvegarde: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addInfoRow(VerticalLayout layout, String label, String value) {
        Paragraph row = new Paragraph();
        Span labelSpan = new Span(label + ": ");
        labelSpan.getStyle().set("font-weight", "bold");
        Span valueSpan = new Span(value);
        row.add(labelSpan, valueSpan);
        layout.add(row);
    }
    
    private void addInfoRow(VerticalLayout layout, String label, Span valueComponent) {
        Paragraph row = new Paragraph();
        Span labelSpan = new Span(label + ": ");
        labelSpan.getStyle().set("font-weight", "bold");
        row.add(labelSpan, valueComponent);
        layout.add(row);
    }
    
    private Span createStatusBadge() {
        Span badge = new Span(reclamation.getStatut().getLabel());
        badge.getElement().getThemeList().add("badge");
        
        switch (reclamation.getStatut()) {
            case NOUVELLE:
                badge.getElement().getThemeList().add("primary");
                break;
            case EN_COURS:
                badge.getElement().getThemeList().add("contrast");
                break;
            case RESOLUE:
                badge.getElement().getThemeList().add("success");
                break;
            case FERMEE:
                badge.getElement().getThemeList().add("normal");
                break;
            case REJETEE:
                badge.getElement().getThemeList().add("error");
                break;
        }
        
        return badge;
    }
    
    // Listener interface
    public interface SaveListener {
        void onSave(Reclamation reclamation);
    }
    
    public void addSaveListener(SaveListener listener) {
        saveListeners.add(listener);
    }
}