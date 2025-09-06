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
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;
import com.fasterxml.jackson.core.type.TypeReference;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.List;
import java.util.Base64;
import java.io.ByteArrayInputStream;

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
        
        // Fichiers joints
        layout.add(createFilesSection());
        
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
    
    private VerticalLayout createFilesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        
        H3 title = new H3("Fichiers joints");
        section.add(title);
        
        if (demande.getDocumentsJson() != null && !demande.getDocumentsJson().trim().isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> documentsData = mapper.readValue(demande.getDocumentsJson(), new TypeReference<Map<String, Object>>() {});
                
                boolean hasFiles = false;
                
                for (Map.Entry<String, Object> entry : documentsData.entrySet()) {
                    String fieldName = entry.getKey();
                    Object value = entry.getValue();
                    
                    if (value instanceof List<?>) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> files = (List<Map<String, Object>>) value;
                        
                        if (!files.isEmpty()) {
                            hasFiles = true;
                            
                            H4 fieldTitle = new H4("📎 " + fieldName);
                            section.add(fieldTitle);
                            
                            for (Map<String, Object> fileData : files) {
                                section.add(createFileItem(fileData));
                            }
                        }
                    }
                }
                
                if (!hasFiles) {
                    Div noFilesDiv = new Div();
                    noFilesDiv.setText("Aucun fichier joint à cette demande");
                    noFilesDiv.getStyle().set("font-style", "italic")
                                      .set("color", "var(--lumo-secondary-text-color)");
                    section.add(noFilesDiv);
                }
                
            } catch (Exception e) {
                Div errorDiv = new Div();
                errorDiv.setText("Erreur lors du chargement des fichiers: " + e.getMessage());
                errorDiv.getStyle().set("color", "var(--lumo-error-text-color)");
                section.add(errorDiv);
                
                // Debug information
                Div debugDiv = new Div();
                debugDiv.setText("Debug: Raw documentsJson = " + demande.getDocumentsJson());
                debugDiv.getStyle().set("font-size", "0.8em")
                          .set("color", "var(--lumo-secondary-text-color)")
                          .set("margin-top", "0.5rem")
                          .set("font-family", "monospace")
                          .set("word-break", "break-all");
                section.add(debugDiv);
            }
        } else {
            Div noFilesDiv = new Div();
            noFilesDiv.setText("Aucun fichier joint à cette demande");
            noFilesDiv.getStyle().set("font-style", "italic")
                              .set("color", "var(--lumo-secondary-text-color)");
            section.add(noFilesDiv);
            
            // Debug information
            Div debugDiv = new Div();
            debugDiv.setText("Debug: documentsJson = " + (demande.getDocumentsJson() == null ? "null" : "'" + demande.getDocumentsJson() + "'"));
            debugDiv.getStyle().set("font-size", "0.8em")
                      .set("color", "var(--lumo-secondary-text-color)")
                      .set("margin-top", "0.5rem")
                      .set("font-family", "monospace");
            section.add(debugDiv);
        }
        
        return section;
    }
    
    private HorizontalLayout createFileItem(Map<String, Object> fileData) {
        HorizontalLayout fileItem = new HorizontalLayout();
        fileItem.setSpacing(true);
        fileItem.setAlignItems(FlexComponent.Alignment.CENTER);
        fileItem.getStyle().set("padding", "8px")
                          .set("margin-bottom", "4px")
                          .set("background-color", "var(--lumo-contrast-5pct)")
                          .set("border-radius", "var(--lumo-border-radius)");
        
        // File icon
        Span icon = new Span(VaadinIcon.FILE.create());
        icon.getStyle().set("color", "var(--lumo-primary-text-color)");
        
        // File info
        VerticalLayout fileInfo = new VerticalLayout();
        fileInfo.setPadding(false);
        fileInfo.setSpacing(false);
        
        String filename = String.valueOf(fileData.get("filename"));
        String contentType = String.valueOf(fileData.get("contentType"));
        Object sizeObj = fileData.get("size");
        
        Span nameSpan = new Span(filename);
        nameSpan.getStyle().set("font-weight", "500");
        
        String sizeText = "";
        if (sizeObj instanceof Number) {
            long size = ((Number) sizeObj).longValue();
            sizeText = formatFileSize(size);
        }
        
        Span detailsSpan = new Span(contentType + (sizeText.isEmpty() ? "" : " • " + sizeText));
        detailsSpan.getStyle().set("font-size", "12px")
                              .set("color", "var(--lumo-secondary-text-color)");
        
        fileInfo.add(nameSpan, detailsSpan);
        
        // Download button
        Button downloadBtn = new Button("Télécharger", VaadinIcon.DOWNLOAD.create());
        downloadBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL,
                                   com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        
        String base64Content = String.valueOf(fileData.get("base64Content"));
        if (base64Content != null && !base64Content.equals("null")) {
            StreamResource resource = new StreamResource(filename, () -> {
                try {
                    byte[] fileBytes = Base64.getDecoder().decode(base64Content);
                    return new ByteArrayInputStream(fileBytes);
                } catch (Exception e) {
                    return new ByteArrayInputStream(new byte[0]);
                }
            });
            
            Anchor downloadLink = new Anchor(resource, "");
            downloadLink.add(downloadBtn);
            downloadLink.getElement().setAttribute("download", true);
            
            fileItem.add(icon, fileInfo, downloadLink);
        } else {
            downloadBtn.setEnabled(false);
            downloadBtn.setText("Indisponible");
            fileItem.add(icon, fileInfo, downloadBtn);
        }
        
        fileItem.setFlexGrow(1, fileInfo);
        
        return fileItem;
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
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