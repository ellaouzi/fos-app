package com.fosagri.application.views.agents;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.service.AdhConjointService;
import com.fosagri.application.service.AdhEnfantService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
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

import java.io.ByteArrayInputStream;

@PageTitle("Gestion des Agents")
@Route("agents")
@Menu(order = 1, icon = LineAwesomeIconUrl.USERS_SOLID)
public class AgentView extends VerticalLayout {

    @Autowired
    private AdhAgentService agentService;
    
    @Autowired
    private AdhConjointService conjointService;
    
    @Autowired
    private AdhEnfantService enfantService;

    private Grid<AdhAgent> grid;
    private TextField searchField;
    private Button addButton;

    public AgentView(AdhAgentService agentService, AdhConjointService conjointService, AdhEnfantService enfantService) {
        this.agentService = agentService;
        this.conjointService = conjointService;
        this.enfantService = enfantService;
        
        addClassName("agent-view");
        setSizeFull();
        
        createHeader();
        createSearchBar();
        createGrid();
        
        updateGrid();
    }

    private void createHeader() {
        H2 title = new H2("Gestion des Agents Agricoles");
        title.addClassName("view-title");
        add(title);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Rechercher par nom, prénom ou ID...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.addValueChangeListener(e -> updateGrid());
        searchField.setWidth("300px");

        addButton = new Button("Nouvel Agent", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openAgentDialog(new AdhAgent()));

        HorizontalLayout searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidthFull();
        searchLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        
        add(searchLayout);
    }

    private void createGrid() {
        grid = new Grid<>(AdhAgent.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        grid.setHeight("600px");

        // Photo column
        grid.addComponentColumn(agent -> createAvatarComponent(agent.getAgent_photo(), agent.getAgent_photo_filename()))
            .setHeader("Photo")
            .setWidth("80px")
            .setFlexGrow(0);

        grid.addColumn(AdhAgent::getIdAdh).setHeader("ID Adhérent").setAutoWidth(true);
        grid.addColumn(AdhAgent::getCodAg).setHeader("Code Agent").setAutoWidth(true);
        grid.addColumn(AdhAgent::getNOM_AG).setHeader("Nom").setAutoWidth(true);
        grid.addColumn(AdhAgent::getPR_AG).setHeader("Prénom").setAutoWidth(true);
        grid.addColumn(AdhAgent::getCIN_AG).setHeader("CIN").setAutoWidth(true);
        grid.addColumn(AdhAgent::getSex_AG).setHeader("Sexe").setAutoWidth(true);
        grid.addColumn(AdhAgent::getMail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(AdhAgent::getVille).setHeader("Ville").setAutoWidth(true);

        grid.addComponentColumn(agent -> {
            Button familyButton = new Button("Famille", VaadinIcon.FAMILY.create());
            familyButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            familyButton.addClickListener(e -> openFamilyDialog(agent));

            Button editButton = new Button("Modifier", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openAgentDialog(agent));

            Button deleteButton = new Button("Supprimer", VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDelete(agent));

            return new HorizontalLayout(familyButton, editButton, deleteButton);
        }).setHeader("Actions").setAutoWidth(true);

        add(grid);
    }

    private void updateGrid() {
        String searchTerm = searchField.getValue();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            grid.setItems(agentService.findAll());
        } else {
            grid.setItems(agentService.searchAgents(searchTerm));
        }
    }

    private void openAgentDialog(AdhAgent agent) {
        AgentFormDialog dialog = new AgentFormDialog(agent, this::saveAgent, this::closeDialog);
        dialog.open();
    }

    private void openFamilyDialog(AdhAgent agent) {
        AgentFamilyDialog dialog = new AgentFamilyDialog(agent, conjointService, enfantService);
        dialog.open();
    }

    private void saveAgent(AdhAgent agent) {
        try {
            agentService.save(agent);
            updateGrid();
            showSuccessNotification("Agent sauvegardé avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    private void closeDialog() {
        // Dialog will close automatically
    }

    private void confirmDelete(AdhAgent agent) {
        Dialog confirmDialog = new Dialog();
        confirmDialog.setHeaderTitle("Confirmer la suppression");
        
        Div content = new Div();
        content.setText("Êtes-vous sûr de vouloir supprimer l'agent " + agent.getNOM_AG() + " " + agent.getPR_AG() + " ?");
        confirmDialog.add(content);

        Button confirmButton = new Button("Supprimer", e -> {
            deleteAgent(agent);
            confirmDialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        Button cancelButton = new Button("Annuler", e -> confirmDialog.close());

        confirmDialog.getFooter().add(cancelButton, confirmButton);
        confirmDialog.open();
    }

    private void deleteAgent(AdhAgent agent) {
        try {
            agentService.delete(agent);
            updateGrid();
            showSuccessNotification("Agent supprimé avec succès!");
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
            avatar.setAlt("Photo de profil");
        } else {
            // Default avatar placeholder
            avatar.setSrc("https://via.placeholder.com/40x40/e0e0e0/666?text=?");
            avatar.setAlt("Aucune photo");
        }
        
        return avatar;
    }
}