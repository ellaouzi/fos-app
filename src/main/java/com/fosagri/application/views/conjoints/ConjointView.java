package com.fosagri.application.views.conjoints;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.service.AdhConjointService;
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
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;

@PageTitle("Gestion des Conjoints")
@Route("conjoints")
@Menu(order = 3, icon = LineAwesomeIconUrl.HEART_SOLID)
public class ConjointView extends VerticalLayout {

    @Autowired
    private AdhConjointService conjointService;
    
    @Autowired
    private AdhAgentService agentService;

    private Grid<AdhConjoint> grid;
    private TextField searchField;
    private ComboBox<AdhAgent> agentFilter;

    public ConjointView(AdhConjointService conjointService, AdhAgentService agentService) {
        this.conjointService = conjointService;
        this.agentService = agentService;
        
        addClassName("conjoint-view");
        setSizeFull();
        
        createHeader();
        createSearchBar();
        createGrid();
        
        updateGrid();
    }

    private void createHeader() {
        H2 title = new H2("Gestion des Conjoints");
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

        Button addButton = new Button("Nouveau Conjoint", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openConjointDialog(new AdhConjoint()));

        HorizontalLayout searchLayout = new HorizontalLayout(searchField, agentFilter, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidthFull();
        searchLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        add(searchLayout);
    }

    private void createGrid() {
        grid = new Grid<>(AdhConjoint.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("600px");

        // Photo column
        grid.addComponentColumn(conjoint -> createAvatarComponent(conjoint.getConjoint_photo(), conjoint.getConjoint_photo_filename()))
            .setHeader("Photo")
            .setWidth("80px")
            .setFlexGrow(0);

        grid.addColumn(AdhConjoint::getCodAg).setHeader("Code Agent").setAutoWidth(true);
        grid.addColumn(AdhConjoint::getNOM_CONJ).setHeader("Nom").setAutoWidth(true);
        grid.addColumn(AdhConjoint::getPR_CONJ).setHeader("Prénom").setAutoWidth(true);
        grid.addColumn(AdhConjoint::getCIN_CONJ).setHeader("CIN").setAutoWidth(true);
        grid.addColumn(AdhConjoint::getSex_CONJ).setHeader("Sexe").setAutoWidth(true);
        grid.addColumn(conjoint -> conjoint.getSit_CJ() != null ? conjoint.getSit_CJ() : "").setHeader("Situation").setAutoWidth(true);
        
        grid.addColumn(conjoint -> {
            if (conjoint.getDat_N_CONJ() != null) {
                return new SimpleDateFormat("dd/MM/yyyy").format(conjoint.getDat_N_CONJ());
            }
            return "";
        }).setHeader("Date naissance").setAutoWidth(true);

        grid.addColumn(conjoint -> conjoint.isValide() ? "Validé" : "Non validé").setHeader("Statut").setAutoWidth(true);

        grid.addComponentColumn(conjoint -> {
            Button editButton = new Button("Modifier", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openConjointDialog(conjoint));

            Button deleteButton = new Button("Supprimer", VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> deleteConjoint(conjoint));

            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions").setAutoWidth(true);

        add(grid);
    }

    private void updateGrid() {
        AdhAgent selectedAgent = agentFilter.getValue();
        if (selectedAgent != null) {
            grid.setItems(conjointService.findByCodeAgent(selectedAgent.getCodAg()));
        } else {
            grid.setItems(conjointService.findAll());
        }
    }

    private void openConjointDialog(AdhConjoint conjoint) {
        ConjointFormDialog dialog = new ConjointFormDialog(conjoint, this::saveConjoint, this::closeDialog);
        dialog.open();
    }

    private void saveConjoint(AdhConjoint conjoint) {
        try {
            conjointService.save(conjoint);
            updateGrid();
            showSuccessNotification("Conjoint sauvegardé avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    private void closeDialog() {
        // Dialog will close automatically
    }

    private void deleteConjoint(AdhConjoint conjoint) {
        try {
            conjointService.delete(conjoint);
            updateGrid();
            showSuccessNotification("Conjoint supprimé avec succès!");
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
            avatar.setAlt("Photo du conjoint");
        } else {
            // Default avatar placeholder
            avatar.setSrc("https://via.placeholder.com/40x40/e0e0e0/666?text=?");
            avatar.setAlt("Aucune photo");
        }
        
        return avatar;
    }
}