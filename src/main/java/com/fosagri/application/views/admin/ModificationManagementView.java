package com.fosagri.application.views.admin;

import com.fosagri.application.entities.ModificationDemande;
import com.fosagri.application.entities.ModificationDemande.StatutModification;
import com.fosagri.application.entities.ModificationDemande.TypeAction;
import com.fosagri.application.entities.ModificationDemande.TypeEntite;
import com.fosagri.application.services.ModificationDemandeService;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
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
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@PageTitle("Gestion des Modifications")
@Route(value = "admin/modifications", layout = MainLayout.class)
@Menu(order = 5, icon = LineAwesomeIconUrl.EDIT)
@RolesAllowed("ADMIN")
public class ModificationManagementView extends VerticalLayout {

    private final ModificationDemandeService modificationService;
    private Grid<ModificationDemande> grid;
    private TextField searchField;
    private ComboBox<String> statusFilter;
    private ComboBox<String> typeFilter;
    private ComboBox<String> actionFilter;

    // Stats labels
    private Span totalCountLabel;
    private Span pendingCountLabel;
    private Span validatedCountLabel;
    private Span rejectedCountLabel;

    public ModificationManagementView(ModificationDemandeService modificationService) {
        this.modificationService = modificationService;

        addClassName("modifications-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("padding", "0.5rem");
        getStyle().set("gap", "0.5rem");

        createHeader();
        createStatsSection();
        createFilters();
        createGrid();

        updateGrid();
    }

    private void createHeader() {
        H2 title = new H2("Gestion des Modifications");
        title.getStyle()
            .set("color", "#1e293b")
            .set("margin", "0")
            .set("margin-bottom", "0.25rem")
            .set("font-size", "1.25rem");
        add(title);
    }

    private void createStatsSection() {
        HorizontalLayout statsContainer = new HorizontalLayout();
        statsContainer.setWidthFull();
        statsContainer.setSpacing(true);
        statsContainer.getStyle().set("margin-bottom", "0.5rem");

        // Total card
        totalCountLabel = new Span("0");
        Component totalCard = createStatCard("Total", totalCountLabel, VaadinIcon.LIST, "#6366f1");

        // Pending card
        pendingCountLabel = new Span("0");
        Component pendingCard = createStatCard("En attente", pendingCountLabel, VaadinIcon.CLOCK, "#f59e0b");

        // Validated card
        validatedCountLabel = new Span("0");
        Component validatedCard = createStatCard("Validees", validatedCountLabel, VaadinIcon.CHECK, "#10b981");

        // Rejected card
        rejectedCountLabel = new Span("0");
        Component rejectedCard = createStatCard("Refusees", rejectedCountLabel, VaadinIcon.CLOSE, "#ef4444");

        statsContainer.add(totalCard, pendingCard, validatedCard, rejectedCard);
        add(statsContainer);

        updateStats();
    }

    private Component createStatCard(String label, Span valueSpan, VaadinIcon iconType, String color) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("padding", "0.5rem 1rem")
            .set("box-shadow", "0 1px 4px rgba(0,0,0,0.06)")
            .set("border-left", "3px solid " + color)
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.75rem")
            .set("min-width", "120px");

        // Icon container
        Div iconContainer = new Div();
        iconContainer.getStyle()
            .set("width", "32px")
            .set("height", "32px")
            .set("border-radius", "8px")
            .set("background", color + "15")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

        Icon icon = iconType.create();
        icon.setSize("16px");
        icon.getStyle().set("color", color);
        iconContainer.add(icon);

        // Text container
        Div textContainer = new Div();

        valueSpan.getStyle()
            .set("font-size", "1.25rem")
            .set("font-weight", "700")
            .set("color", color)
            .set("display", "block")
            .set("line-height", "1");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.75rem")
            .set("display", "block")
            .set("margin-top", "0.1rem");

        textContainer.add(valueSpan, labelSpan);
        card.add(iconContainer, textContainer);

        return card;
    }

    private void updateStats() {
        long total = modificationService.findAll().size();
        long pending = modificationService.countByStatut(StatutModification.EN_ATTENTE);
        long validated = modificationService.countByStatut(StatutModification.VALIDE);
        long rejected = modificationService.countByStatut(StatutModification.REFUSE);

        totalCountLabel.setText(String.valueOf(total));
        pendingCountLabel.setText(String.valueOf(pending));
        validatedCountLabel.setText(String.valueOf(validated));
        rejectedCountLabel.setText(String.valueOf(rejected));
    }

    private void createFilters() {
        HorizontalLayout filters = new HorizontalLayout();
        filters.setWidthFull();
        filters.setAlignItems(FlexComponent.Alignment.END);
        filters.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        filters.getStyle().set("margin-bottom", "0.25rem");

        // Left side: search + filters
        HorizontalLayout leftSide = new HorizontalLayout();
        leftSide.setAlignItems(FlexComponent.Alignment.END);
        leftSide.setSpacing(true);
        leftSide.getStyle().set("gap", "0.5rem");

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher agent...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(300);
        searchField.setWidth("200px");
        searchField.setClearButtonVisible(true);
        searchField.getStyle()
            .set("--vaadin-input-field-border-radius", "1.5rem")
            .set("font-size", "0.85rem");
        searchField.addValueChangeListener(e -> updateGrid());

        statusFilter = new ComboBox<>();
        statusFilter.setPlaceholder("Statut");
        statusFilter.setItems("Tous", "EN_ATTENTE", "VALIDE", "REFUSE");
        statusFilter.setValue("Tous");
        statusFilter.setWidth("120px");
        statusFilter.getStyle().set("--vaadin-combo-box-overlay-width", "140px");
        statusFilter.addValueChangeListener(e -> updateGrid());

        typeFilter = new ComboBox<>();
        typeFilter.setPlaceholder("Type");
        typeFilter.setItems("Tous", "AGENT", "CONJOINT", "ENFANT");
        typeFilter.setValue("Tous");
        typeFilter.setWidth("110px");
        typeFilter.addValueChangeListener(e -> updateGrid());

        actionFilter = new ComboBox<>();
        actionFilter.setPlaceholder("Action");
        actionFilter.setItems("Tous", "CREATION", "MODIFICATION");
        actionFilter.setValue("Tous");
        actionFilter.setWidth("130px");
        actionFilter.addValueChangeListener(e -> updateGrid());

        leftSide.add(searchField, statusFilter, typeFilter, actionFilter);

        // Right side: refresh button
        Button refreshBtn = new Button(VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        refreshBtn.getElement().setAttribute("title", "Actualiser");
        refreshBtn.addClickListener(e -> updateGrid());

        filters.add(leftSide, refreshBtn);
        add(filters);
    }

    private void createGrid() {
        grid = new Grid<>(ModificationDemande.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeight("100%");
        grid.getStyle()
            .set("border-radius", "8px")
            .set("box-shadow", "0 1px 4px rgba(0,0,0,0.06)")
            .set("font-size", "0.85rem")
            .set("--lumo-size-m", "42px");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Agent info: Name + ID in 2 lines
        grid.addComponentColumn(this::createAgentCell)
            .setHeader("Agent")
            .setAutoWidth(true);

        // Action + Type merged
        grid.addComponentColumn(this::createActionTypeCell)
            .setHeader("Type")
            .setAutoWidth(true);

        // Entity label
        grid.addColumn(ModificationDemande::getEntiteLabel)
            .setHeader("Entite")
            .setAutoWidth(true)
            .setFlexGrow(1);

        // Status badge
        grid.addComponentColumn(this::createStatusBadge)
            .setHeader("Statut")
            .setAutoWidth(true);

        // Date: Creation + Traitement in 2 lines
        grid.addComponentColumn(m -> createDateCell(m, sdf))
            .setHeader("Dates")
            .setAutoWidth(true);

        // Actions column
        grid.addComponentColumn(this::createActionButtons)
            .setHeader("Actions")
            .setWidth("100px")
            .setFlexGrow(0);

        add(grid);
    }

    private Component createAgentCell(ModificationDemande m) {
        VerticalLayout cell = new VerticalLayout();
        cell.setPadding(false);
        cell.setSpacing(false);
        cell.getStyle().set("gap", "2px");

        if (m.getAgent() != null) {
            // Line 1: Full name
            String fullName = m.getAgent().getNOM_AG() + " " + m.getAgent().getPR_AG();
            Span nameSpan = new Span(fullName);
            nameSpan.getStyle()
                .set("font-weight", "500")
                .set("font-size", "0.85rem")
                .set("color", "#1e293b");

            // Line 2: ID Adherent
            HorizontalLayout idLine = new HorizontalLayout();
            idLine.setPadding(false);
            idLine.setSpacing(false);
            idLine.setAlignItems(FlexComponent.Alignment.CENTER);
            idLine.getStyle().set("gap", "4px");

            Span idLabel = new Span("ID:");
            idLabel.getStyle()
                .set("font-size", "0.7rem")
                .set("color", "#64748b");
            Span idValue = new Span(m.getAgent().getIdAdh() != null ? m.getAgent().getIdAdh() : "-");
            idValue.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "#475569");
            idLine.add(idLabel, idValue);

            cell.add(nameSpan, idLine);
        } else {
            Span dash = new Span("-");
            dash.getStyle().set("color", "#94a3b8");
            cell.add(dash);
        }

        return cell;
    }

    private Component createActionTypeCell(ModificationDemande m) {
        VerticalLayout cell = new VerticalLayout();
        cell.setPadding(false);
        cell.setSpacing(false);
        cell.getStyle().set("gap", "3px");

        // Line 1: Action badge (Creation/Modification)
        boolean isCreation = m.isCreation();
        Span actionBadge = new Span(isCreation ? "Creation" : "Modif.");
        actionBadge.getStyle()
            .set("padding", "1px 6px")
            .set("border-radius", "9999px")
            .set("font-size", "0.65rem")
            .set("font-weight", "500");

        if (isCreation) {
            actionBadge.getStyle().set("background", "#d1fae5").set("color", "#059669");
        } else {
            actionBadge.getStyle().set("background", "#e0f2fe").set("color", "#0284c7");
        }

        // Line 2: Type badge (Agent/Conjoint/Enfant)
        TypeEntite type = m.getTypeEntite();
        Span typeBadge = new Span(type != null ? type.name() : "-");
        typeBadge.getStyle()
            .set("padding", "1px 6px")
            .set("border-radius", "9999px")
            .set("font-size", "0.65rem")
            .set("font-weight", "500");

        if (type != null) {
            switch (type) {
                case AGENT:
                    typeBadge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
                    break;
                case CONJOINT:
                    typeBadge.getStyle().set("background", "#f3e8ff").set("color", "#7c3aed");
                    break;
                case ENFANT:
                    typeBadge.getStyle().set("background", "#dcfce7").set("color", "#166534");
                    break;
            }
        } else {
            typeBadge.getStyle().set("background", "#f3f4f6").set("color", "#374151");
        }

        cell.add(actionBadge, typeBadge);
        return cell;
    }

    private Component createStatusBadge(ModificationDemande m) {
        StatutModification status = m.getStatut();
        String statusLabel = "";
        String bgColor = "#f3f4f6";
        String textColor = "#374151";

        if (status != null) {
            switch (status) {
                case EN_ATTENTE:
                    statusLabel = "En attente";
                    bgColor = "#fef3c7";
                    textColor = "#92400e";
                    break;
                case VALIDE:
                    statusLabel = "Validee";
                    bgColor = "#dcfce7";
                    textColor = "#166534";
                    break;
                case REFUSE:
                    statusLabel = "Refusee";
                    bgColor = "#fee2e2";
                    textColor = "#991b1b";
                    break;
            }
        }

        Span badge = new Span(statusLabel);
        badge.getStyle()
            .set("padding", "2px 8px")
            .set("border-radius", "9999px")
            .set("font-size", "0.7rem")
            .set("font-weight", "500")
            .set("background", bgColor)
            .set("color", textColor);

        return badge;
    }

    private Component createDateCell(ModificationDemande m, SimpleDateFormat sdf) {
        VerticalLayout cell = new VerticalLayout();
        cell.setPadding(false);
        cell.setSpacing(false);
        cell.getStyle().set("gap", "2px");

        // Line 1: Date creation
        HorizontalLayout createdLine = new HorizontalLayout();
        createdLine.setPadding(false);
        createdLine.setSpacing(false);
        createdLine.setAlignItems(FlexComponent.Alignment.CENTER);
        createdLine.getStyle().set("gap", "4px");

        Icon createdIcon = VaadinIcon.CALENDAR.create();
        createdIcon.setSize("10px");
        createdIcon.getStyle().set("color", "#3b82f6");
        Span createdValue = new Span(m.getDateCreation() != null ? sdf.format(m.getDateCreation()) : "-");
        createdValue.getStyle()
            .set("font-size", "0.75rem")
            .set("color", "#1e293b");
        createdLine.add(createdIcon, createdValue);

        cell.add(createdLine);

        // Line 2: Date traitement (if exists)
        if (m.getDateTraitement() != null) {
            HorizontalLayout treatedLine = new HorizontalLayout();
            treatedLine.setPadding(false);
            treatedLine.setSpacing(false);
            treatedLine.setAlignItems(FlexComponent.Alignment.CENTER);
            treatedLine.getStyle().set("gap", "4px");

            Icon treatedIcon = VaadinIcon.CHECK_CIRCLE.create();
            treatedIcon.setSize("10px");
            treatedIcon.getStyle().set("color", "#10b981");
            Span treatedValue = new Span(sdf.format(m.getDateTraitement()));
            treatedValue.getStyle()
                .set("font-size", "0.75rem")
                .set("color", "#475569");
            treatedLine.add(treatedIcon, treatedValue);

            cell.add(treatedLine);
        }

        return cell;
    }

    private Component createActionButtons(ModificationDemande modification) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(false);
        actions.getStyle().set("gap", "0.25rem");

        Button viewBtn = new Button(VaadinIcon.EYE.create());
        viewBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        viewBtn.getElement().setAttribute("title", "Voir details");
        viewBtn.addClickListener(e -> openDetailsDialog(modification));

        actions.add(viewBtn);

        // Only show approve/reject buttons for pending modifications
        if (modification.getStatut() == StatutModification.EN_ATTENTE) {
            Button approveBtn = new Button(VaadinIcon.CHECK.create());
            approveBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            approveBtn.getElement().setAttribute("title", "Valider");
            approveBtn.addClickListener(e -> openDetailsDialog(modification));

            Button rejectBtn = new Button(VaadinIcon.CLOSE.create());
            rejectBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            rejectBtn.getElement().setAttribute("title", "Refuser");
            rejectBtn.addClickListener(e -> openDetailsDialog(modification));

            actions.add(approveBtn, rejectBtn);
        }

        return actions;
    }

    private void updateGrid() {
        String search = searchField.getValue();
        String status = statusFilter.getValue();
        String type = typeFilter.getValue();
        String action = actionFilter.getValue();

        List<ModificationDemande> modifications;

        if (search != null && !search.trim().isEmpty()) {
            modifications = modificationService.searchModifications(search.trim());
        } else if (status != null && !"Tous".equals(status)) {
            modifications = modificationService.findByStatut(StatutModification.valueOf(status));
        } else {
            modifications = modificationService.findAll();
        }

        // Apply type filter
        if (type != null && !"Tous".equals(type)) {
            final TypeEntite filterType = TypeEntite.valueOf(type);
            modifications = modifications.stream()
                .filter(m -> filterType == m.getTypeEntite())
                .collect(Collectors.toList());
        }

        // Apply action filter
        if (action != null && !"Tous".equals(action)) {
            final TypeAction filterAction = TypeAction.valueOf(action);
            modifications = modifications.stream()
                .filter(m -> filterAction == m.getTypeAction())
                .collect(Collectors.toList());
        }

        // Apply status filter if search was used
        if (status != null && !"Tous".equals(status) && search != null && !search.trim().isEmpty()) {
            final StatutModification filterStatus = StatutModification.valueOf(status);
            modifications = modifications.stream()
                .filter(m -> filterStatus == m.getStatut())
                .collect(Collectors.toList());
        }

        grid.setItems(modifications);
        updateStats();
    }

    private void openDetailsDialog(ModificationDemande modification) {
        // Fetch the complete modification with all fields (including documentsJson)
        ModificationDemande fullModification = modificationService.findById(modification.getId())
            .orElse(modification);
        ModificationDetailsDialog dialog = new ModificationDetailsDialog(fullModification, modificationService, () -> updateGrid());
        dialog.open();
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
