package com.fosagri.application.views.admin;

import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.services.PrestationRefService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
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

@PageTitle("Gestion des Prestations")
@Route("admin/prestations")
@Menu(order = 3, icon = LineAwesomeIconUrl.CLIPBOARD_LIST_SOLID)
@RolesAllowed("ADMIN")
public class PrestationManagementView extends VerticalLayout {

    private final PrestationRefService prestationService;
    private Grid<PrestationRef> grid;
    private TextField searchField;
    private Div statsContainer;

    public PrestationManagementView(PrestationRefService prestationService) {
        this.prestationService = prestationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createStatsSection());
        add(createSearchBar());
        add(createGrid());

        updateGrid();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H2 title = new H2("Gestion des Prestations");
        title.getStyle().set("margin", "0");

        Button addButton = new Button("Nouvelle Prestation", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openPrestationDialog(null));

        header.add(title, addButton);
        return header;
    }

    private Component createStatsSection() {
        statsContainer = new Div();
        statsContainer.getStyle()
            .set("display", "flex")
            .set("gap", "1rem")
            .set("margin-bottom", "1rem");

        updateStats();
        return statsContainer;
    }

    private void updateStats() {
        statsContainer.removeAll();

        long total = prestationService.count();
        long open = prestationService.findOpenPrestations().size();
        long closed = total - open;

        statsContainer.add(
            createStatCard("Total", String.valueOf(total), "#3b82f6"),
            createStatCard("Ouvertes", String.valueOf(open), "#10b981"),
            createStatCard("Fermées", String.valueOf(closed), "#ef4444")
        );
    }

    private Component createStatCard(String label, String value, String color) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("padding", "1rem 1.5rem")
            .set("box-shadow", "0 1px 3px rgba(0,0,0,0.1)")
            .set("border-left", "4px solid " + color)
            .set("min-width", "120px");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "1.5rem")
            .set("font-weight", "700")
            .set("color", color)
            .set("display", "block");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.875rem");

        card.add(valueSpan, labelSpan);
        return card;
    }

    private Component createSearchBar() {
        HorizontalLayout searchLayout = new HorizontalLayout();
        searchLayout.setWidthFull();
        searchLayout.setAlignItems(FlexComponent.Alignment.CENTER);

        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par label...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> updateGrid());

        searchLayout.add(searchField);
        return searchLayout;
    }

    private Component createGrid() {
        grid = new Grid<>(PrestationRef.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("500px");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        grid.addColumn(PrestationRef::getId).setHeader("ID").setWidth("80px").setFlexGrow(0);
        grid.addColumn(PrestationRef::getLabel).setHeader("Label").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(PrestationRef::getType).setHeader("Type").setAutoWidth(true);
        grid.addComponentColumn(this::createStatusBadge).setHeader("Statut").setAutoWidth(true);
        grid.addColumn(p -> p.getDateDu() != null ? sdf.format(p.getDateDu()) : "-").setHeader("Date début").setAutoWidth(true);
        grid.addColumn(p -> p.getDateAu() != null ? sdf.format(p.getDateAu()) : "-").setHeader("Date fin").setAutoWidth(true);
        grid.addColumn(PrestationRef::getNombreLimit).setHeader("Limite").setAutoWidth(true);
        grid.addComponentColumn(this::createOpenBadge).setHeader("Ouvert").setAutoWidth(true);
        grid.addComponentColumn(this::createActionButtons).setHeader("Actions").setAutoWidth(true);

        return grid;
    }

    private Component createStatusBadge(PrestationRef p) {
        String status = p.getStatut() != null ? p.getStatut() : "N/A";
        Span badge = new Span(status);
        badge.getStyle()
            .set("padding", "4px 8px")
            .set("border-radius", "4px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500");

        if ("ACTIVE".equalsIgnoreCase(status)) {
            badge.getStyle().set("background", "#dcfce7").set("color", "#166534");
        } else {
            badge.getStyle().set("background", "#f3f4f6").set("color", "#374151");
        }
        return badge;
    }

    private Component createOpenBadge(PrestationRef p) {
        Span badge = new Span(p.isOpen() ? "Oui" : "Non");
        badge.getStyle()
            .set("padding", "4px 8px")
            .set("border-radius", "4px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500");

        if (p.isOpen()) {
            badge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
        } else {
            badge.getStyle().set("background", "#fee2e2").set("color", "#991b1b");
        }
        return badge;
    }

    private Component createActionButtons(PrestationRef prestation) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        Button editBtn = new Button(VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        editBtn.getElement().setAttribute("title", "Modifier");
        editBtn.addClickListener(e -> openPrestationDialog(prestation));

        Button deleteBtn = new Button(VaadinIcon.TRASH.create());
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
        deleteBtn.getElement().setAttribute("title", "Supprimer");
        deleteBtn.addClickListener(e -> confirmDelete(prestation));

        actions.add(editBtn, deleteBtn);
        return actions;
    }

    private void updateGrid() {
        String search = searchField.getValue();
        List<PrestationRef> prestations;

        if (search != null && !search.trim().isEmpty()) {
            prestations = prestationService.searchByLabel(search.trim());
        } else {
            prestations = prestationService.findAll();
        }

        grid.setItems(prestations);
        updateStats();
    }

    private void openPrestationDialog(PrestationRef prestation) {
        PrestationFormDialog dialog = new PrestationFormDialog(
            prestation,
            savedPrestation -> {
                prestationService.save(savedPrestation);
                updateGrid();
                showSuccessNotification(prestation == null ? "Prestation créée" : "Prestation mise à jour");
            }
        );
        dialog.open();
    }

    private void confirmDelete(PrestationRef prestation) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmer la suppression");

        Div content = new Div();
        content.setText("Êtes-vous sûr de vouloir supprimer la prestation \"" + prestation.getLabel() + "\" ?");
        content.getStyle().set("padding", "1rem 0");
        confirmDialog.add(content);

        Button cancelBtn = new Button("Annuler", e -> confirmDialog.close());
        Button deleteBtn = new Button("Supprimer", e -> {
            try {
                prestationService.deleteById(prestation.getId());
                updateGrid();
                showSuccessNotification("Prestation supprimée");
            } catch (Exception ex) {
                showErrorNotification("Erreur: " + ex.getMessage());
            }
            confirmDialog.close();
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_ERROR);

        confirmDialog.getFooter().add(cancelBtn, deleteBtn);
        confirmDialog.open();
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
