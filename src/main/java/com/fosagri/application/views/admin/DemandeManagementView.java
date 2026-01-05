package com.fosagri.application.views.admin;

import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.services.DemandePrestationService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Gestion des Demandes")
@Route(value = "admin/demandes", layout = MainLayout.class)
@Menu(order = 4, icon = LineAwesomeIconUrl.FILE_ALT)
@RolesAllowed("ADMIN")
public class DemandeManagementView extends VerticalLayout {

    private final DemandePrestationService demandeService;
    private Grid<DemandePrestation> grid;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private HorizontalLayout statsContainer;

    public DemandeManagementView(DemandePrestationService demandeService) {
        this.demandeService = demandeService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#f8fafc");

        // Main container with padding
        VerticalLayout mainContainer = new VerticalLayout();
        mainContainer.setSizeFull();
        mainContainer.setPadding(true);
        mainContainer.setSpacing(false);
        mainContainer.getStyle().set("gap", "0.75rem");

        mainContainer.add(createHeader());
        mainContainer.add(createStatsSection());
        mainContainer.addAndExpand(createContentCard());

        add(mainContainer);
        setFlexGrow(1, mainContainer);
        updateGrid();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle().set("margin-bottom", "0.5rem");

        HorizontalLayout titleSection = new HorizontalLayout();
        titleSection.setAlignItems(FlexComponent.Alignment.CENTER);
        titleSection.setSpacing(true);

        Icon icon = VaadinIcon.FILE_TEXT_O.create();
        icon.getStyle().set("color", "#3b6b35");

        H3 title = new H3("Gestion des Demandes");
        title.getStyle()
            .set("margin", "0")
            .set("color", "#1e293b")
            .set("font-weight", "600");

        titleSection.add(icon, title);

        Button refreshBtn = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        refreshBtn.addClickListener(e -> updateGrid());

        header.add(titleSection, refreshBtn);
        return header;
    }

    private Component createStatsSection() {
        statsContainer = new HorizontalLayout();
        statsContainer.setWidthFull();
        statsContainer.getStyle()
            .set("gap", "0.5rem")
            .set("flex-wrap", "wrap");
        statsContainer.setPadding(false);
        statsContainer.setSpacing(false);

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
            createStatCard("Total", String.valueOf(total), "#6366f1", VaadinIcon.RECORDS),
            createStatCard("Soumises", String.valueOf(soumises), "#f59e0b", VaadinIcon.CLOCK),
            createStatCard("En cours", String.valueOf(enCours), "#3b82f6", VaadinIcon.HOURGLASS),
            createStatCard("Acceptées", String.valueOf(acceptees), "#10b981", VaadinIcon.CHECK_CIRCLE),
            createStatCard("Refusées", String.valueOf(refusees), "#ef4444", VaadinIcon.CLOSE_CIRCLE)
        );
    }

    private Component createStatCard(String label, String value, String color, VaadinIcon iconType) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("padding", "0.6rem 1rem")
            .set("box-shadow", "0 1px 2px rgba(0,0,0,0.05)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.6rem")
            .set("flex", "1")
            .set("min-width", "120px");

        Div iconWrapper = new Div();
        iconWrapper.getStyle()
            .set("background", color + "15")
            .set("border-radius", "6px")
            .set("padding", "0.4rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

        Icon icon = iconType.create();
        icon.setSize("16px");
        icon.getStyle().set("color", color);
        iconWrapper.add(icon);

        Div textWrapper = new Div();

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "1.1rem")
            .set("font-weight", "700")
            .set("color", "#1e293b")
            .set("display", "block")
            .set("line-height", "1.2");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.7rem")
            .set("text-transform", "uppercase")
            .set("letter-spacing", "0.02em");

        textWrapper.add(valueSpan, labelSpan);
        card.add(iconWrapper, textWrapper);
        return card;
    }

    private Component createContentCard() {
        VerticalLayout card = new VerticalLayout();
        card.setSizeFull();
        card.setPadding(false);
        card.setSpacing(false);
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "10px")
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.08)")
            .set("overflow", "hidden");

        card.add(createFilters());
        card.addAndExpand(createGrid());

        return card;
    }

    private Component createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setAlignItems(FlexComponent.Alignment.CENTER);
        filters.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        filters.getStyle()
            .set("padding", "0.75rem 1rem")
            .set("border-bottom", "1px solid #e2e8f0")
            .set("background", "#fafbfc");

        HorizontalLayout leftFilters = new HorizontalLayout();
        leftFilters.setAlignItems(FlexComponent.Alignment.CENTER);
        leftFilters.setSpacing(true);
        leftFilters.getStyle().set("gap", "0.5rem");

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("220px");
        searchField.getStyle().set("--vaadin-input-field-border-radius", "6px");
        searchField.addValueChangeListener(e -> updateGrid());

        statusFilter = new ComboBox<>();
        statusFilter.setPlaceholder("Statut");
        statusFilter.setItems("Tous", "SOUMISE", "EN_COURS", "ACCEPTEE", "REFUSEE", "TERMINEE");
        statusFilter.setValue("Tous");
        statusFilter.setWidth("140px");
        statusFilter.addValueChangeListener(e -> updateGrid());

        leftFilters.add(searchField, statusFilter);

        // Count display
        Span countLabel = new Span();
        countLabel.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.8rem");
        countLabel.setId("count-label");

        filters.add(leftFilters, countLabel);
        return filters;
    }

    private Component createGrid() {
        grid = new Grid<>(DemandePrestation.class, false);
        grid.addThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_NO_BORDER);
        grid.setSizeFull();
        grid.getStyle().set("font-size", "0.85rem");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");

        grid.addColumn(d -> d.getAgent() != null ? d.getAgent().getNOM_AG() + " " + d.getAgent().getPR_AG() : "-")
            .setHeader("Agent")
            .setFlexGrow(1);

        grid.addColumn(d -> d.getPrestation() != null ? d.getPrestation().getLabel() : "-")
            .setHeader("Prestation")
            .setFlexGrow(2);

        grid.addComponentColumn(this::createStatusInfo)
            .setHeader("Statut / Dates")
            .setWidth("180px")
            .setFlexGrow(0);

        grid.addComponentColumn(this::createActionButtons)
            .setHeader("")
            .setWidth("80px")
            .setFlexGrow(0);

        return grid;
    }

    private Component createStatusInfo(DemandePrestation d) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");

        Div container = new Div();
        container.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "2px");

        // Status badge
        String status = d.getStatut() != null ? d.getStatut() : "SOUMISE";
        Span badge = new Span(getStatusLabel(status));
        badge.getStyle()
            .set("padding", "2px 8px")
            .set("border-radius", "10px")
            .set("font-size", "0.7rem")
            .set("font-weight", "500")
            .set("white-space", "nowrap")
            .set("display", "inline-block")
            .set("width", "fit-content");

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

        // Dates
        String demande = d.getDateDemande() != null ? sdf.format(d.getDateDemande()) : "-";
        String traitement = d.getDateTraitement() != null ? sdf.format(d.getDateTraitement()) : "-";

        Span dates = new Span(demande + " → " + traitement);
        dates.getStyle()
            .set("font-size", "0.65rem")
            .set("color", "#64748b");

        container.add(badge, dates);
        return container;
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
        actions.setSpacing(false);
        actions.getStyle().set("gap", "0.25rem");

        Button viewBtn = new Button(VaadinIcon.EYE.create());
        viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        viewBtn.getStyle().set("color", "#64748b");
        viewBtn.getElement().setAttribute("title", "Voir détails");
        viewBtn.addClickListener(e -> openDetailsDialog(demande));

        Button processBtn = new Button(VaadinIcon.CHECK.create());
        processBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
        processBtn.getStyle().set("color", "#10b981");
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
