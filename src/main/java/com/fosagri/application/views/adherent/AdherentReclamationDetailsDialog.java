package com.fosagri.application.views.adherent;

import com.fosagri.application.entities.Reclamation;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.text.SimpleDateFormat;

public class AdherentReclamationDetailsDialog extends Dialog {

    private final Reclamation reclamation;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public AdherentReclamationDetailsDialog(Reclamation reclamation) {
        this.reclamation = reclamation;

        setHeaderTitle("Détails de la Réclamation");
        setWidth("700px");
        setCloseOnOutsideClick(true);

        createContent();
        createFooter();
    }

    private void createContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        // Header with status
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H3 title = new H3(reclamation.getObjet());
        title.getStyle().set("margin", "0");

        Span statusBadge = createStatusBadge();

        header.add(title, statusBadge);
        content.add(header);

        // Metadata row
        HorizontalLayout metaRow = new HorizontalLayout();
        metaRow.setSpacing(true);
        metaRow.getStyle().set("flex-wrap", "wrap");

        metaRow.add(
            createMetaItem("Type", reclamation.getType() != null ? reclamation.getType().getLabel() : "-"),
            createMetaItem("Priorité", reclamation.getPriorite() != null ? reclamation.getPriorite().getLabel() : "-"),
            createMetaItem("Date création", reclamation.getDateCreation() != null ? sdf.format(reclamation.getDateCreation()) : "-")
        );

        if (reclamation.getDateTraitement() != null) {
            metaRow.add(createMetaItem("Date traitement", sdf.format(reclamation.getDateTraitement())));
        }

        if (reclamation.getDateCloture() != null) {
            metaRow.add(createMetaItem("Date clôture", sdf.format(reclamation.getDateCloture())));
        }

        content.add(metaRow);
        content.add(new Hr());

        // Detail section
        if (reclamation.getDetail() != null && !reclamation.getDetail().isEmpty()) {
            Div detailSection = new Div();
            detailSection.getStyle()
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("padding", "1rem")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("margin-bottom", "1rem");

            Div detailLabel = new Div();
            detailLabel.setText("Description");
            detailLabel.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "var(--lumo-secondary-text-color)")
                .set("text-transform", "uppercase")
                .set("margin-bottom", "0.5rem");

            Div detailText = new Div();
            detailText.setText(reclamation.getDetail());
            detailText.getStyle()
                .set("white-space", "pre-wrap")
                .set("line-height", "1.5");

            detailSection.add(detailLabel, detailText);
            content.add(detailSection);
        }

        // Response section (if any)
        if (reclamation.getReponseOrganisation() != null && !reclamation.getReponseOrganisation().isEmpty()) {
            Div responseSection = new Div();
            responseSection.getStyle()
                .set("background-color", "#e8f5e9")
                .set("padding", "1rem")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("border-left", "4px solid #4caf50");

            Div responseLabel = new Div();
            responseLabel.setText("Réponse de l'organisation");
            responseLabel.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "#2e7d32")
                .set("text-transform", "uppercase")
                .set("margin-bottom", "0.5rem")
                .set("font-weight", "600");

            Div responseText = new Div();
            responseText.setText(reclamation.getReponseOrganisation());
            responseText.getStyle()
                .set("white-space", "pre-wrap")
                .set("line-height", "1.5");

            responseSection.add(responseLabel, responseText);
            content.add(responseSection);
        }

        // Status timeline
        content.add(createTimeline());

        add(content);
    }

    private Span createStatusBadge() {
        Reclamation.StatutReclamation statut = reclamation.getStatut();
        String label = statut != null ? statut.getLabel() : "Nouvelle";
        Span badge = new Span(label);
        badge.getStyle()
            .set("padding", "6px 16px")
            .set("border-radius", "16px")
            .set("font-size", "14px")
            .set("font-weight", "600")
            .set("white-space", "nowrap");

        if (statut != null) {
            switch (statut) {
                case NOUVELLE:
                    badge.getStyle().set("background-color", "#d1ecf1").set("color", "#0c5460");
                    break;
                case EN_COURS:
                    badge.getStyle().set("background-color", "#fff3cd").set("color", "#856404");
                    break;
                case RESOLUE:
                    badge.getStyle().set("background-color", "#d4edda").set("color", "#155724");
                    break;
                case FERMEE:
                    badge.getStyle().set("background-color", "#e2e3e5").set("color", "#383d41");
                    break;
                case REJETEE:
                    badge.getStyle().set("background-color", "#f8d7da").set("color", "#721c24");
                    break;
            }
        }

        return badge;
    }

    private Div createMetaItem(String label, String value) {
        Div item = new Div();
        item.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "0.5rem 1rem")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("display", "inline-block");

        Span labelSpan = new Span(label + ": ");
        labelSpan.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "0.875rem");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-weight", "500")
            .set("font-size", "0.875rem");

        item.add(labelSpan, valueSpan);
        return item;
    }

    private Div createTimeline() {
        Div timeline = new Div();
        timeline.getStyle()
            .set("margin-top", "1rem")
            .set("padding", "1rem")
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)");

        Div timelineLabel = new Div();
        timelineLabel.setText("Historique");
        timelineLabel.getStyle()
            .set("font-size", "0.75rem")
            .set("color", "var(--lumo-secondary-text-color)")
            .set("text-transform", "uppercase")
            .set("margin-bottom", "0.75rem");

        VerticalLayout events = new VerticalLayout();
        events.setPadding(false);
        events.setSpacing(false);

        // Created event
        events.add(createTimelineEvent(
            "Réclamation créée",
            reclamation.getDateCreation() != null ? sdf.format(reclamation.getDateCreation()) : "-",
            "#3498db"
        ));

        // Processing event
        if (reclamation.getDateTraitement() != null) {
            events.add(createTimelineEvent(
                "Prise en charge",
                sdf.format(reclamation.getDateTraitement()),
                "#f39c12"
            ));
        }

        // Closure event
        if (reclamation.getDateCloture() != null) {
            String closureLabel;
            String color;
            if (reclamation.getStatut() == Reclamation.StatutReclamation.RESOLUE) {
                closureLabel = "Résolue";
                color = "#27ae60";
            } else if (reclamation.getStatut() == Reclamation.StatutReclamation.REJETEE) {
                closureLabel = "Rejetée";
                color = "#e74c3c";
            } else {
                closureLabel = "Fermée";
                color = "#7f8c8d";
            }
            events.add(createTimelineEvent(closureLabel, sdf.format(reclamation.getDateCloture()), color));
        }

        timeline.add(timelineLabel, events);
        return timeline;
    }

    private HorizontalLayout createTimelineEvent(String label, String date, String color) {
        HorizontalLayout event = new HorizontalLayout();
        event.setAlignItems(FlexComponent.Alignment.CENTER);
        event.setSpacing(true);
        event.getStyle().set("margin-bottom", "0.5rem");

        Div dot = new Div();
        dot.getStyle()
            .set("width", "12px")
            .set("height", "12px")
            .set("border-radius", "50%")
            .set("background-color", color)
            .set("flex-shrink", "0");

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "500");

        Span dateSpan = new Span(date);
        dateSpan.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-size", "0.875rem");

        event.add(dot, labelSpan, dateSpan);
        return event;
    }

    private void createFooter() {
        Button closeBtn = new Button("Fermer", e -> close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        getFooter().add(closeBtn);
    }
}
