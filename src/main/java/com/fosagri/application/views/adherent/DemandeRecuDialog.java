package com.fosagri.application.views.adherent;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.entities.DemandePrestation;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * Receipt dialog for demande prestation with header and footer images
 */
public class DemandeRecuDialog extends Dialog {

    private final DemandePrestation demande;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    private Div receiptContent;

    public DemandeRecuDialog(DemandePrestation demande) {
        this.demande = demande;

        setHeaderTitle("Reçu de la demande");
        setWidth("800px");
        setHeight("auto");
        setMaxHeight("90vh");
        setModal(true);
        setDraggable(true);
        setResizable(true);

        createContent();
        createFooter();
    }

    private void createContent() {
        receiptContent = new Div();
        receiptContent.setId("receipt-content");
        receiptContent.getStyle()
            .set("background", "white")
            .set("padding", "0")
            .set("font-family", "Arial, sans-serif");

        // Header Image
        receiptContent.add(createHeaderSection());

        // Main Content
        receiptContent.add(createMainContent());

        // Footer Image
        receiptContent.add(createFooterSection());

        // Add print styles
        addPrintStyles();

        add(receiptContent);
    }

    private Div createHeaderSection() {
        Div headerSection = new Div();
        headerSection.getStyle()
            .set("width", "100%")
            .set("text-align", "center");

        Image headerImage = new Image("images/header.png", "FOS-AGRI Header");
        headerImage.setWidth("100%");
        headerImage.getStyle()
            .set("max-height", "80px")
            .set("object-fit", "contain");

        headerSection.add(headerImage);
        return headerSection;
    }

    private Div createMainContent() {
        Div mainContent = new Div();
        mainContent.getStyle()
            .set("padding", "0.75rem 1.5rem")
            .set("font-size", "0.85rem");

        // Title and Reference in one line
        Div headerRow = new Div();
        headerRow.getStyle()
            .set("display", "flex")
            .set("justify-content", "space-between")
            .set("align-items", "center")
            .set("border-bottom", "2px solid #3b6b35")
            .set("padding-bottom", "0.4rem")
            .set("margin-bottom", "0.75rem");

        Span title = new Span("RECU DE DEMANDE DE PRESTATION");
        title.getStyle()
            .set("color", "#2c5aa0")
            .set("font-size", "1rem")
            .set("font-weight", "bold");

        Span refValue = new Span("Réf: DEM-" + String.format("%06d", demande.getId()));
        refValue.getStyle()
            .set("font-family", "monospace")
            .set("font-size", "0.9rem")
            .set("color", "#2c5aa0")
            .set("font-weight", "bold");

        headerRow.add(title, refValue);
        mainContent.add(headerRow);

        // Compact two-column layout for Demandeur and Prestation
        Div topSection = new Div();
        topSection.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "1fr 1fr")
            .set("gap", "0.75rem")
            .set("margin-bottom", "0.5rem");

        topSection.add(createDemandeurSection());
        topSection.add(createPrestationSection());
        mainContent.add(topSection);

        // Dates and Status in one row
        Div bottomSection = new Div();
        bottomSection.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "2fr 1fr")
            .set("gap", "0.75rem")
            .set("margin-bottom", "0.5rem");

        bottomSection.add(createDatesSection());
        bottomSection.add(createStatusSection());
        mainContent.add(bottomSection);

        // Form Responses (if any) - compact
        if (demande.getReponseJson() != null && !demande.getReponseJson().isEmpty()) {
            mainContent.add(createFormResponsesSection());
        }

        // Signature area - compact
        mainContent.add(createSignatureSection());

        return mainContent;
    }

    private Div createRefSection() {
        Div refSection = new Div();
        refSection.getStyle()
            .set("text-align", "right")
            .set("margin-bottom", "1rem")
            .set("padding", "0.5rem")
            .set("background", "#f8f9fa")
            .set("border-radius", "4px");

        Span refLabel = new Span("N° de référence: ");
        refLabel.getStyle().set("font-weight", "bold");

        Span refValue = new Span("DEM-" + String.format("%06d", demande.getId()));
        refValue.getStyle()
            .set("font-family", "monospace")
            .set("font-size", "1.1rem")
            .set("color", "#2c5aa0");

        refSection.add(refLabel, refValue);
        return refSection;
    }

    private Div createDemandeurSection() {
        Div section = createSection("DEMANDEUR");

        Div content = new Div();
        content.getStyle()
            .set("padding", "0.4rem")
            .set("background", "#fafafa")
            .set("border-radius", "4px")
            .set("font-size", "0.8rem");

        if (demande.getAgent() != null) {
            content.add(createCompactInfoRow("Nom",
                demande.getAgent().getNOM_AG() + " " + demande.getAgent().getPR_AG()));
            content.add(createCompactInfoRow("ID",
                demande.getAgent().getIdAdh() != null ? demande.getAgent().getIdAdh() : "-"));
            content.add(createCompactInfoRow("CIN",
                demande.getAgent().getCIN_AG() != null ? demande.getAgent().getCIN_AG() : "-"));
        }

        section.add(content);
        return section;
    }

    private Div createPrestationSection() {
        Div section = createSection("PRESTATION");

        Div content = new Div();
        content.getStyle()
            .set("padding", "0.4rem")
            .set("background", "#fafafa")
            .set("border-radius", "4px")
            .set("font-size", "0.8rem");

        if (demande.getPrestation() != null) {
            Div prestationName = new Div();
            prestationName.setText(demande.getPrestation().getLabel());
            prestationName.getStyle()
                .set("font-size", "0.85rem")
                .set("font-weight", "600")
                .set("color", "#2c5aa0")
                .set("margin-bottom", "0.25rem");
            content.add(prestationName);

            if (demande.getPrestation().getType() != null) {
                Span typeBadge = new Span(demande.getPrestation().getType());
                typeBadge.getStyle()
                    .set("background", "#e8f5e9")
                    .set("color", "#2e7d32")
                    .set("padding", "1px 6px")
                    .set("border-radius", "8px")
                    .set("font-size", "0.7rem");
                content.add(typeBadge);
            }
        }

        section.add(content);
        return section;
    }

    private Div createDatesSection() {
        Div section = createSection("DATES");

        Div content = new Div();
        content.getStyle()
            .set("display", "flex")
            .set("gap", "0.5rem")
            .set("flex-wrap", "wrap");

        content.add(createCompactDateBox("Soumission",
            demande.getDateDemande() != null ? sdfDate.format(demande.getDateDemande()) : "-",
            "#e3f2fd", "#1976d2"));

        content.add(createCompactDateBox("Traitement",
            demande.getDateTraitement() != null ? sdfDate.format(demande.getDateTraitement()) : "-",
            "#fff3e0", "#f57c00"));

        content.add(createCompactDateBox("Finalisation",
            demande.getDateFinalisation() != null ? sdfDate.format(demande.getDateFinalisation()) : "-",
            "#e8f5e9", "#388e3c"));

        section.add(content);
        return section;
    }

    private Div createCompactDateBox(String label, String value, String bgColor, String textColor) {
        Div box = new Div();
        box.getStyle()
            .set("background", bgColor)
            .set("padding", "0.3rem 0.5rem")
            .set("border-radius", "4px")
            .set("text-align", "center")
            .set("flex", "1")
            .set("min-width", "80px");

        Span labelSpan = new Span(label + ": ");
        labelSpan.getStyle()
            .set("font-size", "0.7rem")
            .set("color", "#666");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-weight", "600")
            .set("color", textColor)
            .set("font-size", "0.75rem");

        box.add(labelSpan, valueSpan);
        return box;
    }

    private Div createCompactInfoRow(String label, String value) {
        Div row = new Div();
        row.getStyle()
            .set("display", "flex")
            .set("padding", "0.15rem 0")
            .set("border-bottom", "1px solid #eee");

        Span labelSpan = new Span(label + ": ");
        labelSpan.getStyle()
            .set("font-weight", "600")
            .set("min-width", "40px")
            .set("color", "#555")
            .set("font-size", "0.75rem");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("color", "#333")
            .set("font-size", "0.75rem");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private Div createStatusSection() {
        Div section = createSection("STATUT");

        Div content = new Div();
        content.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.5rem")
            .set("padding", "0.4rem")
            .set("background", getStatusBackground(demande.getStatut()))
            .set("border-radius", "4px")
            .set("border-left", "3px solid " + getStatusColor(demande.getStatut()));

        Span statusIcon = new Span(getStatusIcon(demande.getStatut()));
        statusIcon.getStyle().set("font-size", "1rem");

        Span statusLabel = new Span(getStatutLabel(demande.getStatut()));
        statusLabel.getStyle()
            .set("font-weight", "bold")
            .set("font-size", "0.8rem")
            .set("color", getStatusColor(demande.getStatut()));

        content.add(statusIcon, statusLabel);
        section.add(content);
        return section;
    }

    private Div createFormResponsesSection() {
        Div section = createSection("INFORMATIONS");

        Div content = new Div();
        content.getStyle()
            .set("padding", "0.3rem")
            .set("background", "#fafafa")
            .set("border-radius", "4px")
            .set("font-size", "0.75rem")
            .set("display", "grid")
            .set("grid-template-columns", "1fr 1fr")
            .set("gap", "0.2rem");

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
                    .set("padding", "0.1rem 0.2rem")
                    .set("background", "white")
                    .set("border-radius", "2px");

                Span keySpan = new Span(formatFieldName(entry.getKey()) + ": ");
                keySpan.getStyle()
                    .set("font-weight", "600")
                    .set("color", "#555")
                    .set("font-size", "0.7rem");

                Span valueSpan = new Span(formatValue(entry.getValue()));
                valueSpan.getStyle()
                    .set("color", "#333")
                    .set("font-size", "0.7rem");

                row.add(keySpan, valueSpan);
                content.add(row);
            }
        } catch (Exception e) {
            // Ignore parsing errors
        }

        section.add(content);
        return section;
    }

    private Div createSignatureSection() {
        Div section = new Div();
        section.getStyle()
            .set("margin-top", "0.75rem")
            .set("display", "flex")
            .set("justify-content", "space-between")
            .set("align-items", "flex-end")
            .set("padding-top", "0.5rem")
            .set("border-top", "1px dashed #ccc");

        // Date d'impression
        Div leftSide = new Div();
        leftSide.setText("Imprimé le: " + sdfDate.format(new java.util.Date()));
        leftSide.getStyle()
            .set("font-size", "0.7rem")
            .set("color", "#666");

        // Cachet et signature
        Div rightSide = new Div();
        rightSide.setText("Cachet et Signature");
        rightSide.getStyle()
            .set("text-align", "center")
            .set("border-top", "1px solid #999")
            .set("padding-top", "0.25rem")
            .set("min-width", "120px")
            .set("font-size", "0.7rem")
            .set("color", "#666");

        section.add(leftSide, rightSide);
        return section;
    }

    private Div createFooterSection() {
        Div footerSection = new Div();
        footerSection.getStyle()
            .set("width", "100%")
            .set("text-align", "center")
            .set("margin-top", "0.5rem");

        Image footerImage = new Image("images/footer.png", "FOS-AGRI Footer");
        footerImage.setWidth("100%");
        footerImage.getStyle()
            .set("max-height", "60px")
            .set("object-fit", "contain");

        footerSection.add(footerImage);
        return footerSection;
    }

    private Div createSection(String title) {
        Div section = new Div();
        section.getStyle().set("margin-bottom", "0");

        Div titleDiv = new Div();
        titleDiv.setText(title);
        titleDiv.getStyle()
            .set("font-weight", "bold")
            .set("font-size", "0.7rem")
            .set("color", "#3b6b35")
            .set("margin-bottom", "0.2rem")
            .set("text-transform", "uppercase")
            .set("letter-spacing", "0.5px");

        section.add(titleDiv);
        return section;
    }

    private Div createInfoRow(String label, String value) {
        Div row = new Div();

        Span labelSpan = new Span(label + ": ");
        labelSpan.getStyle()
            .set("font-weight", "600")
            .set("color", "#555");

        Span valueSpan = new Span(value);
        valueSpan.getStyle().set("color", "#333");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private String formatFieldName(String fieldName) {
        if (fieldName == null) return "";
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

    private String getStatutLabel(String statut) {
        if (statut == null) return "Soumise";
        switch (statut.toUpperCase()) {
            case "SOUMISE": return "Demande Soumise";
            case "EN_COURS": return "En cours de traitement";
            case "ACCEPTEE": return "Demande Acceptée";
            case "REFUSEE": return "Demande Refusée";
            case "TERMINEE": return "Demande Terminée";
            default: return statut;
        }
    }

    private String getStatusColor(String statut) {
        if (statut == null) return "#f57c00";
        switch (statut.toUpperCase()) {
            case "SOUMISE": return "#f57c00";
            case "EN_COURS": return "#1976d2";
            case "ACCEPTEE": return "#388e3c";
            case "REFUSEE": return "#d32f2f";
            case "TERMINEE": return "#616161";
            default: return "#616161";
        }
    }

    private String getStatusBackground(String statut) {
        if (statut == null) return "#fff3e0";
        switch (statut.toUpperCase()) {
            case "SOUMISE": return "#fff3e0";
            case "EN_COURS": return "#e3f2fd";
            case "ACCEPTEE": return "#e8f5e9";
            case "REFUSEE": return "#ffebee";
            case "TERMINEE": return "#f5f5f5";
            default: return "#f5f5f5";
        }
    }

    private String getStatusIcon(String statut) {
        if (statut == null) return "\u23F3"; // hourglass
        switch (statut.toUpperCase()) {
            case "SOUMISE": return "\u23F3"; // hourglass
            case "EN_COURS": return "\u2699"; // gear
            case "ACCEPTEE": return "\u2705"; // check
            case "REFUSEE": return "\u274C"; // x
            case "TERMINEE": return "\u2714"; // checkmark
            default: return "\u2139"; // info
        }
    }

    private void addPrintStyles() {
        String printStyles = """
            <style>
                @media print {
                    body * {
                        visibility: hidden;
                    }
                    #receipt-content, #receipt-content * {
                        visibility: visible;
                    }
                    #receipt-content {
                        position: absolute;
                        left: 0;
                        top: 0;
                        width: 100%;
                        padding: 0 !important;
                        margin: 0 !important;
                    }
                    vaadin-dialog-overlay {
                        display: none !important;
                    }
                    .no-print {
                        display: none !important;
                    }
                }
            </style>
            """;

        UI.getCurrent().getPage().executeJs(
            "if (!document.getElementById('receipt-print-styles')) {" +
            "  var style = document.createElement('style');" +
            "  style.id = 'receipt-print-styles';" +
            "  style.innerHTML = `" + printStyles.replace("`", "\\`") + "`;" +
            "  document.head.appendChild(style);" +
            "}"
        );
    }

    private void createFooter() {
        Button printButton = new Button("Imprimer", VaadinIcon.PRINT.create());
        printButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        printButton.getStyle()
            .set("background", "#3b6b35")
            .set("color", "white");
        printButton.addClickListener(e -> printReceipt());

        Button closeButton = new Button("Fermer", e -> close());
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout footer = new HorizontalLayout(printButton, closeButton);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setWidthFull();
        footer.getStyle().set("padding-top", "1rem");
        footer.addClassName("no-print");

        getFooter().add(footer);
    }

    private void printReceipt() {
        UI.getCurrent().getPage().executeJs(
            "setTimeout(function() {" +
            "  var content = document.getElementById('receipt-content');" +
            "  if (content) {" +
            "    var printWindow = window.open('', '_blank');" +
            "    printWindow.document.write('<html><head><title>Reçu de Demande</title>');" +
            "    printWindow.document.write('<style>');" +
            "    printWindow.document.write('body { font-family: Arial, sans-serif; margin: 0; padding: 0; }');" +
            "    printWindow.document.write('img { max-width: 100%; height: auto; }');" +
            "    printWindow.document.write('@page { size: A4; margin: 10mm; }');" +
            "    printWindow.document.write('</style>');" +
            "    printWindow.document.write('</head><body>');" +
            "    printWindow.document.write(content.innerHTML);" +
            "    printWindow.document.write('</body></html>');" +
            "    printWindow.document.close();" +
            "    printWindow.focus();" +
            "    setTimeout(function() { printWindow.print(); }, 500);" +
            "  }" +
            "}, 100);"
        );
    }
}
