package com.fosagri.application.views.fichiers;

import com.fosagri.application.model.Fichier;
import com.fosagri.application.service.FichierService;
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

import java.text.SimpleDateFormat;

@PageTitle("Gestion des Fichiers")
@Route("fichiers")
@Menu(order = 5, icon = LineAwesomeIconUrl.FILE_ALT_SOLID)
public class FichierView extends VerticalLayout {

    @Autowired
    private FichierService fichierService;

    private Grid<Fichier> grid;
    private TextField searchField;

    public FichierView(FichierService fichierService) {
        this.fichierService = fichierService;
        
        addClassName("fichier-view");
        setSizeFull();
        
        createHeader();
        createSearchBar();
        createGrid();
        
        updateGrid();
    }

    private void createHeader() {
        H2 title = new H2("Gestion des Fichiers");
        title.addClassName("view-title");
        add(title);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom de fichier...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateGrid());
        searchField.setWidth("300px");

        Button addButton = new Button("Nouveau Fichier", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openFichierDialog(new Fichier()));

        HorizontalLayout searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidthFull();
        searchLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        add(searchLayout);
    }

    private void createGrid() {
        grid = new Grid<>(Fichier.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("600px");

        grid.addColumn(Fichier::getCod_ag).setHeader("Code Agent").setAutoWidth(true);
        grid.addColumn(Fichier::getFileName).setHeader("Nom fichier").setAutoWidth(true);
        grid.addColumn(Fichier::getExtention).setHeader("Extension").setAutoWidth(true);
        grid.addColumn(Fichier::getIdadh).setHeader("ID Adhérent").setAutoWidth(true);
        
        grid.addColumn(fichier -> {
            if (fichier.getCreated() != null) {
                return new SimpleDateFormat("dd/MM/yyyy").format(fichier.getCreated());
            }
            return "";
        }).setHeader("Date création").setAutoWidth(true);

        grid.addColumn(fichier -> {
            if (fichier.getDesignation() != null && fichier.getDesignation().length() > 50) {
                return fichier.getDesignation().substring(0, 47) + "...";
            }
            return fichier.getDesignation() != null ? fichier.getDesignation() : "";
        }).setHeader("Désignation").setAutoWidth(true);

        grid.addColumn(Fichier::getDocument).setHeader("Document").setAutoWidth(true);

        grid.addComponentColumn(fichier -> {
            Button editButton = new Button("Modifier", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openFichierDialog(fichier));

            Button deleteButton = new Button("Supprimer", VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> deleteFichier(fichier));

            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions").setAutoWidth(true);

        add(grid);
    }

    private void updateGrid() {
        grid.setItems(fichierService.findAll());
    }

    private void openFichierDialog(Fichier fichier) {
        FichierFormDialog dialog = new FichierFormDialog(fichier, this::saveFichier, this::closeDialog);
        dialog.open();
    }

    private void saveFichier(Fichier fichier) {
        try {
            fichierService.save(fichier);
            updateGrid();
            showSuccessNotification("Fichier sauvegardé avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    private void closeDialog() {
        // Dialog will close automatically
    }

    private void deleteFichier(Fichier fichier) {
        try {
            fichierService.delete(fichier);
            updateGrid();
            showSuccessNotification("Fichier supprimé avec succès!");
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