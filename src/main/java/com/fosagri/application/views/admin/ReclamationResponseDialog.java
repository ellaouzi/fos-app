package com.fosagri.application.views.admin;

import com.fosagri.application.entities.Reclamation;
import com.fosagri.application.services.ReclamationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextArea;

import java.text.SimpleDateFormat;

public class ReclamationResponseDialog extends Dialog {

    private final Reclamation reclamation;
    private final ReclamationService reclamationService;
    private final Runnable onUpdate;
    private ComboBox<Reclamation.StatutReclamation> statusCombo;
    private TextArea responseArea;

    public ReclamationResponseDialog(Reclamation reclamation, ReclamationService reclamationService, Runnable onUpdate) {
        this.reclamation = reclamation;
        this.reclamationService = reclamationService;
        this.onUpdate = onUpdate;

        setHeaderTitle("Réclamation #" + reclamation.getId());
        setWidth("750px");
        setHeight("580px");

        createContent();
        createFooter();
    }

    private void createContent() {
        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();

        tabSheet.add("Détails", createDetailsTab());
        tabSheet.add("Répondre", createResponseTab());

        add(tabSheet);
    }

    private Div createDetailsTab() {
        Div content = new Div();
        content.getStyle().set("padding", "1rem").set("overflow-y", "auto");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Agent info
        Div agentSection = createSection("Informations Agent");
        String agentName = "-";
        try {
            agentName = reclamationService.getAgentNameForReclamation(reclamation.getId());
        } catch (Exception e) {
            // Ignore
        }
        agentSection.add(createInfoRow("Agent", agentName));

        // Reclamation info
        Div reclamationSection = createSection("Réclamation");
        reclamationSection.add(createInfoRow("Objet", reclamation.getObjet()));
        reclamationSection.add(createInfoRow("Type", reclamation.getType() != null ? reclamation.getType().getLabel() : "-"));
        reclamationSection.add(createInfoRow("Priorité", reclamation.getPriorite() != null ? reclamation.getPriorite().getLabel() : "Normale"));
        reclamationSection.add(createInfoRow("Statut", reclamation.getStatut() != null ? reclamation.getStatut().getLabel() : "-"));
        reclamationSection.add(createInfoRow("Date création", reclamation.getDateCreation() != null ? sdf.format(reclamation.getDateCreation()) : "-"));
        reclamationSection.add(createInfoRow("Date traitement", reclamation.getDateTraitement() != null ? sdf.format(reclamation.getDateTraitement()) : "-"));

        // Detail section
        Div detailSection = createSection("Description détaillée");
        Div detailContent = new Div();
        detailContent.setText(reclamation.getDetail() != null ? reclamation.getDetail() : "Aucune description");
        detailContent.getStyle()
            .set("background", "#f8fafc")
            .set("padding", "1rem")
            .set("border-radius", "8px")
            .set("white-space", "pre-wrap")
            .set("max-height", "150px")
            .set("overflow-y", "auto");
        detailSection.add(detailContent);

        // Previous response if any
        if (reclamation.getReponseOrganisation() != null && !reclamation.getReponseOrganisation().isEmpty()) {
            Div responseSection = createSection("Réponse précédente");
            Div responseContent = new Div();
            responseContent.setText(reclamation.getReponseOrganisation());
            responseContent.getStyle()
                .set("background", "#dcfce7")
                .set("padding", "1rem")
                .set("border-radius", "8px")
                .set("white-space", "pre-wrap");
            responseSection.add(responseContent);
            content.add(agentSection, reclamationSection, detailSection, responseSection);
        } else {
            content.add(agentSection, reclamationSection, detailSection);
        }

        return content;
    }

    private Div createResponseTab() {
        Div content = new Div();
        content.getStyle().set("padding", "1rem");

        VerticalLayout form = new VerticalLayout();
        form.setPadding(false);
        form.setSpacing(true);

        // Current status
        Div statusInfo = new Div();
        statusInfo.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.5rem")
            .set("margin-bottom", "1rem");

        Span statusLabel = new Span("Statut actuel: ");
        statusLabel.getStyle().set("color", "#64748b");

        Span statusBadge = new Span(reclamation.getStatut() != null ? reclamation.getStatut().getLabel() : "Nouvelle");
        statusBadge.getStyle()
            .set("padding", "4px 12px")
            .set("border-radius", "12px")
            .set("font-weight", "500")
            .set("background", getStatusColor(reclamation.getStatut()))
            .set("color", "white");

        statusInfo.add(statusLabel, statusBadge);

        // New status
        statusCombo = new ComboBox<>("Nouveau statut");
        statusCombo.setItems(Reclamation.StatutReclamation.values());
        statusCombo.setItemLabelGenerator(Reclamation.StatutReclamation::getLabel);
        statusCombo.setValue(reclamation.getStatut() != null ? reclamation.getStatut() : Reclamation.StatutReclamation.NOUVELLE);
        statusCombo.setWidthFull();

        // Response
        responseArea = new TextArea("Réponse de l'organisation");
        responseArea.setValue(reclamation.getReponseOrganisation() != null ? reclamation.getReponseOrganisation() : "");
        responseArea.setWidthFull();
        responseArea.setHeight("180px");
        responseArea.setPlaceholder("Saisissez votre réponse à cette réclamation...");

        // Buttons
        Button updateStatusBtn = new Button("Mettre à jour le statut");
        updateStatusBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        updateStatusBtn.addClickListener(e -> updateStatus());

        Button saveResponseBtn = new Button("Enregistrer la réponse");
        saveResponseBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveResponseBtn.addClickListener(e -> saveResponse());

        Div buttonRow = new Div(updateStatusBtn, saveResponseBtn);
        buttonRow.getStyle()
            .set("display", "flex")
            .set("gap", "1rem")
            .set("margin-top", "1rem");

        form.add(statusInfo, statusCombo, responseArea, buttonRow);
        content.add(form);
        return content;
    }

    private String getStatusColor(Reclamation.StatutReclamation status) {
        if (status == null) return "#6b7280";
        switch (status) {
            case NOUVELLE: return "#f59e0b";
            case EN_COURS: return "#3b82f6";
            case RESOLUE: return "#10b981";
            case REJETEE: return "#ef4444";
            case FERMEE: return "#6b7280";
            default: return "#6b7280";
        }
    }

    private void updateStatus() {
        Reclamation.StatutReclamation newStatus = statusCombo.getValue();
        if (newStatus == null) {
            showError("Veuillez sélectionner un statut");
            return;
        }

        try {
            reclamationService.updateStatut(reclamation.getId(), newStatus);
            showSuccess("Statut mis à jour");
            onUpdate.run();
            close();
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private void saveResponse() {
        String response = responseArea.getValue();
        if (response == null || response.trim().isEmpty()) {
            showError("Veuillez saisir une réponse");
            return;
        }

        try {
            reclamationService.addOrganizationResponse(reclamation.getId(), response.trim(), null);
            showSuccess("Réponse enregistrée");
            onUpdate.run();
            close();
        } catch (Exception e) {
            showError("Erreur: " + e.getMessage());
        }
    }

    private Div createSection(String title) {
        Div section = new Div();
        section.getStyle()
            .set("margin-bottom", "1rem")
            .set("background", "white")
            .set("border-radius", "8px")
            .set("padding", "0.75rem")
            .set("border", "1px solid #e2e8f0");

        H3 sectionTitle = new H3(title);
        sectionTitle.getStyle()
            .set("margin", "0 0 0.5rem 0")
            .set("font-size", "0.9rem")
            .set("color", "#374151");

        section.add(sectionTitle);
        return section;
    }

    private Div createInfoRow(String label, String value) {
        Div row = new Div();
        row.getStyle()
            .set("display", "flex")
            .set("justify-content", "space-between")
            .set("padding", "0.35rem 0")
            .set("border-bottom", "1px solid #f3f4f6")
            .set("font-size", "0.9rem");

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("color", "#6b7280");

        Span valueSpan = new Span(value != null ? value : "-");
        valueSpan.getStyle().set("font-weight", "500").set("color", "#1f2937");

        row.add(labelSpan, valueSpan);
        return row;
    }

    private void createFooter() {
        Button closeBtn = new Button("Fermer", e -> close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getFooter().add(closeBtn);
    }

    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_END)
            .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private void showError(String message) {
        Notification.show(message, 5000, Notification.Position.TOP_END)
            .addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}
