package com.fosagri.application.views.admin;

import com.fosagri.application.entities.Reclamation;
import com.fosagri.application.services.ReclamationService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Gestion des Réclamations")
@Route("admin/reclamations")
@Menu(order = 5, icon = LineAwesomeIconUrl.EXCLAMATION_TRIANGLE_SOLID)
@RolesAllowed("ADMIN")
public class ReclamationManagementView extends VerticalLayout {

    private final ReclamationService reclamationService;
    private Grid<Reclamation> grid;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> typeFilter;
    private Div statsContainer;

    public ReclamationManagementView(ReclamationService reclamationService) {
        this.reclamationService = reclamationService;

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

        H2 title = new H2("Gestion des Réclamations");
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

        Map<Reclamation.StatutReclamation, Long> stats = reclamationService.getStatusStatistics();

        long nouvelle = stats.getOrDefault(Reclamation.StatutReclamation.NOUVELLE, 0L);
        long enCours = stats.getOrDefault(Reclamation.StatutReclamation.EN_COURS, 0L);
        long resolue = stats.getOrDefault(Reclamation.StatutReclamation.RESOLUE, 0L);
        long rejetee = stats.getOrDefault(Reclamation.StatutReclamation.REJETEE, 0L);
        long total = nouvelle + enCours + resolue + rejetee;

        statsContainer.add(
            createStatCard("Total", String.valueOf(total), "#6366f1"),
            createStatCard("Nouvelles", String.valueOf(nouvelle), "#f59e0b"),
            createStatCard("En cours", String.valueOf(enCours), "#3b82f6"),
            createStatCard("Résolues", String.valueOf(resolue), "#10b981"),
            createStatCard("Rejetées", String.valueOf(rejetee), "#ef4444")
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
        searchField.setPlaceholder("Objet ou détails...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("250px");
        searchField.addValueChangeListener(e -> updateGrid());

        statusFilter = new ComboBox<>("Statut");
        statusFilter.setItems("Tous", "NOUVELLE", "EN_COURS", "RESOLUE", "REJETEE");
        statusFilter.setValue("Tous");
        statusFilter.addValueChangeListener(e -> updateGrid());

        typeFilter = new ComboBox<>("Type");
        typeFilter.setItems("Tous", "ADMINISTRATIVE", "FINANCIERE", "TECHNIQUE", "PRESTATIONS", "AUTRE");
        typeFilter.setValue("Tous");
        typeFilter.addValueChangeListener(e -> updateGrid());

        Button refreshBtn = new Button(VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> updateGrid());

        filters.add(searchField, statusFilter, typeFilter, refreshBtn);
        return filters;
    }

    private Component createGrid() {
        grid = new Grid<>(Reclamation.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("450px");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        grid.addColumn(Reclamation::getId).setHeader("ID").setWidth("70px").setFlexGrow(0);
        grid.addColumn(r -> {
            try {
                return reclamationService.getAgentNameForReclamation(r.getId());
            } catch (Exception e) {
                return "-";
            }
        }).setHeader("Agent").setAutoWidth(true);
        grid.addColumn(Reclamation::getObjet).setHeader("Objet").setAutoWidth(true).setFlexGrow(1);
        grid.addComponentColumn(this::createTypeBadge).setHeader("Type").setAutoWidth(true);
        grid.addComponentColumn(this::createStatusBadge).setHeader("Statut").setAutoWidth(true);
        grid.addComponentColumn(this::createPriorityBadge).setHeader("Priorité").setAutoWidth(true);
        grid.addColumn(r -> r.getDateCreation() != null ? sdf.format(r.getDateCreation()) : "-")
            .setHeader("Date création").setAutoWidth(true);
        grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setAutoWidth(true);

        return grid;
    }

    private Component createTypeBadge(Reclamation r) {
        String type = r.getType() != null ? r.getType().name() : "N/A";
        Span badge = new Span(type);
        badge.getStyle()
            .set("padding", "4px 8px")
            .set("border-radius", "4px")
            .set("font-size", "0.7rem")
            .set("font-weight", "500")
            .set("background", "#f3f4f6")
            .set("color", "#374151");
        return badge;
    }

    private Component createStatusBadge(Reclamation r) {
        Reclamation.StatutReclamation status = r.getStatut();
        String label = status != null ? status.getLabel() : "N/A";
        Span badge = new Span(label);
        badge.getStyle()
            .set("padding", "4px 10px")
            .set("border-radius", "12px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500");

        if (status != null) {
            switch (status) {
                case NOUVELLE:
                    badge.getStyle().set("background", "#fef3c7").set("color", "#92400e");
                    break;
                case EN_COURS:
                    badge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
                    break;
                case RESOLUE:
                    badge.getStyle().set("background", "#dcfce7").set("color", "#166534");
                    break;
                case REJETEE:
                    badge.getStyle().set("background", "#fee2e2").set("color", "#991b1b");
                    break;
                default:
                    badge.getStyle().set("background", "#f3f4f6").set("color", "#374151");
            }
        }
        return badge;
    }

    private Component createPriorityBadge(Reclamation r) {
        Reclamation.PrioriteReclamation priority = r.getPriorite();
        String label = priority != null ? priority.getLabel() : "Normale";
        Span badge = new Span(label);
        badge.getStyle()
            .set("padding", "4px 8px")
            .set("border-radius", "4px")
            .set("font-size", "0.7rem")
            .set("font-weight", "500");

        if (priority != null) {
            switch (priority) {
                case URGENTE:
                    badge.getStyle().set("background", "#fee2e2").set("color", "#991b1b");
                    break;
                case HAUTE:
                    badge.getStyle().set("background", "#ffedd5").set("color", "#9a3412");
                    break;
                case NORMALE:
                    badge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
                    break;
                case FAIBLE:
                    badge.getStyle().set("background", "#f3f4f6").set("color", "#374151");
                    break;
            }
        }
        return badge;
    }

    private Component createActionButtons(Reclamation reclamation) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button viewBtn = new Button(VaadinIcon.EYE.create());
        viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        viewBtn.getElement().setAttribute("title", "Voir détails");
        viewBtn.addClickListener(e -> openDetailsDialog(reclamation));

        Button respondBtn = new Button(VaadinIcon.COMMENT.create());
        respondBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
        respondBtn.getElement().setAttribute("title", "Répondre");
        respondBtn.addClickListener(e -> openDetailsDialog(reclamation));

        actions.add(viewBtn, respondBtn);
        return actions;
    }

    private void updateGrid() {
        String search = searchField.getValue();
        String status = statusFilter.getValue();
        String type = typeFilter.getValue();

        List<Reclamation> reclamations;

        if (search != null && !search.trim().isEmpty()) {
            reclamations = reclamationService.searchReclamations(search.trim());
        } else if (status != null && !"Tous".equals(status)) {
            reclamations = reclamationService.findByStatut(Reclamation.StatutReclamation.valueOf(status));
        } else if (type != null && !"Tous".equals(type)) {
            reclamations = reclamationService.findByType(Reclamation.TypeReclamation.valueOf(type));
        } else {
            reclamations = reclamationService.findAll();
        }

        // Apply additional filters
        if (status != null && !"Tous".equals(status) && (search != null && !search.isEmpty() || type != null && !"Tous".equals(type))) {
            final String finalStatus = status;
            reclamations = reclamations.stream()
                .filter(r -> r.getStatut() != null && r.getStatut().name().equals(finalStatus))
                .collect(Collectors.toList());
        }

        if (type != null && !"Tous".equals(type) && (search != null && !search.isEmpty())) {
            final String finalType = type;
            reclamations = reclamations.stream()
                .filter(r -> r.getType() != null && r.getType().name().equals(finalType))
                .collect(Collectors.toList());
        }

        grid.setItems(reclamations);
        updateStats();
    }

    private void openDetailsDialog(Reclamation reclamation) {
        ReclamationResponseDialog dialog = new ReclamationResponseDialog(reclamation, reclamationService, () -> updateGrid());
        dialog.open();
    }
}
