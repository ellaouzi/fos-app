package com.fosagri.application.views.enfants;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.model.AdhEnfant;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.service.AdhEnfantService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;

@PageTitle("Gestion des Enfants")
@Route(value = "enfants", layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class EnfantView extends VerticalLayout {

    @Autowired
    private AdhEnfantService enfantService;
    
    @Autowired
    private AdhAgentService agentService;

    private Grid<AdhEnfant> grid;
    private TextField searchField;
    private ComboBox<AdhAgent> agentFilter;

    public EnfantView(AdhEnfantService enfantService, AdhAgentService agentService) {
        this.enfantService = enfantService;
        this.agentService = agentService;
        
        addClassName("enfant-view");
        setSizeFull();
        
        createHeader();
        createSearchBar();
        createGrid();
        
        updateGrid();
    }

    private void createHeader() {
        H2 title = new H2("Gestion des Enfants");
        title.addClassName("view-title");
        add(title);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom ou prénom...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateGrid());
        searchField.setWidth("250px");

        agentFilter = new ComboBox<>("Filtrer par Agent");
        agentFilter.setItems(agentService.findAll());
        agentFilter.setItemLabelGenerator(agent -> agent.getNOM_AG() + " " + agent.getPR_AG() + " (" + agent.getCodAg() + ")");
        agentFilter.setPlaceholder("Tous les agents");
        agentFilter.setClearButtonVisible(true);
        agentFilter.setWidth("300px");
        agentFilter.addValueChangeListener(e -> updateGrid());

        Button addButton = new Button("Nouvel Enfant", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openEnfantDialog(new AdhEnfant()));

        HorizontalLayout searchLayout = new HorizontalLayout(searchField, agentFilter, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidthFull();
        searchLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        add(searchLayout);
    }

    private void createGrid() {
        grid = new Grid<>(AdhEnfant.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("600px");

        // Photo column
        grid.addComponentColumn(enfant -> createAvatarComponent(enfant.getEnfant_photo(), enfant.getEnfant_photo_filename()))
            .setHeader("Photo")
            .setWidth("80px")
            .setFlexGrow(0);

        grid.addColumn(AdhEnfant::getCodAg).setHeader("Code Agent").setAutoWidth(true);
        grid.addColumn(AdhEnfant::getNom_pac).setHeader("Nom").setAutoWidth(true);
        grid.addColumn(AdhEnfant::getPr_pac).setHeader("Prénom").setAutoWidth(true);
        grid.addColumn(AdhEnfant::getSex_pac).setHeader("Sexe").setAutoWidth(true);
        grid.addColumn(AdhEnfant::getCin_PAC).setHeader("CIN").setAutoWidth(true);
        grid.addColumn(enfant -> enfant.getNiv_INSTRUCTION() != null ? enfant.getNiv_INSTRUCTION() : "").setHeader("Niveau instruction").setAutoWidth(true);
        
        grid.addColumn(enfant -> {
            if (enfant.getDat_n_pac() != null) {
                return new SimpleDateFormat("dd/MM/yyyy").format(enfant.getDat_n_pac());
            }
            return "";
        }).setHeader("Date naissance").setAutoWidth(true);

        grid.addColumn(enfant -> enfant.isValide() ? "Validé" : "Non validé").setHeader("Statut").setAutoWidth(true);

        grid.addComponentColumn(enfant -> {
            Button editButton = new Button("Modifier", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openEnfantDialog(enfant));

            Button deleteButton = new Button("Supprimer", VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> deleteEnfant(enfant));

            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions").setAutoWidth(true);

        add(grid);
    }

    private void updateGrid() {
        AdhAgent selectedAgent = agentFilter.getValue();
        if (selectedAgent != null) {
            grid.setItems(enfantService.findByCodeAgent(selectedAgent.getCodAg()));
        } else {
            grid.setItems(enfantService.findAll());
        }
    }

    private void openEnfantDialog(AdhEnfant enfant) {
        EnfantFormDialog dialog = new EnfantFormDialog(enfant, this::saveEnfant, this::closeDialog);
        dialog.open();
    }

    private void saveEnfant(AdhEnfant enfant) {
        try {
            enfantService.save(enfant);
            updateGrid();
            showSuccessNotification("Enfant sauvegardé avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    private void closeDialog() {
        // Dialog will close automatically
    }

    private void deleteEnfant(AdhEnfant enfant) {
        try {
            enfantService.delete(enfant);
            updateGrid();
            showSuccessNotification("Enfant supprimé avec succès!");
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
    
    private Image createAvatarComponent(byte[] photoData, String filename) {
        Image avatar = new Image();
        avatar.setWidth("40px");
        avatar.setHeight("40px");
        avatar.getStyle().set("border-radius", "50%");
        avatar.getStyle().set("object-fit", "cover");
        avatar.getStyle().set("border", "1px solid #e0e0e0");
        
        if (photoData != null && photoData.length > 0) {
            StreamResource streamResource = new StreamResource(
                filename != null ? filename : "avatar.jpg",
                () -> new ByteArrayInputStream(photoData)
            );
            avatar.setSrc(streamResource);
            avatar.setAlt("Photo de l'enfant");
        } else {
            // Default avatar placeholder
            avatar.setSrc("https://via.placeholder.com/40x40/e0e0e0/666?text=?");
            avatar.setAlt("Aucune photo");
        }
        
        return avatar;
    }
}