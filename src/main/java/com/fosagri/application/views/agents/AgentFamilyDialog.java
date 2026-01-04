package com.fosagri.application.views.agents;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.model.AdhEnfant;
import com.fosagri.application.service.AdhConjointService;
import com.fosagri.application.service.AdhEnfantService;
import com.fosagri.application.views.conjoints.ConjointFormDialog;
import com.fosagri.application.views.enfants.EnfantFormDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.server.StreamResource;

import java.io.ByteArrayInputStream;
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
    private Tab conjointsTab;
    private Tab enfantsTab;
    private Span conjointsCountBadge;
    private Span enfantsCountBadge;

    public AgentFamilyDialog(AdhAgent agent, AdhConjointService conjointService, AdhEnfantService enfantService) {
        this.agent = agent;
        this.conjointService = conjointService;
        this.enfantService = enfantService;

        setHeaderTitle("Famille de l'adhérent");
        setWidth("1200px");
        setHeight("750px");

        createContent();
        loadData();
    }

    private void createContent() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);

        // Agent header card
        mainLayout.add(createAgentHeader());

        // Create tabs with count badges
        conjointsCountBadge = createCountBadge(0);
        enfantsCountBadge = createCountBadge(0);

        HorizontalLayout conjointsTabContent = new HorizontalLayout();
        conjointsTabContent.setAlignItems(FlexComponent.Alignment.CENTER);
        conjointsTabContent.setSpacing(false);
        conjointsTabContent.getStyle().set("gap", "0.5rem");
        Icon conjIcon = VaadinIcon.USER.create();
        conjIcon.setSize("16px");
        conjointsTabContent.add(conjIcon, new Span("Conjoints"), conjointsCountBadge);

        HorizontalLayout enfantsTabContent = new HorizontalLayout();
        enfantsTabContent.setAlignItems(FlexComponent.Alignment.CENTER);
        enfantsTabContent.setSpacing(false);
        enfantsTabContent.getStyle().set("gap", "0.5rem");
        Icon enfIcon = VaadinIcon.CHILD.create();
        enfIcon.setSize("16px");
        enfantsTabContent.add(enfIcon, new Span("Enfants"), enfantsCountBadge);

        conjointsTab = new Tab(conjointsTabContent);
        enfantsTab = new Tab(enfantsTabContent);
        tabs = new Tabs(conjointsTab, enfantsTab);
        tabs.getStyle().set("margin-top", "1rem");

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
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        getFooter().add(closeButton);
    }

    private Component createAgentHeader() {
        Div card = new Div();
        card.getStyle()
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 100%)")
            .set("border-radius", "12px")
            .set("padding", "1.25rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "1rem");

        // Avatar
        Component avatar = createAvatar(
            agent.getAgent_photo(),
            agent.getAgent_photo_filename(),
            agent.getAgent_photo_contentType(),
            getInitials(agent.getNOM_AG(), agent.getPR_AG()),
            "70px", "1.5rem"
        );

        // Info section
        Div info = new Div();
        info.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "0.25rem")
            .set("flex", "1");

        Span fullName = new Span(agent.getNOM_AG() + " " + agent.getPR_AG());
        fullName.getStyle()
            .set("color", "white")
            .set("font-size", "1.5rem")
            .set("font-weight", "700");

        HorizontalLayout badges = new HorizontalLayout();
        badges.setSpacing(false);
        badges.getStyle().set("gap", "0.5rem");

        Span idBadge = createInfoBadge("ID: " + (agent.getIdAdh() != null ? agent.getIdAdh() : "-"));
        Span cinBadge = createInfoBadge("CIN: " + (agent.getCIN_AG() != null ? agent.getCIN_AG() : "-"));

        badges.add(idBadge, cinBadge);

        if (agent.getNum_Tel() != null && !agent.getNum_Tel().isEmpty()) {
            badges.add(createInfoBadge(agent.getNum_Tel()));
        }

        info.add(fullName, badges);
        card.add(avatar, info);

        return card;
    }

    private Span createInfoBadge(String text) {
        Span badge = new Span(text);
        badge.getStyle()
            .set("background", "rgba(255,255,255,0.2)")
            .set("color", "white")
            .set("padding", "0.25rem 0.75rem")
            .set("border-radius", "9999px")
            .set("font-size", "0.8rem");
        return badge;
    }

    private Span createCountBadge(int count) {
        Span badge = new Span(String.valueOf(count));
        badge.getStyle()
            .set("background", "#3b6b35")
            .set("color", "white")
            .set("padding", "2px 8px")
            .set("border-radius", "9999px")
            .set("font-size", "0.75rem")
            .set("font-weight", "600")
            .set("min-width", "20px")
            .set("text-align", "center");
        return badge;
    }

    private VerticalLayout createConjointLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        Button addConjointButton = new Button("Nouveau Conjoint", VaadinIcon.PLUS.create());
        addConjointButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addConjointButton.getStyle().set("border-radius", "2rem");
        addConjointButton.addClickListener(e -> openConjointDialog(new AdhConjoint()));

        HorizontalLayout headerLayout = new HorizontalLayout(addConjointButton);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        headerLayout.setWidthFull();

        // Create conjoint grid
        conjointGrid = new Grid<>(AdhConjoint.class, false);
        conjointGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        conjointGrid.setHeight("380px");
        conjointGrid.getStyle()
            .set("border-radius", "8px")
            .set("border", "1px solid #e2e8f0");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Photo column
        conjointGrid.addComponentColumn(c -> createGridAvatar(
            c.getConjoint_photo(),
            c.getConjoint_photo_filename(),
            c.getConjoint_photo_contentType(),
            VaadinIcon.USER
        )).setHeader("").setWidth("60px").setFlexGrow(0);

        conjointGrid.addColumn(AdhConjoint::getNOM_CONJ).setHeader("Nom").setAutoWidth(true);
        conjointGrid.addColumn(AdhConjoint::getPR_CONJ).setHeader("Prénom").setAutoWidth(true);
        conjointGrid.addColumn(AdhConjoint::getCIN_CONJ).setHeader("CIN").setAutoWidth(true);
        conjointGrid.addComponentColumn(c -> createSexeBadge(c.getSex_CONJ())).setHeader("Sexe").setAutoWidth(true);
        conjointGrid.addColumn(c -> c.getDat_N_CONJ() != null ? sdf.format(c.getDat_N_CONJ()) : "-")
            .setHeader("Naissance").setAutoWidth(true);
        conjointGrid.addColumn(AdhConjoint::getTele).setHeader("Téléphone").setAutoWidth(true);
        conjointGrid.addComponentColumn(c -> createStatusBadge(c.isValide())).setHeader("Statut").setAutoWidth(true);

        conjointGrid.addComponentColumn(conjoint -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(false);
            actions.getStyle().set("gap", "0.25rem");

            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.getElement().setAttribute("title", "Modifier");
            editButton.addClickListener(e -> openConjointDialog(conjoint));

            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.getElement().setAttribute("title", "Supprimer");
            deleteButton.addClickListener(e -> deleteConjoint(conjoint));

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);

        layout.add(headerLayout, conjointGrid);
        return layout;
    }

    private VerticalLayout createEnfantLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        Button addEnfantButton = new Button("Nouvel Enfant", VaadinIcon.PLUS.create());
        addEnfantButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addEnfantButton.getStyle().set("border-radius", "2rem");
        addEnfantButton.addClickListener(e -> openEnfantDialog(new AdhEnfant()));

        HorizontalLayout headerLayout = new HorizontalLayout(addEnfantButton);
        headerLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        headerLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        headerLayout.setWidthFull();

        // Create enfant grid
        enfantGrid = new Grid<>(AdhEnfant.class, false);
        enfantGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        enfantGrid.setHeight("380px");
        enfantGrid.getStyle()
            .set("border-radius", "8px")
            .set("border", "1px solid #e2e8f0");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Photo column
        enfantGrid.addComponentColumn(e -> createGridAvatar(
            e.getEnfant_photo(),
            e.getEnfant_photo_filename(),
            e.getEnfant_photo_contentType(),
            VaadinIcon.CHILD
        )).setHeader("").setWidth("60px").setFlexGrow(0);

        enfantGrid.addColumn(AdhEnfant::getNom_pac).setHeader("Nom").setAutoWidth(true);
        enfantGrid.addColumn(AdhEnfant::getPr_pac).setHeader("Prénom").setAutoWidth(true);
        enfantGrid.addColumn(AdhEnfant::getCin_PAC).setHeader("CIN").setAutoWidth(true);
        enfantGrid.addComponentColumn(e -> createSexeBadge(e.getSex_pac())).setHeader("Sexe").setAutoWidth(true);
        enfantGrid.addColumn(e -> e.getDat_n_pac() != null ? sdf.format(e.getDat_n_pac()) : "-")
            .setHeader("Naissance").setAutoWidth(true);
        enfantGrid.addColumn(AdhEnfant::getNiv_INSTRUCTION).setHeader("Niveau").setAutoWidth(true);
        enfantGrid.addComponentColumn(e -> createStatusBadge(e.isValide())).setHeader("Statut").setAutoWidth(true);

        enfantGrid.addComponentColumn(enfant -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(false);
            actions.getStyle().set("gap", "0.25rem");

            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.getElement().setAttribute("title", "Modifier");
            editButton.addClickListener(e -> openEnfantDialog(enfant));

            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.getElement().setAttribute("title", "Supprimer");
            deleteButton.addClickListener(e -> deleteEnfant(enfant));

            actions.add(editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);

        layout.add(headerLayout, enfantGrid);
        return layout;
    }

    private Component createAvatar(byte[] photoData, String filename, String contentType,
            String initials, String size, String fontSize) {
        Div avatar = new Div();
        avatar.getStyle()
            .set("width", size)
            .set("height", size)
            .set("border-radius", "50%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("flex-shrink", "0")
            .set("overflow", "hidden");

        if (photoData != null && photoData.length > 0 && contentType != null && contentType.startsWith("image/")) {
            String safeFilename = filename != null ? filename : "photo.jpg";
            StreamResource resource = new StreamResource(safeFilename, () -> new ByteArrayInputStream(photoData));
            Image photo = new Image(resource, "Photo");
            photo.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("object-fit", "cover");
            avatar.add(photo);
            avatar.getStyle().set("border", "3px solid rgba(255,255,255,0.3)");
        } else {
            avatar.getStyle()
                .set("background", "rgba(255,255,255,0.2)")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("font-size", fontSize)
                .set("border", "3px solid rgba(255,255,255,0.3)");
            avatar.setText(initials);
        }

        return avatar;
    }

    private Component createGridAvatar(byte[] photoData, String filename, String contentType, VaadinIcon fallbackIcon) {
        Div avatar = new Div();
        avatar.getStyle()
            .set("width", "36px")
            .set("height", "36px")
            .set("border-radius", "50%")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("flex-shrink", "0")
            .set("overflow", "hidden");

        if (photoData != null && photoData.length > 0 && contentType != null && contentType.startsWith("image/")) {
            String safeFilename = filename != null ? filename : "photo.jpg";
            StreamResource resource = new StreamResource(safeFilename, () -> new ByteArrayInputStream(photoData));
            Image photo = new Image(resource, "Photo");
            photo.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("object-fit", "cover");
            avatar.add(photo);
            avatar.getStyle().set("border", "2px solid #e2e8f0");
        } else {
            avatar.getStyle()
                .set("background", "#f1f5f9")
                .set("border", "2px solid #e2e8f0");
            Icon icon = fallbackIcon.create();
            icon.setSize("18px");
            icon.getStyle().set("color", "#64748b");
            avatar.add(icon);
        }

        return avatar;
    }

    private Component createSexeBadge(String sexe) {
        Span badge = new Span();
        badge.getStyle()
            .set("padding", "4px 10px")
            .set("border-radius", "9999px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500");

        if ("M".equalsIgnoreCase(sexe)) {
            badge.setText("M");
            badge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
        } else if ("F".equalsIgnoreCase(sexe)) {
            badge.setText("F");
            badge.getStyle().set("background", "#fce7f3").set("color", "#9d174d");
        } else {
            badge.setText("-");
            badge.getStyle().set("background", "#f3f4f6").set("color", "#6b7280");
        }

        return badge;
    }

    private Component createStatusBadge(boolean valid) {
        Span badge = new Span(valid ? "Validé" : "En attente");
        badge.getStyle()
            .set("padding", "4px 10px")
            .set("border-radius", "9999px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500")
            .set("background", valid ? "#dcfce7" : "#fef3c7")
            .set("color", valid ? "#166534" : "#92400e");
        return badge;
    }

    private String getInitials(String nom, String prenom) {
        StringBuilder initials = new StringBuilder();
        if (nom != null && !nom.isEmpty()) {
            initials.append(nom.charAt(0));
        }
        if (prenom != null && !prenom.isEmpty()) {
            initials.append(prenom.charAt(0));
        }
        return initials.toString().toUpperCase();
    }

    private void loadData() {
        // Load conjoints
        List<AdhConjoint> conjoints = conjointService.findByCodeAgent(agent.getCodAg());
        conjointGrid.setItems(conjoints);
        conjointsCountBadge.setText(String.valueOf(conjoints.size()));

        // Load enfants
        List<AdhEnfant> enfants = enfantService.findByCodeAgent(agent.getCodAg());
        enfantGrid.setItems(enfants);
        enfantsCountBadge.setText(String.valueOf(enfants.size()));
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
