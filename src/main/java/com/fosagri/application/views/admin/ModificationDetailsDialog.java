package com.fosagri.application.views.admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.dto.DocumentUpload;
import com.fosagri.application.dto.FieldChange;
import com.fosagri.application.entities.ModificationDemande;
import com.fosagri.application.entities.ModificationDemande.StatutModification;
import com.fosagri.application.security.AuthenticatedUser;
import com.fosagri.application.services.ModificationDemandeService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Image;
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
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ModificationDetailsDialog extends Dialog {

    private final ModificationDemande modification;
    private final ModificationDemandeService modificationService;
    private final Runnable onUpdate;
    private final ObjectMapper objectMapper;
    private TextArea commentField;

    {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public ModificationDetailsDialog(ModificationDemande modification,
                                      ModificationDemandeService modificationService,
                                      Runnable onUpdate) {
        this.modification = modification;
        this.modificationService = modificationService;
        this.onUpdate = onUpdate;

        setWidth("800px");
        setMaxHeight("90vh");
        setCloseOnOutsideClick(false);
        setCloseOnEsc(true);

        setHeaderTitle("Details de la modification #" + modification.getId());

        createContent();
        createFooter();
    }

    private void createContent() {
        TabSheet tabSheet = new TabSheet();
        tabSheet.setWidthFull();

        // Tab 1: Information
        Tab infoTab = new Tab(VaadinIcon.INFO_CIRCLE.create(), new Span("Informations"));
        VerticalLayout infoContent = createInfoTab();

        // Tab 2: Comparison
        Tab comparisonTab = new Tab(VaadinIcon.SPLIT.create(), new Span("Comparaison"));
        VerticalLayout comparisonContent = createComparisonTab();

        // Tab 3: Documents
        Tab documentsTab = new Tab(VaadinIcon.FILE_O.create(), new Span("Documents"));
        VerticalLayout documentsContent = createDocumentsTab();

        // Tab 4: Processing (only for pending)
        if (modification.getStatut() == StatutModification.EN_ATTENTE) {
            Tab processingTab = new Tab(VaadinIcon.CHECK_SQUARE.create(), new Span("Traitement"));
            VerticalLayout processingContent = createProcessingTab();

            tabSheet.add(infoTab, infoContent);
            tabSheet.add(comparisonTab, comparisonContent);
            tabSheet.add(documentsTab, documentsContent);
            tabSheet.add(processingTab, processingContent);
        } else {
            tabSheet.add(infoTab, infoContent);
            tabSheet.add(comparisonTab, comparisonContent);
            tabSheet.add(documentsTab, documentsContent);
        }

        add(tabSheet);
    }

    private VerticalLayout createInfoTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Agent info section
        H3 agentTitle = new H3("Agent demandeur");
        agentTitle.getStyle().set("margin", "0");

        Div agentInfo = new Div();
        agentInfo.getStyle()
            .set("background", "#f8fafc")
            .set("padding", "1rem")
            .set("border-radius", "8px")
            .set("margin-bottom", "1rem");

        if (modification.getAgent() != null) {
            agentInfo.add(
                createInfoRow("Nom complet", modification.getAgent().getNOM_AG() + " " + modification.getAgent().getPR_AG()),
                createInfoRow("ID Adherent", modification.getAgent().getIdAdh()),
                createInfoRow("CIN", modification.getAgent().getCIN_AG())
            );
        }

        // Modification info section
        H3 modTitle = new H3("Details de la modification");
        modTitle.getStyle().set("margin", "0");

        Div modInfo = new Div();
        modInfo.getStyle()
            .set("background", "#f8fafc")
            .set("padding", "1rem")
            .set("border-radius", "8px");

        modInfo.add(
            createInfoRow("Type d'action", createActionBadge(modification.isCreation())),
            createInfoRow("Type d'entite", modification.getTypeEntiteLabel()),
            createInfoRow("Entite", modification.getEntiteLabel()),
            createInfoRow("Statut", createStatusBadge(modification.getStatut())),
            createInfoRow("Date de demande", modification.getDateCreation() != null ? sdf.format(modification.getDateCreation()) : "-")
        );

        if (modification.getDateTraitement() != null) {
            modInfo.add(createInfoRow("Date de traitement", sdf.format(modification.getDateTraitement())));
        }

        if (modification.getCommentaireAdmin() != null && !modification.getCommentaireAdmin().isEmpty()) {
            modInfo.add(createInfoRow("Commentaire admin", modification.getCommentaireAdmin()));
        }

        layout.add(agentTitle, agentInfo, modTitle, modInfo);
        return layout;
    }

    private VerticalLayout createComparisonTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        boolean isCreation = modification.isCreation();
        H3 title = new H3(isCreation ? "Nouvelles valeurs" : "Comparaison des valeurs");
        title.getStyle().set("margin", "0");

        // Create comparison table
        Div comparisonTable = new Div();
        comparisonTable.getStyle()
            .set("width", "100%")
            .set("overflow-x", "auto");

        // Header row
        HorizontalLayout headerRow = new HorizontalLayout();
        headerRow.setWidthFull();
        headerRow.getStyle()
            .set("background", "#f1f5f9")
            .set("padding", "0.75rem")
            .set("font-weight", "600")
            .set("border-radius", "8px 8px 0 0");

        Span fieldHeader = new Span("Champ");
        fieldHeader.getStyle().set("flex", "1");

        if (isCreation) {
            // For creation, only show new values
            Span newHeader = new Span("Valeur");
            newHeader.getStyle().set("flex", "1");
            headerRow.add(fieldHeader, newHeader);
        } else {
            // For modification, show old and new values
            Span oldHeader = new Span("Valeur actuelle");
            oldHeader.getStyle().set("flex", "1");
            Span newHeader = new Span("Nouvelle valeur");
            newHeader.getStyle().set("flex", "1");
            headerRow.add(fieldHeader, oldHeader, newHeader);
        }

        comparisonTable.add(headerRow);

        // Data rows
        List<FieldChange> changes = modificationService.compareValues(
            modification.getAnciennesValeursJson(),
            modification.getNouvellesValeursJson()
        );

        if (changes.isEmpty()) {
            Div noChanges = new Div();
            noChanges.setText(isCreation ? "Aucune donnee trouvee" : "Aucune modification detectee");
            noChanges.getStyle()
                .set("padding", "2rem")
                .set("text-align", "center")
                .set("color", "#64748b");
            comparisonTable.add(noChanges);
        } else {
            for (FieldChange change : changes) {
                HorizontalLayout row = new HorizontalLayout();
                row.setWidthFull();
                row.getStyle()
                    .set("padding", "0.75rem")
                    .set("border-bottom", "1px solid #e2e8f0")
                    .set("align-items", "center");

                // Field name
                Span fieldName = new Span(change.getFieldLabel());
                fieldName.getStyle().set("flex", "1").set("font-weight", "500");

                if (isCreation) {
                    // For creation, only show new value
                    Div newValue = createValueCell(change.getNewValue(), "#dcfce7", change.isDocument());
                    newValue.getStyle().set("flex", "1");
                    row.add(fieldName, newValue);
                } else {
                    // For modification, show old and new values
                    Div oldValue = createValueCell(change.getOldValue(), "#fee2e2", change.isDocument());
                    oldValue.getStyle().set("flex", "1");

                    Div newValue = createValueCell(change.getNewValue(), "#dcfce7", change.isDocument());
                    newValue.getStyle().set("flex", "1");

                    row.add(fieldName, oldValue, newValue);
                }

                comparisonTable.add(row);
            }
        }

        layout.add(title, comparisonTable);
        return layout;
    }

    private Div createValueCell(Object value, String bgColor, boolean isDocument) {
        Div cell = new Div();
        cell.getStyle()
            .set("padding", "0.5rem")
            .set("border-radius", "4px")
            .set("background", bgColor)
            .set("min-height", "2rem");

        if (value == null || value.toString().isEmpty()) {
            cell.setText("-");
            cell.getStyle().set("color", "#94a3b8").set("font-style", "italic");
        } else if (isDocument) {
            Icon icon = VaadinIcon.FILE.create();
            icon.setSize("16px");
            Span text = new Span(" Document");
            HorizontalLayout docLayout = new HorizontalLayout(icon, text);
            docLayout.setAlignItems(FlexComponent.Alignment.CENTER);
            docLayout.setSpacing(false);
            cell.add(docLayout);
        } else {
            cell.setText(value.toString());
        }

        return cell;
    }

    private VerticalLayout createDocumentsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        H3 title = new H3("Documents joints");
        title.getStyle().set("margin", "0");

        // Fetch documents JSON
        String entityJson = modification.getDocumentsJson();
        String dbJson = modificationService.getDocumentsJsonById(modification.getId());
        String json = entityJson != null ? entityJson : dbJson;

        // Parse documents with flexible structure handling
        List<DocumentUpload> documents = parseDocumentsFlexible(json);

        if (documents.isEmpty()) {
            Div noDocuments = new Div();
            noDocuments.setText("Aucun document joint a cette demande");
            noDocuments.getStyle()
                .set("padding", "2rem")
                .set("text-align", "center")
                .set("color", "#64748b")
                .set("background", "#f8fafc")
                .set("border-radius", "8px");
            layout.add(title, noDocuments);
        } else {
            Div documentsGrid = new Div();
            documentsGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(auto-fill, minmax(250px, 1fr))")
                .set("gap", "1rem");

            for (DocumentUpload doc : documents) {
                Div docCard = createDocumentCard(doc);
                documentsGrid.add(docCard);
            }

            layout.add(title, documentsGrid);
        }

        return layout;
    }

    /**
     * Parse documents JSON with flexible structure handling.
     * Supports multiple formats:
     * 1. List<DocumentUpload> - direct array of documents
     * 2. Map<String, List<FileData>> - field name to array of files (DemandePrestationService format)
     */
    private List<DocumentUpload> parseDocumentsFlexible(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }

        List<DocumentUpload> result = new ArrayList<>();

        try {
            JsonNode rootNode = objectMapper.readTree(json);

            if (rootNode.isArray()) {
                // Format 1: Direct array of documents
                for (JsonNode docNode : rootNode) {
                    DocumentUpload doc = parseDocumentNode(docNode, null);
                    if (doc != null) {
                        result.add(doc);
                    }
                }
            } else if (rootNode.isObject()) {
                // Format 2: Object with field names as keys
                Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String fieldName = entry.getKey();
                    JsonNode valueNode = entry.getValue();

                    if (valueNode.isArray()) {
                        // Array of files for this field
                        for (JsonNode fileNode : valueNode) {
                            DocumentUpload doc = parseDocumentNode(fileNode, fieldName);
                            if (doc != null) {
                                result.add(doc);
                            }
                        }
                    } else if (valueNode.isObject()) {
                        // Single file object
                        DocumentUpload doc = parseDocumentNode(valueNode, fieldName);
                        if (doc != null) {
                            result.add(doc);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing documents JSON: " + e.getMessage());
        }

        return result;
    }

    /**
     * Parse a single document node into DocumentUpload
     */
    private DocumentUpload parseDocumentNode(JsonNode node, String defaultFieldName) {
        if (node == null || !node.isObject()) {
            return null;
        }

        try {
            DocumentUpload doc = new DocumentUpload();

            // Field name
            if (node.has("fieldName") && !node.get("fieldName").isNull()) {
                doc.setFieldName(node.get("fieldName").asText());
            } else {
                doc.setFieldName(defaultFieldName);
            }

            // Filename
            if (node.has("filename") && !node.get("filename").isNull()) {
                doc.setFilename(node.get("filename").asText());
            }

            // Content type
            if (node.has("contentType") && !node.get("contentType").isNull()) {
                doc.setContentType(node.get("contentType").asText());
            }

            // Size
            if (node.has("size") && !node.get("size").isNull()) {
                doc.setSize(node.get("size").asLong());
            }

            // Data - try multiple field names
            byte[] data = null;
            String[] dataFields = {"data", "base64Content", "content", "fileData"};
            for (String dataField : dataFields) {
                if (node.has(dataField) && !node.get(dataField).isNull()) {
                    JsonNode dataNode = node.get(dataField);
                    if (dataNode.isTextual()) {
                        String base64 = dataNode.asText();
                        // Remove data URL prefix if present
                        if (base64.contains(",")) {
                            base64 = base64.substring(base64.indexOf(",") + 1);
                        }
                        data = Base64.getDecoder().decode(base64);
                        break;
                    } else if (dataNode.isBinary()) {
                        data = dataNode.binaryValue();
                        break;
                    }
                }
            }
            doc.setData(data);

            // Only return if we have some useful data
            if (doc.getFilename() != null || doc.getFieldName() != null || data != null) {
                return doc;
            }
        } catch (Exception e) {
            System.err.println("Error parsing document node: " + e.getMessage());
        }

        return null;
    }

    private List<DocumentUpload> parseDocumentsWithFallback() {
        // Try entity first, then fetch from DB
        String documentsJson = modification.getDocumentsJson();
        if (documentsJson == null || documentsJson.isEmpty()) {
            documentsJson = modificationService.getDocumentsJsonById(modification.getId());
        }

        if (documentsJson == null || documentsJson.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(documentsJson, new TypeReference<List<DocumentUpload>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private Div createDocumentCard(DocumentUpload doc) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "8px")
            .set("padding", "1rem")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("align-items", "center");

        // Document label
        String fieldLabel = modificationService.getFieldLabel(doc.getFieldName());
        Span labelSpan = new Span(fieldLabel);
        labelSpan.getStyle()
            .set("font-weight", "600")
            .set("color", "#1e293b")
            .set("margin-bottom", "0.5rem");

        // Document preview or icon
        Div previewContainer = new Div();
        previewContainer.getStyle()
            .set("width", "100%")
            .set("min-height", "120px")
            .set("display", "flex")
            .set("justify-content", "center")
            .set("align-items", "center")
            .set("background", "#f8fafc")
            .set("border-radius", "4px")
            .set("margin-bottom", "0.5rem");

        if (doc.getData() != null && doc.getData().length > 0) {
            String contentType = doc.getContentType();
            if (contentType != null && contentType.startsWith("image/")) {
                // Show image preview
                StreamResource resource = new StreamResource(
                    doc.getFilename() != null ? doc.getFilename() : "image",
                    () -> new ByteArrayInputStream(doc.getData())
                );
                Image preview = new Image(resource, doc.getFilename());
                preview.setMaxHeight("150px");
                preview.setMaxWidth("100%");
                preview.getStyle().set("object-fit", "contain").set("border-radius", "4px");
                previewContainer.add(preview);
            } else {
                // Show file icon for PDFs and other files
                Icon fileIcon = VaadinIcon.FILE_TEXT_O.create();
                fileIcon.setSize("48px");
                fileIcon.getStyle().set("color", "#64748b");
                previewContainer.add(fileIcon);
            }

            // Download button
            StreamResource downloadResource = new StreamResource(
                doc.getFilename() != null ? doc.getFilename() : "document",
                () -> new ByteArrayInputStream(doc.getData())
            );
            Anchor downloadLink = new Anchor(downloadResource, "");
            downloadLink.getElement().setAttribute("download", true);

            Button downloadBtn = new Button("Telecharger", VaadinIcon.DOWNLOAD.create());
            downloadBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            downloadLink.add(downloadBtn);

            // File info
            Span filenameSpan = new Span(doc.getFilename() != null ? doc.getFilename() : "Document");
            filenameSpan.getStyle()
                .set("font-size", "0.85rem")
                .set("color", "#64748b")
                .set("margin-bottom", "0.5rem")
                .set("word-break", "break-all")
                .set("text-align", "center");

            Span sizeSpan = new Span(formatFileSize(doc.getSize()));
            sizeSpan.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "#94a3b8")
                .set("margin-bottom", "0.5rem");

            card.add(labelSpan, previewContainer, filenameSpan, sizeSpan, downloadLink);
        } else {
            Icon noFileIcon = VaadinIcon.FILE_REMOVE.create();
            noFileIcon.setSize("48px");
            noFileIcon.getStyle().set("color", "#cbd5e1");
            previewContainer.add(noFileIcon);

            Span noDataSpan = new Span("Aucune donnee");
            noDataSpan.getStyle().set("color", "#94a3b8").set("font-style", "italic");

            card.add(labelSpan, previewContainer, noDataSpan);
        }

        return card;
    }

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private VerticalLayout createProcessingTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        H3 title = new H3("Traitement de la demande");
        title.getStyle().set("margin", "0");

        // Current status display
        Div currentStatus = new Div();
        currentStatus.getStyle()
            .set("background", "#fef3c7")
            .set("padding", "1rem")
            .set("border-radius", "8px")
            .set("margin-bottom", "1rem");

        HorizontalLayout statusRow = new HorizontalLayout();
        statusRow.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon clockIcon = VaadinIcon.CLOCK.create();
        clockIcon.getStyle().set("color", "#92400e");
        Span statusText = new Span("Cette modification est en attente de validation");
        statusText.getStyle().set("color", "#92400e").set("font-weight", "500");
        statusRow.add(clockIcon, statusText);
        currentStatus.add(statusRow);

        // Comment field
        commentField = new TextArea("Commentaire (optionnel)");
        commentField.setWidthFull();
        commentField.setPlaceholder("Ajouter un commentaire pour l'adherent...");
        commentField.setHeight("100px");

        // Action buttons
        HorizontalLayout actionButtons = new HorizontalLayout();
        actionButtons.setSpacing(true);
        actionButtons.getStyle().set("margin-top", "1rem");

        Button approveBtn = new Button("Valider la modification", VaadinIcon.CHECK.create());
        approveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);
        approveBtn.addClickListener(e -> approveModification());

        Button rejectBtn = new Button("Refuser la modification", VaadinIcon.CLOSE.create());
        rejectBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);
        rejectBtn.addClickListener(e -> rejectModification());

        actionButtons.add(approveBtn, rejectBtn);

        layout.add(title, currentStatus, commentField, actionButtons);
        return layout;
    }

    private void createFooter() {
        Button closeBtn = new Button("Fermer", e -> close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getFooter().add(closeBtn);
    }

    private HorizontalLayout createInfoRow(String label, String value) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle()
            .set("font-weight", "500")
            .set("color", "#64748b")
            .set("min-width", "150px");

        Span valueSpan = new Span(value != null ? value : "-");
        valueSpan.getStyle().set("color", "#1e293b");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private HorizontalLayout createInfoRow(String label, Span valueBadge) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setSpacing(true);

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle()
            .set("font-weight", "500")
            .set("color", "#64748b")
            .set("min-width", "150px");

        row.add(labelSpan, valueBadge);
        return row;
    }

    private Span createStatusBadge(StatutModification status) {
        String label = "";
        String bgColor = "";
        String textColor = "";

        if (status != null) {
            switch (status) {
                case EN_ATTENTE:
                    label = "En attente";
                    bgColor = "#fef3c7";
                    textColor = "#92400e";
                    break;
                case VALIDE:
                    label = "Validee";
                    bgColor = "#dcfce7";
                    textColor = "#166534";
                    break;
                case REFUSE:
                    label = "Refusee";
                    bgColor = "#fee2e2";
                    textColor = "#991b1b";
                    break;
            }
        } else {
            label = "-";
            bgColor = "#f3f4f6";
            textColor = "#374151";
        }

        Span badge = new Span(label);
        badge.getStyle()
            .set("padding", "4px 10px")
            .set("border-radius", "12px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500")
            .set("background", bgColor)
            .set("color", textColor);

        return badge;
    }

    private Span createActionBadge(boolean isCreation) {
        String label = isCreation ? "Creation" : "Modification";
        String bgColor = isCreation ? "#d1fae5" : "#e0f2fe";
        String textColor = isCreation ? "#059669" : "#0284c7";

        Span badge = new Span(label);
        badge.getStyle()
            .set("padding", "4px 10px")
            .set("border-radius", "12px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500")
            .set("background", bgColor)
            .set("color", textColor);

        return badge;
    }

    private void approveModification() {
        try {
            // Get current admin user ID (simplified - you may need to inject AuthenticatedUser)
            Long adminUserId = 1L; // TODO: Get from AuthenticatedUser

            String comment = commentField.getValue();
            modificationService.approveModification(modification.getId(), adminUserId, comment);

            showSuccessNotification("Modification validee avec succes!");
            close();
            if (onUpdate != null) {
                onUpdate.run();
            }
        } catch (Exception e) {
            showErrorNotification("Erreur: " + e.getMessage());
        }
    }

    private void rejectModification() {
        String comment = commentField.getValue();
        if (comment == null || comment.trim().isEmpty()) {
            showErrorNotification("Veuillez indiquer un motif de refus");
            return;
        }

        try {
            // Get current admin user ID
            Long adminUserId = 1L; // TODO: Get from AuthenticatedUser

            modificationService.rejectModification(modification.getId(), adminUserId, comment);

            showSuccessNotification("Modification refusee");
            close();
            if (onUpdate != null) {
                onUpdate.run();
            }
        } catch (Exception e) {
            showErrorNotification("Erreur: " + e.getMessage());
        }
    }

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
