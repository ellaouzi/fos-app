package com.fosagri.application.views.users;

import com.fosagri.application.model.Utilisateur;
import com.fosagri.application.service.UtilisateurService;
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
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.text.SimpleDateFormat;

@PageTitle("Gestion des Utilisateurs")
@Route(value = "users", layout = MainLayout.class)
@Menu(order = 10, icon = LineAwesomeIconUrl.USER_SOLID)
@jakarta.annotation.security.RolesAllowed("ADMIN")
public class UserView extends VerticalLayout {

    @Autowired
    private UtilisateurService utilisateurService;

    private Grid<Utilisateur> grid;
    private TextField searchField;
    private String currentFilter = "";
    private CallbackDataProvider<Utilisateur, Void> dataProvider;

    public UserView(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;

        addClassName("user-view");
        setSizeFull();

        createHeader();
        createSearchBar();
        createGrid();

        configureDataProvider();
    }

    private void createHeader() {
        H2 title = new H2("Gestion des Utilisateurs");
        title.addClassName("view-title");
        add(title);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom d'utilisateur...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> {
            currentFilter = e.getValue();
            updateGrid();
        });
        searchField.setWidth("300px");

        Button addButton = new Button("Nouvel Utilisateur", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openUserDialog(new Utilisateur()));

        HorizontalLayout searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidthFull();
        searchLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        add(searchLayout);
    }

    private void createGrid() {
        grid = new Grid<>(Utilisateur.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("600px");

        grid.addColumn(Utilisateur::getUsername).setHeader("Nom d'utilisateur").setAutoWidth(true);
        grid.addColumn(Utilisateur::getNom).setHeader("Nom").setAutoWidth(true);
        grid.addColumn(Utilisateur::getPrenom).setHeader("Prénom").setAutoWidth(true);
        grid.addColumn(Utilisateur::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(Utilisateur::getCin).setHeader("CIN").setAutoWidth(true);
        grid.addColumn(Utilisateur::getPpr).setHeader("PPR").setAutoWidth(true);
        grid.addColumn(user -> user.isEnabled() ? "Actif" : "Inactif").setHeader("Statut").setAutoWidth(true);
        
        grid.addColumn(user -> {
            if (user.getCreated() != null) {
                return new SimpleDateFormat("dd/MM/yyyy").format(user.getCreated());
            }
            return "";
        }).setHeader("Date création").setAutoWidth(true);

        grid.addComponentColumn(user -> {
            Button editButton = new Button("Modifier", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openUserDialog(user));

            Button toggleButton = new Button(
                user.isEnabled() ? "Désactiver" : "Activer", 
                user.isEnabled() ? VaadinIcon.BAN.create() : VaadinIcon.CHECK.create()
            );
            toggleButton.addThemeVariants(
                ButtonVariant.LUMO_SMALL, 
                user.isEnabled() ? ButtonVariant.LUMO_ERROR : ButtonVariant.LUMO_SUCCESS
            );
            toggleButton.addClickListener(e -> toggleUserStatus(user));

            return new HorizontalLayout(editButton, toggleButton);
        }).setHeader("Actions").setAutoWidth(true);

        add(grid);
    }

    private void configureDataProvider() {
        dataProvider = DataProvider.fromCallbacks(
            query -> {
                int offset = query.getOffset();
                int limit = query.getLimit();
                PageRequest pageRequest = PageRequest.of(offset / limit, limit);

                if (currentFilter == null || currentFilter.isEmpty()) {
                    return utilisateurService.findAll(pageRequest).stream();
                } else {
                    return utilisateurService.findByUsernameContaining(currentFilter, pageRequest).stream();
                }
            },
            query -> {
                if (currentFilter == null || currentFilter.isEmpty()) {
                    return (int) utilisateurService.count();
                } else {
                    return (int) utilisateurService.countByUsernameContaining(currentFilter);
                }
            }
        );
        grid.setDataProvider(dataProvider);
    }

    private void updateGrid() {
        dataProvider.refreshAll();
    }

    private void openUserDialog(Utilisateur user) {
        UserFormDialog dialog = new UserFormDialog(user, this::saveUser, this::closeDialog);
        dialog.open();
    }

    private void saveUser(Utilisateur user) {
        try {
            utilisateurService.save(user);
            updateGrid();
            showSuccessNotification("Utilisateur sauvegardé avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    private void closeDialog() {
        // Dialog will close automatically
    }

    private void toggleUserStatus(Utilisateur user) {
        try {
            user.setEnabled(!user.isEnabled());
            utilisateurService.save(user);
            updateGrid();
            showSuccessNotification("Statut utilisateur modifié avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la modification: " + e.getMessage());
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