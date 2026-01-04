package com.fosagri.application.views.adherent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.entities.DemandePrestation;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class DemandeDetailsDialog extends Dialog {

    private final DemandePrestation demande;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public DemandeDetailsDialog(DemandePrestation demande) {
        this.demande = demande;

        setHeaderTitle("Détails de la demande");
        setWidth("700px");
        setHeight("auto");
        setMaxHeight("80vh");
        setModal(true);
        setDraggable(true);
        setResizable(true);

        createContent();
        createFooter();
    }

    private void createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);

        // Prestation info
        layout.add(createPrestationSection());

        // Status info
        layout.add(createStatusSection());

        // Dates info
        layout.add(createDatesSection());

        // Commentaire if exists
        if (demande.getCommentaire() != null && !demande.getCommentaire().isEmpty()) {
            layout.add(createCommentaireSection());
        }

        // Form responses if exist
        if (demande.getReponseJson() != null && !demande.getReponseJson().isEmpty()) {
            layout.add(createFormResponsesSection());
        }

        // Attached documents if exist
        if (demande.getDocumentsJson() != null && !demande.getDocumentsJson().isEmpty()) {
            layout.add(createDocumentsSection());
        }

        add(layout);
    }

    private Div createPrestationSection() {
        Div section = new Div();
        section.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "1rem")
            .set("border-radius", "var(--lumo-border-radius)")
            .set("width", "100%");

        H3 title = new H3(demande.getPrestation() != null ? demande.getPrestation().getLabel() : "N/A");
        title.getStyle().set("margin", "0 0 0.5rem 0");

        if (demande.getPrestation() != null) {
            if (demande.getPrestation().getType() != null) {
                Span typeBadge = new Span(demande.getPrestation().getType());
                typeBadge.getStyle()
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("color", "var(--lumo-primary-text-color)")
                    .set("padding", "2px 8px")
                    .set("border-radius", "12px")
                    .set("font-size", "12px");
                section.add(title, typeBadge);
            } else {
                section.add(title);
            }

            if (demande.getPrestation().getDescription() != null) {
                Div desc = new Div();
                desc.setText(demande.getPrestation().getDescription());
                desc.getStyle()
                    .set("color", "var(--lumo-secondary-text-color)")
                    .set("margin-top", "0.5rem")
                    .set("font-size", "0.9rem");
                section.add(desc);
            }
        } else {
            section.add(title);
        }

        return section;
    }

    private Div createStatusSection() {
        Div section = new Div();
        section.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "1rem")
            .set("margin-top", "1rem");

        Div label = new Div();
        label.setText("Statut:");
        label.getStyle().set("font-weight", "600");

        Span badge = createStatutBadge(demande.getStatut());

        section.add(label, badge);
        return section;
    }

    private Div createDatesSection() {
        Div section = new Div();
        section.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(200px, 1fr))")
            .set("gap", "1rem")
            .set("margin-top", "1rem");

        section.add(createDateCard("Date de demande",
            demande.getDateDemande() != null ? sdf.format(demande.getDateDemande()) : "-"));

        section.add(createDateCard("Date de traitement",
            demande.getDateTraitement() != null ? sdf.format(demande.getDateTraitement()) : "-"));

        section.add(createDateCard("Date de finalisation",
            demande.getDateFinalisation() != null ? sdf.format(demande.getDateFinalisation()) : "-"));

        return section;
    }

    private Div createDateCard(String label, String value) {
        Div card = new Div();
        card.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "0.75rem")
            .set("border-radius", "var(--lumo-border-radius)");

        Div labelDiv = new Div();
        labelDiv.setText(label);
        labelDiv.getStyle()
            .set("font-size", "0.8rem")
            .set("color", "var(--lumo-secondary-text-color)");

        Div valueDiv = new Div();
        valueDiv.setText(value);
        valueDiv.getStyle()
            .set("font-weight", "600")
            .set("margin-top", "0.25rem");

        card.add(labelDiv, valueDiv);
        return card;
    }

    private Div createCommentaireSection() {
        Div section = new Div();
        section.getStyle().set("margin-top", "1rem");

        H4 title = new H4("Commentaire du traitement");
        title.getStyle().set("margin", "0 0 0.5rem 0");

        Div content = new Div();
        content.setText(demande.getCommentaire());
        content.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "1rem")
            .set("border-radius", "var(--lumo-border-radius)")
            .set("white-space", "pre-wrap");

        section.add(title, content);
        return section;
    }

    private Div createFormResponsesSection() {
        Div section = new Div();
        section.getStyle().set("margin-top", "1rem");

        H4 title = new H4("Réponses du formulaire");
        title.getStyle().set("margin", "0 0 0.5rem 0");

        Div content = new Div();
        content.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "1rem")
            .set("border-radius", "var(--lumo-border-radius)");

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> responses = mapper.readValue(
                demande.getReponseJson(),
                new TypeReference<Map<String, Object>>() {}
            );

            for (Map.Entry<String, Object> entry : responses.entrySet()) {
                Div row = new Div();
                row.getStyle()
                    .set("display", "flex")
                    .set("padding", "0.5rem 0")
                    .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

                Div keyDiv = new Div();
                keyDiv.setText(formatFieldName(entry.getKey()) + ":");
                keyDiv.getStyle()
                    .set("font-weight", "600")
                    .set("min-width", "150px")
                    .set("color", "var(--lumo-secondary-text-color)");

                Div valueDiv = new Div();
                valueDiv.setText(formatValue(entry.getValue()));
                valueDiv.getStyle().set("flex", "1");

                row.add(keyDiv, valueDiv);
                content.add(row);
            }
        } catch (Exception e) {
            Div errorDiv = new Div();
            errorDiv.setText("Impossible de charger les réponses");
            errorDiv.getStyle().set("color", "var(--lumo-error-text-color)");
            content.add(errorDiv);
        }

        section.add(title, content);
        return section;
    }

    private String formatFieldName(String fieldName) {
        if (fieldName == null) return "";
        // Convert camelCase or snake_case to readable format
        return fieldName
            .replaceAll("([a-z])([A-Z])", "$1 $2")
            .replaceAll("_", " ")
            .substring(0, 1).toUpperCase() + fieldName.substring(1)
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("_", " ");
    }

    private String formatValue(Object value) {
        if (value == null) return "-";
        if (value instanceof java.util.List) {
            java.util.List<?> list = (java.util.List<?>) value;
            if (list.isEmpty()) return "-";
            if (list.get(0) instanceof Map) {
                // File metadata
                StringBuilder sb = new StringBuilder();
                for (Object item : list) {
                    if (item instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) item;
                        sb.append(map.get("filename")).append(", ");
                    }
                }
                return sb.length() > 0 ? sb.substring(0, sb.length() - 2) : "-";
            }
            return String.join(", ", list.stream().map(Object::toString).toArray(String[]::new));
        }
        return value.toString();
    }

    private Span createStatutBadge(String statut) {
        String displayStatut = statut != null ? statut : "SOUMISE";
        Span badge = new Span(getStatutLabel(displayStatut));
        badge.getStyle()
            .set("padding", "6px 16px")
            .set("border-radius", "16px")
            .set("font-size", "14px")
            .set("font-weight", "600");

        switch (displayStatut.toUpperCase()) {
            case "SOUMISE":
                badge.getStyle().set("background-color", "#fff3cd").set("color", "#856404");
                break;
            case "EN_COURS":
                badge.getStyle().set("background-color", "#d1ecf1").set("color", "#0c5460");
                break;
            case "ACCEPTEE":
                badge.getStyle().set("background-color", "#d4edda").set("color", "#155724");
                break;
            case "REFUSEE":
                badge.getStyle().set("background-color", "#f8d7da").set("color", "#721c24");
                break;
            case "TERMINEE":
                badge.getStyle().set("background-color", "#e2e3e5").set("color", "#383d41");
                break;
            default:
                badge.getStyle().set("background-color", "#e2e3e5").set("color", "#383d41");
        }

        return badge;
    }

    private String getStatutLabel(String statut) {
        switch (statut.toUpperCase()) {
            case "SOUMISE": return "Soumise";
            case "EN_COURS": return "En cours de traitement";
            case "ACCEPTEE": return "Acceptée";
            case "REFUSEE": return "Refusée";
            case "TERMINEE": return "Terminée";
            default: return statut;
        }
    }

    private Div createDocumentsSection() {
        Div section = new Div();
        section.getStyle().set("margin-top", "1rem");

        H4 title = new H4("Pièces jointes");
        title.getStyle().set("margin", "0 0 0.5rem 0");

        Div content = new Div();
        content.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "1rem")
            .set("border-radius", "var(--lumo-border-radius)");

        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> documents = mapper.readValue(
                demande.getDocumentsJson(),
                new TypeReference<List<Map<String, Object>>>() {}
            );

            if (documents.isEmpty()) {
                Div emptyDiv = new Div();
                emptyDiv.setText("Aucune pièce jointe");
                emptyDiv.getStyle().set("color", "var(--lumo-secondary-text-color)");
                content.add(emptyDiv);
            } else {
                for (Map<String, Object> doc : documents) {
                    content.add(createDocumentRow(doc));
                }
            }
        } catch (Exception e) {
            Div errorDiv = new Div();
            errorDiv.setText("Impossible de charger les pièces jointes");
            errorDiv.getStyle().set("color", "var(--lumo-error-text-color)");
            content.add(errorDiv);
        }

        section.add(title, content);
        return section;
    }

    private Div createDocumentRow(Map<String, Object> doc) {
        Div row = new Div();
        row.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between")
            .set("padding", "0.75rem")
            .set("margin-bottom", "0.5rem")
            .set("background-color", "white")
            .set("border-radius", "var(--lumo-border-radius)")
            .set("border", "1px solid var(--lumo-contrast-10pct)");

        // File info
        Div fileInfo = new Div();
        fileInfo.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.75rem");

        // File icon
        Span icon = new Span();
        String filename = getDocFilename(doc);
        icon.add(getFileIcon(filename));
        fileInfo.add(icon);

        // Name and size
        Div nameAndSize = new Div();
        Span nameSpan = new Span(filename);
        nameSpan.getStyle().set("font-weight", "500");

        Object sizeObj = doc.get("size");
        if (sizeObj != null) {
            long size = sizeObj instanceof Number ? ((Number) sizeObj).longValue() : 0;
            Span sizeSpan = new Span(" (" + formatFileSize(size) + ")");
            sizeSpan.getStyle()
                .set("color", "var(--lumo-secondary-text-color)")
                .set("font-size", "0.85rem");
            nameAndSize.add(nameSpan, sizeSpan);
        } else {
            nameAndSize.add(nameSpan);
        }

        fileInfo.add(nameAndSize);

        // Download button
        Div actions = new Div();
        actions.getStyle().set("display", "flex").set("gap", "0.5rem");

        String storedPath = (String) doc.get("storedPath");
        if (storedPath != null && !storedPath.isEmpty()) {
            // File stored on disk - create download link
            String encodedPath = URLEncoder.encode(storedPath, StandardCharsets.UTF_8);

            Anchor viewLink = new Anchor("/api/files/view?path=" + encodedPath, "");
            viewLink.setTarget("_blank");
            Button viewBtn = new Button(VaadinIcon.EYE.create());
            viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            viewBtn.getElement().setAttribute("title", "Voir");
            viewLink.add(viewBtn);

            Anchor downloadLink = new Anchor("/api/files/download?path=" + encodedPath, "");
            downloadLink.getElement().setAttribute("download", "");
            Button downloadBtn = new Button(VaadinIcon.DOWNLOAD.create());
            downloadBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            downloadBtn.getElement().setAttribute("title", "Télécharger");
            downloadLink.add(downloadBtn);

            actions.add(viewLink, downloadLink);
        } else {
            // File stored in database (legacy) - show info only
            Span infoSpan = new Span("Fichier disponible");
            infoSpan.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "var(--lumo-secondary-text-color)");
            actions.add(infoSpan);
        }

        row.add(fileInfo, actions);
        return row;
    }

    private String getDocFilename(Map<String, Object> doc) {
        String filename = (String) doc.get("originalFilename");
        if (filename == null) {
            filename = (String) doc.get("storedFilename");
        }
        if (filename == null) {
            filename = (String) doc.get("filename");
        }
        return filename != null ? filename : "Fichier";
    }

    private com.vaadin.flow.component.icon.Icon getFileIcon(String filename) {
        if (filename == null) return VaadinIcon.FILE_O.create();

        String ext = "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            ext = filename.substring(lastDot + 1).toLowerCase();
        }

        switch (ext) {
            case "pdf":
                return VaadinIcon.FILE_TEXT.create();
            case "doc":
            case "docx":
                return VaadinIcon.FILE_TEXT_O.create();
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                return VaadinIcon.FILE_PICTURE.create();
            default:
                return VaadinIcon.FILE_O.create();
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void createFooter() {
        Button receiptButton = new Button("Reçu", VaadinIcon.PRINT.create());
        receiptButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        receiptButton.getStyle()
            .set("background", "#3b6b35")
            .set("color", "white");
        receiptButton.addClickListener(e -> {
            DemandeRecuDialog recuDialog = new DemandeRecuDialog(demande);
            recuDialog.open();
        });

        Button closeButton = new Button("Fermer", e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout footer = new HorizontalLayout(receiptButton, closeButton);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();
        footer.getStyle().set("padding-top", "1rem");

        getFooter().add(footer);
    }
}
