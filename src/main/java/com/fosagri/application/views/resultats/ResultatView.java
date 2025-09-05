package com.fosagri.application.views.resultats;

import com.fosagri.application.model.Resultat;
import com.fosagri.application.service.ResultatService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;


@PageTitle("Gestion des Résultats")
@Route("resultats")
@Menu(order = 6, icon = LineAwesomeIconUrl.CHART_BAR_SOLID)
public class ResultatView extends VerticalLayout {

    @Autowired
    private ResultatService resultatService;

    private Grid<Resultat> grid;
    private TextField searchField;

    public ResultatView(ResultatService resultatService) {
        this.resultatService = resultatService;
        
        addClassName("resultat-view");
        setSizeFull();
        
        createHeader();
        createSearchBar();
        createGrid();
        
        updateGrid();
    }

    private void createHeader() {
        H2 title = new H2("Gestion des Résultats d'Opérations");
        title.addClassName("view-title");
        add(title);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par PPR, ID adhérent ou opération...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateGrid());
        searchField.setWidth("350px");

        Button addButton = new Button("Nouveau Résultat", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openResultatDialog(new Resultat()));

        HorizontalLayout searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidthFull();
        searchLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        add(searchLayout);
    }

    private void createGrid() {
        grid = new Grid<>(Resultat.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("600px");

        grid.addColumn(Resultat::getPpr).setHeader("PPR").setAutoWidth(true);
        grid.addColumn(Resultat::getIdadh).setHeader("ID Adhérent").setAutoWidth(true);
        grid.addColumn(Resultat::getOperation).setHeader("Opération").setAutoWidth(true);
        grid.addColumn(Resultat::getStatut).setHeader("Statut").setAutoWidth(true);
        grid.addColumn(Resultat::getOperation_id).setHeader("ID Opération").setAutoWidth(true);

        grid.addComponentColumn(resultat -> {
            Button editButton = new Button("Modifier", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openResultatDialog(resultat));

            Button deleteButton = new Button("Supprimer", VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> deleteResultat(resultat));

            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions").setAutoWidth(true);

        add(grid);
    }

    private void updateGrid() {
        grid.setItems(resultatService.findAll());
    }

    private void openResultatDialog(Resultat resultat) {
        ResultatFormDialog dialog = new ResultatFormDialog(resultat, this::saveResultat, this::closeDialog);
        dialog.open();
    }

    private void saveResultat(Resultat resultat) {
        try {
            resultatService.save(resultat);
            updateGrid();
            showSuccessNotification("Résultat sauvegardé avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    private void closeDialog() {
        // Dialog will close automatically
    }

    private void deleteResultat(Resultat resultat) {
        try {
            resultatService.delete(resultat);
            updateGrid();
            showSuccessNotification("Résultat supprimé avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la suppression: " + e.getMessage());
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