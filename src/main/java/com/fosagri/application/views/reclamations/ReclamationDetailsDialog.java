package com.fosagri.application.views.reclamations;

import com.fosagri.application.entities.Reclamation;
import com.fosagri.application.services.ReclamationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Map;

public class ReclamationDetailsDialog extends Dialog {
    
    private final Reclamation reclamation;
    private final ReclamationService reclamationService;
    
    public ReclamationDetailsDialog(Reclamation reclamation, ReclamationService reclamationService) {
        this.reclamation = reclamation;
        this.reclamationService = reclamationService;
        
        setHeaderTitle("Détails de la réclamation #" + reclamation.getId());
        setWidth("800px");
        setHeight("700px");
        setModal(true);
        setResizable(true);
        
        createContent();
    }
    
    private void createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        // Basic information
        H3 infoTitle = new H3("Informations générales");
        
        // Get agent info safely without triggering LOB loading
        String agentInfo = reclamationService.getAgentNameForReclamation(reclamation.getId());
        addInfoRow(layout, "Agent", agentInfo);
        
        addInfoRow(layout, "Objet", reclamation.getObjet());
        addInfoRow(layout, "Type", reclamation.getType().getLabel());
        addInfoRow(layout, "Statut", createStatusBadge());
        addInfoRow(layout, "Priorité", 
            reclamation.getPriorite() != null ? reclamation.getPriorite().getLabel() : "Normale");
        
        // Dates
        H4 datesTitle = new H4("Dates importantes");
        layout.add(datesTitle);
        
        addInfoRow(layout, "Date de création", 
            reclamation.getDateCreation() != null ? sdf.format(reclamation.getDateCreation()) : "N/A");
        
        addInfoRow(layout, "Date de traitement", 
            reclamation.getDateTraitement() != null ? sdf.format(reclamation.getDateTraitement()) : "Non traité");
        
        addInfoRow(layout, "Date de clôture", 
            reclamation.getDateCloture() != null ? sdf.format(reclamation.getDateCloture()) : "Non clôturé");
        
        // Detail
        if (reclamation.getDetail() != null && !reclamation.getDetail().trim().isEmpty()) {
            H4 detailTitle = new H4("Détail de la réclamation");
            layout.add(detailTitle);
            
            TextArea detailArea = new TextArea();
            detailArea.setValue(reclamation.getDetail());
            detailArea.setWidthFull();
            detailArea.setHeight("100px");
            detailArea.setReadOnly(true);
            layout.add(detailArea);
        }
        
        // Organization response
        if (reclamation.getReponseOrganisation() != null && !reclamation.getReponseOrganisation().trim().isEmpty()) {
            H4 responseTitle = new H4("Réponse de l'organisation");
            layout.add(responseTitle);
            
            TextArea responseArea = new TextArea();
            responseArea.setValue(reclamation.getReponseOrganisation());
            responseArea.setWidthFull();
            responseArea.setHeight("100px");
            responseArea.setReadOnly(true);
            layout.add(responseArea);
        }
        
        // File attachment info
        Map<String, Object> fileData = reclamationService.getFileAttachment(reclamation.getFichierAttache());
        if (fileData != null) {
            H4 fileTitle = new H4("Fichier attaché");
            layout.add(fileTitle);
            
            addInfoRow(layout, "Nom du fichier", (String) fileData.get("filename"));
            addInfoRow(layout, "Type", (String) fileData.get("contentType"));
            
            if (fileData.get("size") != null) {
                long size = ((Number) fileData.get("size")).longValue();
                String sizeStr = formatFileSize(size);
                addInfoRow(layout, "Taille", sizeStr);
            }
            
            // Download button could be added here if needed
            Button downloadBtn = new Button("Télécharger le fichier");
            downloadBtn.addClickListener(e -> downloadFile(fileData));
            layout.add(downloadBtn);
        }
        
        // Close button
        Button closeButton = new Button("Fermer");
        closeButton.addClickListener(e -> close());
        layout.add(closeButton);
        
        add(layout);
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
    
    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return (size / (1024 * 1024)) + " MB";
    }
    
    private void downloadFile(Map<String, Object> fileData) {
        try {
            String filename = (String) fileData.get("filename");
            String contentType = (String) fileData.get("contentType");
            String base64Content = (String) fileData.get("content");
            
            if (base64Content == null || base64Content.trim().isEmpty()) {
                com.vaadin.flow.component.notification.Notification.show(
                    "Aucun contenu de fichier disponible");
                return;
            }
            
            // Create data URL for download
            String dataUrl = "data:" + (contentType != null ? contentType : "application/octet-stream") + 
                           ";base64," + base64Content;
            
            // Use JavaScript to trigger download
            String script = String.format(
                "const link = document.createElement('a');" +
                "link.href = '%s';" +
                "link.download = '%s';" +
                "document.body.appendChild(link);" +
                "link.click();" +
                "document.body.removeChild(link);",
                dataUrl, filename != null ? filename.replace("'", "\\'") : "attachment"
            );
            
            getUI().ifPresent(ui -> ui.getPage().executeJs(script));
            
            com.vaadin.flow.component.notification.Notification.show(
                "Téléchargement du fichier " + filename + " démarré");
                
        } catch (Exception e) {
            com.vaadin.flow.component.notification.Notification.show(
                "Erreur lors du téléchargement: " + e.getMessage());
            e.printStackTrace();
        }
    }
}