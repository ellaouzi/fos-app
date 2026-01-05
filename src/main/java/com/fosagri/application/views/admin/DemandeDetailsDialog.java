package com.fosagri.application.views.admin;

import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.services.DemandePrestationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DemandeDetailsDialog extends Dialog {

    private final DemandePrestation demande;
    private final DemandePrestationService demandeService;
    private final Runnable onUpdate;
    private ComboBox<String> statusCombo;
    private TextArea commentArea;

    public DemandeDetailsDialog(DemandePrestation demande, DemandePrestationService demandeService, Runnable onUpdate) {
        this.demande = demande;
        this.demandeService = demandeService;
        this.onUpdate = onUpdate;

        setHeaderTitle("Détails de la Demande #" + demande.getId());
        setWidth("800px");
        setHeight("600px");

        createContent();
        createFooter();
    }

    private void createContent() {
        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        tabSheet.add("Informations", createInfoTab());
        tabSheet.add("Réponses", createResponsesTab());
        tabSheet.add("Documents", createDocumentsTab());
        tabSheet.add("Traitement", createProcessingTab());

        add(tabSheet);
    }

    private Div createInfoTab() {
        Div content = new Div();
        content.getStyle().set("padding", "1rem");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Agent info
        Div agentSection = createSection("Agent");
        if (demande.getAgent() != null) {
            agentSection.add(createInfoRow("Nom", demande.getAgent().getNOM_AG() + " " + demande.getAgent().getPR_AG()));
            agentSection.add(createInfoRow("CIN", demande.getAgent().getCIN_AG()));
            agentSection.add(createInfoRow("Email", demande.getAgent().getMail()));
            agentSection.add(createInfoRow("Téléphone", demande.getAgent().getNum_Tel()));
        }

        // Prestation info
        Div prestationSection = createSection("Prestation");
        if (demande.getPrestation() != null) {
            prestationSection.add(createInfoRow("Label", demande.getPrestation().getLabel()));
            prestationSection.add(createInfoRow("Type", demande.getPrestation().getType()));
        }

        // Demande info
        Div demandeSection = createSection("Demande");
        demandeSection.add(createInfoRow("Statut", demande.getStatut()));
        demandeSection.add(createInfoRow("Date soumission", demande.getDateDemande() != null ? sdf.format(demande.getDateDemande()) : "-"));
        demandeSection.add(createInfoRow("Date traitement", demande.getDateTraitement() != null ? sdf.format(demande.getDateTraitement()) : "-"));
        demandeSection.add(createInfoRow("Date finalisation", demande.getDateFinalisation() != null ? sdf.format(demande.getDateFinalisation()) : "-"));

        content.add(agentSection, prestationSection, demandeSection);
        return content;
    }

    private Div createResponsesTab() {
        Div content = new Div();
        content.getStyle().set("padding", "1rem").set("overflow-y", "auto");

        String reponseJson = demande.getReponseJson();
        if (reponseJson != null && !reponseJson.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(reponseJson);

                Div responsesGrid = new Div();
                responsesGrid.getStyle()
                    .set("display", "grid")
                    .set("grid-template-columns", "1fr")
                    .set("gap", "0.75rem");

                Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String key = entry.getKey();
                    JsonNode valueNode = entry.getValue();
                    String value = valueNode.isTextual() ? valueNode.asText() : valueNode.toString();

                    // Skip file fields (base64 data)
                    if (value.length() > 500 || key.toLowerCase().contains("file") || key.toLowerCase().contains("photo")) {
                        value = "[Fichier joint]";
                    }

                    responsesGrid.add(createResponseItem(key, value));
                }

                content.add(responsesGrid);
            } catch (Exception e) {
                // Fallback to raw display
                TextArea rawArea = new TextArea("Réponses brutes");
                rawArea.setValue(reponseJson);
                rawArea.setReadOnly(true);
                rawArea.setWidthFull();
                rawArea.setHeight("300px");
                content.add(rawArea);
            }
        } else {
            content.add(new Span("Aucune réponse enregistrée"));
        }

        return content;
    }

    private Div createResponseItem(String label, String value) {
        Div item = new Div();
        item.getStyle()
            .set("background", "#f8fafc")
            .set("padding", "0.75rem")
            .set("border-radius", "8px")
            .set("border", "1px solid #e2e8f0");

        Span labelSpan = new Span(formatLabel(label));
        labelSpan.getStyle()
            .set("font-size", "0.75rem")
            .set("color", "#64748b")
            .set("text-transform", "uppercase")
            .set("display", "block")
            .set("margin-bottom", "0.25rem");

        Span valueSpan = new Span(value != null && !value.isEmpty() ? value : "-");
        valueSpan.getStyle()
            .set("font-weight", "500")
            .set("color", "#1e293b");

        item.add(labelSpan, valueSpan);
        return item;
    }

    private String formatLabel(String label) {
        // Convert camelCase or snake_case to readable format
        return label
            .replaceAll("([a-z])([A-Z])", "$1 $2")
            .replaceAll("_", " ")
            .toLowerCase();
    }

    private Div createDocumentsTab() {
        Div content = new Div();
        content.getStyle().set("padding", "1rem").set("overflow-y", "auto");

        H4 title = new H4("Pièces jointes");
        title.getStyle().set("margin", "0 0 1rem 0").set("color", "#1e293b");

        Div docsContainer = new Div();
        docsContainer.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fill, minmax(280px, 1fr))")
            .set("gap", "1rem");

        String json = demande.getDocumentsJson();
        if (json == null || json.isEmpty()) {
            Div emptyDiv = new Div();
            emptyDiv.setText("Aucune pièce jointe");
            emptyDiv.getStyle()
                .set("color", "#64748b")
                .set("text-align", "center")
                .set("padding", "2rem");
            content.add(title, emptyDiv);
            return content;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> documents = new java.util.ArrayList<>();

            if (json.trim().startsWith("[")) {
                // Array format
                documents = mapper.readValue(json, new TypeReference<List<Map<String, Object>>>() {});
            } else if (json.trim().startsWith("{")) {
                // Single object or map format
                Map<String, Object> singleDoc = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});
                for (Map.Entry<String, Object> entry : singleDoc.entrySet()) {
                    if (entry.getValue() instanceof List) {
                        List<?> fileList = (List<?>) entry.getValue();
                        for (Object item : fileList) {
                            if (item instanceof Map) {
                                documents.add((Map<String, Object>) item);
                            }
                        }
                    } else if (entry.getValue() instanceof Map) {
                        documents.add((Map<String, Object>) entry.getValue());
                    }
                }
                if (documents.isEmpty() && singleDoc.containsKey("filename")) {
                    documents.add(singleDoc);
                }
            }

            if (documents.isEmpty()) {
                Div emptyDiv = new Div();
                emptyDiv.setText("Aucune pièce jointe");
                emptyDiv.getStyle()
                    .set("color", "#64748b")
                    .set("text-align", "center")
                    .set("padding", "2rem");
                content.add(title, emptyDiv);
            } else {
                for (Map<String, Object> doc : documents) {
                    docsContainer.add(createDocumentCard(doc));
                }
                content.add(title, docsContainer);
            }
        } catch (Exception e) {
            System.err.println("Error parsing documentsJson: " + e.getMessage());
            e.printStackTrace();
            Div errorDiv = new Div();
            errorDiv.setText("Impossible de charger les pièces jointes");
            errorDiv.getStyle().set("color", "#ef4444");
            content.add(title, errorDiv);
        }

        return content;
    }

    private Div createDocumentCard(Map<String, Object> doc) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#f8fafc")
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "8px")
            .set("padding", "1rem")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "0.75rem");

        // File info header
        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);
        header.setWidthFull();

        String filename = getDocFilename(doc);
        Icon fileIcon = getFileIcon(filename);
        fileIcon.setSize("24px");
        fileIcon.getStyle().set("color", "#3b6b35");

        Div nameSection = new Div();
        nameSection.getStyle().set("flex", "1").set("min-width", "0");

        Span nameSpan = new Span(filename);
        nameSpan.getStyle()
            .set("font-weight", "500")
            .set("color", "#1e293b")
            .set("display", "block")
            .set("overflow", "hidden")
            .set("text-overflow", "ellipsis")
            .set("white-space", "nowrap");

        Object sizeObj = doc.get("size");
        if (sizeObj != null) {
            long size = sizeObj instanceof Number ? ((Number) sizeObj).longValue() : 0;
            Span sizeSpan = new Span(formatFileSize(size));
            sizeSpan.getStyle()
                .set("color", "#64748b")
                .set("font-size", "0.8rem")
                .set("display", "block");
            nameSection.add(nameSpan, sizeSpan);
        } else {
            nameSection.add(nameSpan);
        }

        header.add(fileIcon, nameSection);
        card.add(header);

        // Actions
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        actions.setWidthFull();

        String storedPath = (String) doc.get("storedPath");
        String base64Content = (String) doc.get("base64Content");

        if (storedPath != null && !storedPath.isEmpty()) {
            String encodedPath = URLEncoder.encode(storedPath, StandardCharsets.UTF_8);

            Anchor viewLink = new Anchor("/api/files/view?path=" + encodedPath, "");
            viewLink.setTarget("_blank");
            Button viewBtn = new Button("Voir", VaadinIcon.EYE.create());
            viewBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            viewLink.add(viewBtn);

            Anchor downloadLink = new Anchor("/api/files/download?path=" + encodedPath, "");
            downloadLink.getElement().setAttribute("download", "");
            Button downloadBtn = new Button("Télécharger", VaadinIcon.DOWNLOAD.create());
            downloadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            downloadLink.add(downloadBtn);

            actions.add(viewLink, downloadLink);
        } else if (base64Content != null && !base64Content.isEmpty()) {
            String contentType = (String) doc.get("contentType");
            if (contentType == null) contentType = "application/octet-stream";

            String dataUrl = "data:" + contentType + ";base64," + base64Content;

            Anchor downloadLink = new Anchor(dataUrl, "");
            downloadLink.getElement().setAttribute("download", filename);
            Button downloadBtn = new Button("Télécharger", VaadinIcon.DOWNLOAD.create());
            downloadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            downloadLink.add(downloadBtn);

            actions.add(downloadLink);
        } else {
            Span noFile = new Span("Fichier non disponible");
            noFile.getStyle().set("color", "#94a3b8").set("font-size", "0.85rem");
            actions.add(noFile);
        }

        card.add(actions);
        return card;
    }

    private String getDocFilename(Map<String, Object> doc) {
        Object filename = doc.get("filename");
        if (filename != null) return filename.toString();
        filename = doc.get("fileName");
        if (filename != null) return filename.toString();
        filename = doc.get("name");
        if (filename != null) return filename.toString();
        return "document";
    }

    private Icon getFileIcon(String filename) {
        if (filename == null) return VaadinIcon.FILE.create();
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return VaadinIcon.FILE_TEXT.create();
        if (lower.endsWith(".doc") || lower.endsWith(".docx")) return VaadinIcon.FILE_TEXT_O.create();
        if (lower.endsWith(".xls") || lower.endsWith(".xlsx")) return VaadinIcon.FILE_TABLE.create();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif"))
            return VaadinIcon.FILE_PICTURE.create();
        if (lower.endsWith(".zip") || lower.endsWith(".rar")) return VaadinIcon.FILE_ZIP.create();
        return VaadinIcon.FILE_O.create();
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024));
        return String.format("%.1f GB", size / (1024.0 * 1024 * 1024));
    }

    private Div createProcessingTab() {
        Div content = new Div();
        content.getStyle().set("padding", "1rem");

        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(true);

        // Current status display
        Div currentStatus = createSection("Statut actuel");
        Span statusBadge = new Span(demande.getStatut() != null ? demande.getStatut() : "SOUMISE");
        statusBadge.getStyle()
            .set("padding", "6px 12px")
            .set("border-radius", "16px")
            .set("font-weight", "500")
            .set("background", getStatusColor(demande.getStatut()))
            .set("color", "white");
        currentStatus.add(statusBadge);

        // Status change
        statusCombo = new ComboBox<>("Nouveau statut");
        statusCombo.setItems("SOUMISE", "EN_COURS", "ACCEPTEE", "REFUSEE", "TERMINEE");
        statusCombo.setValue(demande.getStatut() != null ? demande.getStatut() : "SOUMISE");
        statusCombo.setWidthFull();

        // Comment
        commentArea = new TextArea("Commentaire");
        commentArea.setValue(demande.getCommentaire() != null ? demande.getCommentaire() : "");
        commentArea.setWidthFull();
        commentArea.setHeight("120px");
        commentArea.setPlaceholder("Ajouter un commentaire sur le traitement...");

        // Update button
        Button updateBtn = new Button("Mettre à jour le statut");
        updateBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        updateBtn.addClickListener(e -> updateStatus());

        form.add(currentStatus, statusCombo, commentArea, updateBtn);
        content.add(form);
        return content;
    }

    private String getStatusColor(String status) {
        if (status == null) return "#6b7280";
        switch (status.toUpperCase()) {
            case "SOUMISE": return "#f59e0b";
            case "EN_COURS": return "#3b82f6";
            case "ACCEPTEE": return "#10b981";
            case "REFUSEE": return "#ef4444";
            case "TERMINEE": return "#6366f1";
            default: return "#6b7280";
        }
    }

    private void updateStatus() {
        String newStatus = statusCombo.getValue();
        String comment = commentArea.getValue();

        try {
            demandeService.updateStatut(demande.getId(), newStatus, comment, null);
            Notification.show("Statut mis à jour", 3000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            onUpdate.run();
            close();
        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage(), 5000, Notification.Position.TOP_END)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Div createSection(String title) {
        Div section = new Div();
        section.getStyle()
            .set("margin-bottom", "1.5rem")
            .set("background", "white")
            .set("border-radius", "8px")
            .set("padding", "1rem")
            .set("border", "1px solid #e2e8f0");

        H3 sectionTitle = new H3(title);
        sectionTitle.getStyle()
            .set("margin", "0 0 0.75rem 0")
            .set("font-size", "1rem")
            .set("color", "#374151");

        section.add(sectionTitle);
        return section;
    }

    private Div createInfoRow(String label, String value) {
        Div row = new Div();
        row.getStyle()
            .set("display", "flex")
            .set("justify-content", "space-between")
            .set("padding", "0.5rem 0")
            .set("border-bottom", "1px solid #f3f4f6");

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("color", "#6b7280");

        Span valueSpan = new Span(value != null ? value : "-");
        valueSpan.getStyle().set("font-weight", "500").set("color", "#1f2937");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private void createFooter() {
        Button receiptButton = new Button("Reçu", com.vaadin.flow.component.icon.VaadinIcon.PRINT.create());
        receiptButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        receiptButton.getStyle()
            .set("background", "#3b6b35")
            .set("color", "white");
        receiptButton.addClickListener(e -> {
            com.fosagri.application.views.adherent.DemandeRecuDialog recuDialog =
                new com.fosagri.application.views.adherent.DemandeRecuDialog(demande);
            recuDialog.open();
        });

        Button closeBtn = new Button("Fermer", e -> close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout footer = new HorizontalLayout(receiptButton, closeBtn);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();

        getFooter().add(footer);
    }
}
