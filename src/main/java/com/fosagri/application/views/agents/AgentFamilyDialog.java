package com.fosagri.application.views.agents;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.model.AdhEnfant;
import com.fosagri.application.service.AdhConjointService;
import com.fosagri.application.service.AdhEnfantService;
import com.fosagri.application.views.conjoints.ConjointFormDialog;
import com.fosagri.application.views.enfants.EnfantFormDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;

import java.text.SimpleDateFormat;
import java.util.List;

public class AgentFamilyDialog extends Dialog {

    private final AdhAgent agent;
    private final AdhConjointService conjointService;
    private final AdhEnfantService enfantService;
    
    private Grid<AdhConjoint> conjointGrid;
    private Grid<AdhEnfant> enfantGrid;
    private VerticalLayout conjointLayout;
    private VerticalLayout enfantLayout;
    private Tabs tabs;

    public AgentFamilyDialog(AdhAgent agent, AdhConjointService conjointService, AdhEnfantService enfantService) {
        this.agent = agent;
        this.conjointService = conjointService;
        this.enfantService = enfantService;

        setHeaderTitle("Famille de " + agent.getNOM_AG() + " " + agent.getPR_AG());
        setWidth("1200px");
        setHeight("700px");
        
        createContent();
        loadData();
    }

    private void createContent() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        // Create tabs
        Tab conjointsTab = new Tab("Conjoints");
        Tab enfantsTab = new Tab("Enfants");
        tabs = new Tabs(conjointsTab, enfantsTab);
        
        // Create layouts for each tab
        conjointLayout = createConjointLayout();
        enfantLayout = createEnfantLayout();
        
        // Initially show conjoints
        enfantLayout.setVisible(false);
        
        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() == conjointsTab) {
                conjointLayout.setVisible(true);
                enfantLayout.setVisible(false);
            } else {
                conjointLayout.setVisible(false);
                enfantLayout.setVisible(true);
            }
        });

        mainLayout.add(tabs, conjointLayout, enfantLayout);
        add(mainLayout);

        // Close button
        Button closeButton = new Button("Fermer", e -> close());
        getFooter().add(closeButton);
    }

    private VerticalLayout createConjointLayout() {
        VerticalLayout layout = new VerticalLayout();
        
        H3 title = new H3("Conjoints");
        
        Button addConjointButton = new Button("Nouveau Conjoint", VaadinIcon.PLUS.create());
        addConjointButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addConjointButton.addClickListener(e -> openConjointDialog(new AdhConjoint()));

        HorizontalLayout headerLayout = new HorizontalLayout(title, addConjointButton);
        headerLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
        headerLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        headerLayout.setWidthFull();

        // Create conjoint grid
        conjointGrid = new Grid<>(AdhConjoint.class, false);
        conjointGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        conjointGrid.setHeight("400px");

        conjointGrid.addColumn(AdhConjoint::getNOM_CONJ).setHeader("Nom").setAutoWidth(true);
        conjointGrid.addColumn(AdhConjoint::getPR_CONJ).setHeader("Prénom").setAutoWidth(true);
        conjointGrid.addColumn(AdhConjoint::getCIN_CONJ).setHeader("CIN").setAutoWidth(true);
        conjointGrid.addColumn(AdhConjoint::getSex_CONJ).setHeader("Sexe").setAutoWidth(true);
        conjointGrid.addColumn(conjoint -> {
            if (conjoint.getDat_N_CONJ() != null) {
                return new SimpleDateFormat("dd/MM/yyyy").format(conjoint.getDat_N_CONJ());
            }
            return "";
        }).setHeader("Date naissance").setAutoWidth(true);
        conjointGrid.addColumn(AdhConjoint::getSit_CJ).setHeader("Situation").setAutoWidth(true);

        conjointGrid.addComponentColumn(conjoint -> {
            Button editButton = new Button("Modifier", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openConjointDialog(conjoint));

            Button deleteButton = new Button("Supprimer", VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> deleteConjoint(conjoint));

            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions").setAutoWidth(true);

        layout.add(headerLayout, conjointGrid);
        return layout;
    }

    private VerticalLayout createEnfantLayout() {
        VerticalLayout layout = new VerticalLayout();
        
        H3 title = new H3("Enfants");
        
        Button addEnfantButton = new Button("Nouvel Enfant", VaadinIcon.PLUS.create());
        addEnfantButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addEnfantButton.addClickListener(e -> openEnfantDialog(new AdhEnfant()));

        HorizontalLayout headerLayout = new HorizontalLayout(title, addEnfantButton);
        headerLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
        headerLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
        headerLayout.setWidthFull();

        // Create enfant grid
        enfantGrid = new Grid<>(AdhEnfant.class, false);
        enfantGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COLUMN_BORDERS);
        enfantGrid.setHeight("400px");

        enfantGrid.addColumn(AdhEnfant::getNom_pac).setHeader("Nom").setAutoWidth(true);
        enfantGrid.addColumn(AdhEnfant::getPr_pac).setHeader("Prénom").setAutoWidth(true);
        enfantGrid.addColumn(AdhEnfant::getCin_PAC).setHeader("CIN").setAutoWidth(true);
        enfantGrid.addColumn(AdhEnfant::getSex_pac).setHeader("Sexe").setAutoWidth(true);
        enfantGrid.addColumn(enfant -> {
            if (enfant.getDat_n_pac() != null) {
                return new SimpleDateFormat("dd/MM/yyyy").format(enfant.getDat_n_pac());
            }
            return "";
        }).setHeader("Date naissance").setAutoWidth(true);
        enfantGrid.addColumn(AdhEnfant::getLien_PAR).setHeader("Lien parenté").setAutoWidth(true);

        enfantGrid.addComponentColumn(enfant -> {
            Button editButton = new Button("Modifier", VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.addClickListener(e -> openEnfantDialog(enfant));

            Button deleteButton = new Button("Supprimer", VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> deleteEnfant(enfant));

            return new HorizontalLayout(editButton, deleteButton);
        }).setHeader("Actions").setAutoWidth(true);

        layout.add(headerLayout, enfantGrid);
        return layout;
    }

    private void loadData() {
        // Load conjoints
        List<AdhConjoint> conjoints = conjointService.findByCodeAgent(agent.getCodAg());
        conjointGrid.setItems(conjoints);

        // Load enfants
        List<AdhEnfant> enfants = enfantService.findByCodeAgent(agent.getCodAg());
        enfantGrid.setItems(enfants);
    }

    private void openConjointDialog(AdhConjoint conjoint) {
        // Set the agent code for new conjoints
        if (conjoint.getAdhConjointId() == 0) {
            conjoint.setCodAg(agent.getCodAg());
        }
        
        ConjointFormDialog dialog = new ConjointFormDialog(conjoint, this::saveConjoint, this::closeDialog);
        dialog.open();
    }

    private void saveConjoint(AdhConjoint conjoint) {
        try {
            conjointService.save(conjoint);
            loadData();
            showSuccessNotification("Conjoint sauvegardé avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    private void deleteConjoint(AdhConjoint conjoint) {
        try {
            conjointService.delete(conjoint);
            loadData();
            showSuccessNotification("Conjoint supprimé avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private void openEnfantDialog(AdhEnfant enfant) {
        // Set the agent code for new enfants
        if (enfant.getAdhEnfantId() == null) {
            enfant.setCodAg(agent.getCodAg());
        }
        
        EnfantFormDialog dialog = new EnfantFormDialog(enfant, this::saveEnfant, this::closeDialog);
        dialog.open();
    }

    private void saveEnfant(AdhEnfant enfant) {
        try {
            enfantService.save(enfant);
            loadData();
            showSuccessNotification("Enfant sauvegardé avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la sauvegarde: " + e.getMessage());
        }
    }

    private void deleteEnfant(AdhEnfant enfant) {
        try {
            enfantService.delete(enfant);
            loadData();
            showSuccessNotification("Enfant supprimé avec succès!");
        } catch (Exception e) {
            showErrorNotification("Erreur lors de la suppression: " + e.getMessage());
        }
    }

    private void closeDialog() {
        // Dialog will close automatically
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