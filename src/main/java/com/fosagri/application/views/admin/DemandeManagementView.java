package com.fosagri.application.views.admin;

import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.services.DemandePrestationService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Gestion des Demandes")
@Route("admin/demandes")
@Menu(order = 4, icon = LineAwesomeIconUrl.FILE_ALT)
@RolesAllowed("ADMIN")
public class DemandeManagementView extends VerticalLayout {

    private final DemandePrestationService demandeService;
    private Grid<DemandePrestation> grid;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private Div statsContainer;

    public DemandeManagementView(DemandePrestationService demandeService) {
        this.demandeService = demandeService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createStatsSection());
        add(createFilters());
        add(createGrid());

        updateGrid();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 title = new H2("Gestion des Demandes");
        title.getStyle().set("margin", "0");

        header.add(title);
        return header;
    }

    private Component createStatsSection() {
        statsContainer = new Div();
        statsContainer.getStyle()
            .set("display", "flex")
            .set("gap", "1rem")
            .set("margin-bottom", "1rem")
            .set("flex-wrap", "wrap");

        updateStats();
        return statsContainer;
    }

    private void updateStats() {
        statsContainer.removeAll();

        long total = demandeService.count();
        long soumises = demandeService.countByStatut("SOUMISE");
        long enCours = demandeService.countByStatut("EN_COURS");
        long acceptees = demandeService.countByStatut("ACCEPTEE");
        long refusees = demandeService.countByStatut("REFUSEE");

        statsContainer.add(
            createStatCard("Total", String.valueOf(total), "#6366f1"),
            createStatCard("Soumises", String.valueOf(soumises), "#f59e0b"),
            createStatCard("En cours", String.valueOf(enCours), "#3b82f6"),
            createStatCard("Acceptées", String.valueOf(acceptees), "#10b981"),
            createStatCard("Refusées", String.valueOf(refusees), "#ef4444")
        );
    }

    private Component createStatCard(String label, String value, String color) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("padding", "0.75rem 1.25rem")
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
            .set("border-left", "4px solid " + color)
            .set("min-width", "100px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "1.25rem")
            .set("font-weight", "700")
            .set("color", color)
            .set("display", "block");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.8rem");

        card.add(valueSpan, labelSpan);
        return card;
    }

    private Component createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setAlignItems(FlexComponent.Alignment.END);
        filters.setSpacing(true);

        searchField = new TextField("Rechercher");
        searchField.setPlaceholder("Agent ou prestation...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> updateGrid());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems("Tous", "SOUMISE", "EN_COURS", "ACCEPTEE", "REFUSEE", "TERMINEE");
        statusFilter.setValue("Tous");
        statusFilter.addValueChangeListener(e -> updateGrid());

        Button refreshBtn = new Button(VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> updateGrid());

        filters.add(searchField, statusFilter, refreshBtn);
        return filters;
    }

    private Component createGrid() {
        grid = new Grid<>(DemandePrestation.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("450px");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        grid.addColumn(DemandePrestation::getId).setHeader("ID").setWidth("70px").setFlexGrow(0);
        grid.addColumn(d -> d.getAgent() != null ? d.getAgent().getNOM_AG() + " " + d.getAgent().getPR_AG() : "-")
            .setHeader("Agent").setAutoWidth(true);
        grid.addColumn(d -> d.getPrestation() != null ? d.getPrestation().getLabel() : "-")
            .setHeader("Prestation").setAutoWidth(true).setFlexGrow(1);
        grid.addComponentColumn(this::createStatusBadge).setHeader("Statut").setAutoWidth(true);
        grid.addColumn(d -> d.getDateDemande() != null ? sdf.format(d.getDateDemande()) : "-")
            .setHeader("Date demande").setAutoWidth(true);
        grid.addColumn(d -> d.getDateTraitement() != null ? sdf.format(d.getDateTraitement()) : "-")
            .setHeader("Date traitement").setAutoWidth(true);
        grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setAutoWidth(true);

        return grid;
    }

    private Component createStatusBadge(DemandePrestation d) {
        String status = d.getStatut() != null ? d.getStatut() : "SOUMISE";
        Span badge = new Span(getStatusLabel(status));
        badge.getStyle()
            .set("padding", "4px 10px")
            .set("border-radius", "12px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500");

        switch (status.toUpperCase()) {
            case "SOUMISE":
                badge.getStyle().set("background", "#fef3c7").set("color", "#92400e");
                break;
            case "EN_COURS":
                badge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
                break;
            case "ACCEPTEE":
                badge.getStyle().set("background", "#dcfce7").set("color", "#166534");
                break;
            case "REFUSEE":
                badge.getStyle().set("background", "#fee2e2").set("color", "#991b1b");
                break;
            case "TERMINEE":
                badge.getStyle().set("background", "#e0e7ff").set("color", "#3730a3");
                break;
            default:
                badge.getStyle().set("background", "#f3f4f6").set("color", "#374151");
        }
        return badge;
    }

    private String getStatusLabel(String status) {
        switch (status.toUpperCase()) {
            case "SOUMISE": return "Soumise";
            case "EN_COURS": return "En cours";
            case "ACCEPTEE": return "Acceptée";
            case "REFUSEE": return "Refusée";
            case "TERMINEE": return "Terminée";
            default: return status;
        }
    }

    private Component createActionButtons(DemandePrestation demande) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button viewBtn = new Button(VaadinIcon.EYE.create());
        viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        viewBtn.getElement().setAttribute("title", "Voir détails");
        viewBtn.addClickListener(e -> openDetailsDialog(demande));

        Button processBtn = new Button(VaadinIcon.CHECK.create());
        processBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        processBtn.getElement().setAttribute("title", "Traiter");
        processBtn.addClickListener(e -> openProcessDialog(demande));

        actions.add(viewBtn, processBtn);
        return actions;
    }

    private void updateGrid() {
        String search = searchField.getValue();
        String status = statusFilter.getValue();

        List<DemandePrestation> demandes;

        if (search != null && !search.trim().isEmpty()) {
            demandes = demandeService.searchDemandes(search.trim());
        } else if (status != null && !"Tous".equals(status)) {
            demandes = demandeService.findByStatut(status);
        } else {
            demandes = demandeService.findAll();
        }

        // Apply status filter if search was used
        if (status != null && !"Tous".equals(status) && search != null && !search.trim().isEmpty()) {
            final String finalStatus = status;
            demandes = demandes.stream()
                .filter(d -> finalStatus.equals(d.getStatut()))
                .collect(Collectors.toList());
        }

        grid.setItems(demandes);
        updateStats();
    }

    private void openDetailsDialog(DemandePrestation demande) {
        DemandeDetailsDialog dialog = new DemandeDetailsDialog(demande, demandeService, () -> updateGrid());
        dialog.open();
    }

    private void openProcessDialog(DemandePrestation demande) {
        DemandeDetailsDialog dialog = new DemandeDetailsDialog(demande, demandeService, () -> updateGrid());
        dialog.open();
    }

    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_END);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
