package com.fosagri.application.views.agents;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.service.AdhConjointService;
import com.fosagri.application.service.AdhEnfantService;
import com.fosagri.application.services.ModificationDemandeService;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.server.StreamResource;
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
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;

@PageTitle("Gestion des Adhérents")
@Route(value = "admin/agents", layout = MainLayout.class)
@Menu(order = 2, icon = LineAwesomeIconUrl.USERS_SOLID)
@RolesAllowed("ADMIN")
public class AgentView extends VerticalLayout {

    @Autowired
    private AdhAgentService agentService;

    @Autowired
    private AdhConjointService conjointService;

    @Autowired
    private AdhEnfantService enfantService;

    @Autowired
    private ModificationDemandeService modificationService;

    private Grid<AdhAgent> grid;
    private TextField searchField;
    private Button addButton;

    // Stats labels
    private Span adherentsCountLabel;
    private Span conjointsCountLabel;
    private Span enfantsCountLabel;
    private Span modificationsCountLabel;

    public AgentView(AdhAgentService agentService, AdhConjointService conjointService, AdhEnfantService enfantService, ModificationDemandeService modificationService) {
        this.agentService = agentService;
        this.conjointService = conjointService;
        this.enfantService = enfantService;
        this.modificationService = modificationService;

        addClassName("agent-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("padding", "0.5rem");
        getStyle().set("gap", "0.5rem");

        createHeader();
        createStatsSection();
        createSearchBar();
        createGrid();

        updateGrid();
        updateStats();
    }

    private void createHeader() {
        H2 title = new H2("Gestion des Adhérents");
        title.addClassName("view-title");
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

        // Adherents card
        adherentsCountLabel = new Span("0");
        Component adherentsCard = createStatCard("Adherents", adherentsCountLabel, VaadinIcon.USERS, "#3b6b35");

        // Conjoints card
        conjointsCountLabel = new Span("0");
        Component conjointsCard = createStatCard("Conjoints", conjointsCountLabel, VaadinIcon.USER, "#8b5cf6");

        // Enfants card
        enfantsCountLabel = new Span("0");
        Component enfantsCard = createStatCard("Enfants", enfantsCountLabel, VaadinIcon.CHILD, "#10b981");

        // Modifications pending card
        modificationsCountLabel = new Span("0");
        Component modificationsCard = createStatCard("Modif. en attente", modificationsCountLabel, VaadinIcon.EDIT, "#f59e0b");

        statsContainer.add(adherentsCard, conjointsCard, enfantsCard, modificationsCard);
        add(statsContainer);
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
            .set("min-width", "140px");

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
        long totalAdherents = agentService.count();
        long totalConjoints = conjointService.count();
        long totalEnfants = enfantService.count();
        long pendingModifications = modificationService.countPending();

        adherentsCountLabel.setText(String.valueOf(totalAdherents));
        conjointsCountLabel.setText(String.valueOf(totalConjoints));
        enfantsCountLabel.setText(String.valueOf(totalEnfants));
        modificationsCountLabel.setText(String.valueOf(pendingModifications));
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Recherche: nom, prénom, CIN, ville, tél...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setValueChangeTimeout(300);
        searchField.addValueChangeListener(e -> updateGrid());
        searchField.setWidth("350px");
        searchField.getStyle()
            .set("--vaadin-input-field-border-radius", "1.5rem")
            .set("font-size", "0.85rem");
        searchField.setClearButtonVisible(true);

        addButton = new Button("Nouvel Adhérent", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addButton.getStyle().set("border-radius", "1.5rem");
        addButton.addClickListener(e -> openAgentDialog(new AdhAgent()));

        HorizontalLayout searchLayout = new HorizontalLayout(searchField, addButton);
        searchLayout.setAlignItems(Alignment.CENTER);
        searchLayout.setWidthFull();
        searchLayout.setJustifyContentMode(JustifyContentMode.BETWEEN);
        searchLayout.getStyle().set("margin-bottom", "0.25rem");

        add(searchLayout);
    }

    private void createGrid() {
        grid = new Grid<>(AdhAgent.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeight("100%");
        grid.getStyle()
            .set("border-radius", "8px")
            .set("box-shadow", "0 1px 4px rgba(0,0,0,0.06)")
            .set("font-size", "0.85rem")
            .set("--lumo-size-m", "48px");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Photo column with avatar
        grid.addComponentColumn(this::createAvatarComponent)
            .setHeader("")
            .setWidth("100px")
            .setFlexGrow(0);

        // Merged ID + CIN in 2 lines
        grid.addComponentColumn(this::createIdCinCell)
            .setHeader("ID / CIN").setAutoWidth(true).setSortable(true);

        // Merged Nom/Prénom + Sexe/Situation in 2 lines
        grid.addComponentColumn(this::createIdentityCell)
            .setHeader("Adhérent").setAutoWidth(true).setSortable(true);

        // Contact: Téléphone + Ville in 2 lines with icons
        grid.addComponentColumn(this::createContactCell)
            .setHeader("Contact").setAutoWidth(true);

        // Merged Conjoints + Enfants into Famille
        grid.addComponentColumn(this::createFamilyBadge)
            .setHeader("Famille").setAutoWidth(true);

        // Actions column
        grid.addComponentColumn(agent -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(false);
            actions.getStyle().set("gap", "0.25rem");

            Button familyButton = new Button(VaadinIcon.FAMILY.create());
            familyButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            familyButton.getElement().setAttribute("title", "Voir la famille");
            familyButton.addClickListener(e -> openFamilyDialog(agent));

            Button editButton = new Button(VaadinIcon.EDIT.create());
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editButton.getElement().setAttribute("title", "Modifier");
            editButton.addClickListener(e -> openAgentDialog(agent));

            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteButton.getElement().setAttribute("title", "Supprimer");
            deleteButton.addClickListener(e -> confirmDelete(agent));

            actions.add(familyButton, editButton, deleteButton);
            return actions;
        }).setHeader("Actions").setAutoWidth(true).setFlexGrow(0);

        add(grid);
    }

    private Component createAvatarComponent(AdhAgent agent) {
        // Outer wrapper to ensure proper cell height
        Div wrapper = new Div();
        wrapper.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("padding", "4px 0")
            .set("min-height", "56px");

        Div avatarContainer = new Div();
        avatarContainer.getStyle()
            .set("width", "50px")
            .set("height", "50px")
            .set("min-width", "50px")
            .set("min-height", "50px")
            .set("border-radius", "50%")
            .set("overflow", "hidden")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

        byte[] photoData = agent.getAgent_photo();
        String filename = agent.getAgent_photo_filename();
        String contentType = agent.getAgent_photo_contentType();

        if (photoData != null && photoData.length > 0 && contentType != null && contentType.startsWith("image/")) {
            // Show photo
            StreamResource resource = new StreamResource(
                filename != null ? filename : "avatar.jpg",
                () -> new ByteArrayInputStream(photoData)
            );
            Image avatar = new Image(resource, "Photo");
            avatar.setWidth("50px");
            avatar.setHeight("50px");
            avatar.getStyle()
                .set("object-fit", "cover")
                .set("border-radius", "50%");
            avatarContainer.add(avatar);
            avatarContainer.getStyle().set("border", "2px solid #e2e8f0");
        } else {
            // Show icon fallback
            avatarContainer.getStyle()
                .set("background", "#f1f5f9")
                .set("border", "2px solid #e2e8f0");

            Icon icon = VaadinIcon.USER.create();
            icon.setSize("24px");
            icon.getStyle().set("color", "#64748b");
            avatarContainer.add(icon);
        }

        wrapper.add(avatarContainer);
        return wrapper;
    }

    // Combined ID + CIN cell in 2 lines
    private Component createIdCinCell(AdhAgent agent) {
        VerticalLayout cell = new VerticalLayout();
        cell.setPadding(false);
        cell.setSpacing(false);
        cell.getStyle().set("gap", "2px");

        // Line 1: ID
        HorizontalLayout idLine = new HorizontalLayout();
        idLine.setPadding(false);
        idLine.setSpacing(false);
        idLine.setAlignItems(FlexComponent.Alignment.CENTER);
        idLine.getStyle().set("gap", "4px");

        Span idLabel = new Span("ID:");
        idLabel.getStyle()
            .set("font-size", "0.7rem")
            .set("color", "#64748b");
        Span idValue = new Span(agent.getIdAdh() != null ? agent.getIdAdh() : "-");
        idValue.getStyle()
            .set("font-size", "0.8rem")
            .set("font-weight", "600")
            .set("color", "#1e293b");
        idLine.add(idLabel, idValue);

        // Line 2: CIN
        HorizontalLayout cinLine = new HorizontalLayout();
        cinLine.setPadding(false);
        cinLine.setSpacing(false);
        cinLine.setAlignItems(FlexComponent.Alignment.CENTER);
        cinLine.getStyle().set("gap", "4px");

        Span cinLabel = new Span("CIN:");
        cinLabel.getStyle()
            .set("font-size", "0.7rem")
            .set("color", "#64748b");
        Span cinValue = new Span(agent.getCIN_AG() != null ? agent.getCIN_AG() : "-");
        cinValue.getStyle()
            .set("font-size", "0.8rem")
            .set("font-weight", "500")
            .set("color", "#475569");
        cinLine.add(cinLabel, cinValue);

        cell.add(idLine, cinLine);
        return cell;
    }

    // Combined Contact cell: Phone + City in 2 lines with icons
    private Component createContactCell(AdhAgent agent) {
        VerticalLayout cell = new VerticalLayout();
        cell.setPadding(false);
        cell.setSpacing(false);
        cell.getStyle().set("gap", "2px");

        String tel = agent.getNum_Tel();
        String ville = agent.getVille();

        // Line 1: Phone
        HorizontalLayout phoneLine = new HorizontalLayout();
        phoneLine.setPadding(false);
        phoneLine.setSpacing(false);
        phoneLine.setAlignItems(FlexComponent.Alignment.CENTER);
        phoneLine.getStyle().set("gap", "4px");

        Icon phoneIcon = VaadinIcon.PHONE.create();
        phoneIcon.setSize("12px");
        phoneIcon.getStyle().set("color", "#3b82f6");
        Span phoneValue = new Span(tel != null && !tel.isEmpty() ? tel : "-");
        phoneValue.getStyle()
            .set("font-size", "0.8rem")
            .set("color", "#1e293b");
        phoneLine.add(phoneIcon, phoneValue);

        // Line 2: City
        HorizontalLayout cityLine = new HorizontalLayout();
        cityLine.setPadding(false);
        cityLine.setSpacing(false);
        cityLine.setAlignItems(FlexComponent.Alignment.CENTER);
        cityLine.getStyle().set("gap", "4px");

        Icon cityIcon = VaadinIcon.MAP_MARKER.create();
        cityIcon.setSize("12px");
        cityIcon.getStyle().set("color", "#10b981");
        Span cityValue = new Span(ville != null && !ville.isEmpty() ? ville : "-");
        cityValue.getStyle()
            .set("font-size", "0.8rem")
            .set("color", "#475569");
        cityLine.add(cityIcon, cityValue);

        cell.add(phoneLine, cityLine);
        return cell;
    }

    // Combined Identity cell: Nom/Prénom on line 1, Sexe/Situation on line 2
    private Component createIdentityCell(AdhAgent agent) {
        VerticalLayout cell = new VerticalLayout();
        cell.setPadding(false);
        cell.setSpacing(false);
        cell.getStyle().set("gap", "2px");

        // Line 1: Nom Prénom
        String nom = agent.getNOM_AG() != null ? agent.getNOM_AG() : "";
        String prenom = agent.getPR_AG() != null ? agent.getPR_AG() : "";
        Span nameSpan = new Span(nom + " " + prenom);
        nameSpan.getStyle()
            .set("font-weight", "500")
            .set("font-size", "0.85rem")
            .set("color", "#1e293b");

        // Line 2: Sexe + Situation badges
        HorizontalLayout badges = new HorizontalLayout();
        badges.setPadding(false);
        badges.setSpacing(false);
        badges.getStyle().set("gap", "4px");

        String sexe = agent.getSex_AG();
        String situation = agent.getSituation_familiale();

        // Sexe badge
        Span sexeBadge = new Span();
        sexeBadge.getStyle()
            .set("padding", "1px 5px")
            .set("border-radius", "9999px")
            .set("font-size", "0.6rem")
            .set("font-weight", "500");

        if ("M".equalsIgnoreCase(sexe)) {
            sexeBadge.setText("M");
            sexeBadge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
        } else if ("F".equalsIgnoreCase(sexe)) {
            sexeBadge.setText("F");
            sexeBadge.getStyle().set("background", "#fce7f3").set("color", "#9d174d");
        } else {
            sexeBadge.setText("-");
            sexeBadge.getStyle().set("background", "#f3f4f6").set("color", "#6b7280");
        }

        // Situation badge
        Span sitBadge = new Span();
        sitBadge.getStyle()
            .set("padding", "1px 5px")
            .set("border-radius", "9999px")
            .set("font-size", "0.6rem")
            .set("font-weight", "500");

        if (situation == null || situation.isEmpty()) {
            sitBadge.setText("-");
            sitBadge.getStyle().set("background", "#f3f4f6").set("color", "#6b7280");
        } else {
            switch (situation.toUpperCase()) {
                case "M":
                    sitBadge.setText("Marié");
                    sitBadge.getStyle().set("background", "#dcfce7").set("color", "#166534");
                    break;
                case "C":
                    sitBadge.setText("Célib");
                    sitBadge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
                    break;
                case "D":
                    sitBadge.setText("Div");
                    sitBadge.getStyle().set("background", "#fef3c7").set("color", "#92400e");
                    break;
                case "V":
                    sitBadge.setText("Veuf");
                    sitBadge.getStyle().set("background", "#f3e8ff").set("color", "#7c3aed");
                    break;
                default:
                    sitBadge.setText(situation.substring(0, Math.min(4, situation.length())));
                    sitBadge.getStyle().set("background", "#f3f4f6").set("color", "#6b7280");
            }
        }

        badges.add(sexeBadge, sitBadge);
        cell.add(nameSpan, badges);
        return cell;
    }

    // Merged Sexe + Situation badge (kept for reference)
    private Component createSexeSituationBadge(AdhAgent agent) {
        HorizontalLayout container = new HorizontalLayout();
        container.setSpacing(false);
        container.getStyle().set("gap", "4px");

        String sexe = agent.getSex_AG();
        String situation = agent.getSituation_familiale();

        // Sexe badge
        Span sexeBadge = new Span();
        sexeBadge.getStyle()
            .set("padding", "1px 5px")
            .set("border-radius", "9999px")
            .set("font-size", "0.65rem")
            .set("font-weight", "500");

        if ("M".equalsIgnoreCase(sexe)) {
            sexeBadge.setText("M");
            sexeBadge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
        } else if ("F".equalsIgnoreCase(sexe)) {
            sexeBadge.setText("F");
            sexeBadge.getStyle().set("background", "#fce7f3").set("color", "#9d174d");
        } else {
            sexeBadge.setText("-");
            sexeBadge.getStyle().set("background", "#f3f4f6").set("color", "#6b7280");
        }

        // Situation badge (abbreviated)
        Span sitBadge = new Span();
        sitBadge.getStyle()
            .set("padding", "1px 5px")
            .set("border-radius", "9999px")
            .set("font-size", "0.65rem")
            .set("font-weight", "500");

        if (situation == null || situation.isEmpty()) {
            sitBadge.setText("-");
            sitBadge.getStyle().set("background", "#f3f4f6").set("color", "#6b7280");
        } else {
            switch (situation.toUpperCase()) {
                case "M":
                    sitBadge.setText("Mar");
                    sitBadge.getStyle().set("background", "#dcfce7").set("color", "#166534");
                    break;
                case "C":
                    sitBadge.setText("Cél");
                    sitBadge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
                    break;
                case "D":
                    sitBadge.setText("Div");
                    sitBadge.getStyle().set("background", "#fef3c7").set("color", "#92400e");
                    break;
                case "V":
                    sitBadge.setText("Veu");
                    sitBadge.getStyle().set("background", "#f3e8ff").set("color", "#7c3aed");
                    break;
                default:
                    sitBadge.setText(situation.substring(0, Math.min(3, situation.length())));
                    sitBadge.getStyle().set("background", "#f3f4f6").set("color", "#6b7280");
            }
        }

        container.add(sexeBadge, sitBadge);
        return container;
    }

    // Merged Famille badge (Conjoints + Enfants)
    private Component createFamilyBadge(AdhAgent agent) {
        long conjoints = conjointService.countByAgent(agent);
        long enfants = enfantService.countByAgent(agent);

        HorizontalLayout badge = new HorizontalLayout();
        badge.setAlignItems(FlexComponent.Alignment.CENTER);
        badge.setSpacing(false);
        badge.getStyle()
            .set("gap", "6px")
            .set("padding", "2px 6px")
            .set("border-radius", "9999px")
            .set("background", "#f1f5f9")
            .set("display", "inline-flex");

        // Conjoints
        Icon userIcon = VaadinIcon.USER.create();
        userIcon.setSize("10px");
        userIcon.getStyle().set("color", "#8b5cf6");
        Span conjSpan = new Span(String.valueOf(conjoints));
        conjSpan.getStyle().set("font-size", "0.7rem").set("font-weight", "600").set("color", "#8b5cf6");

        // Enfants
        Icon childIcon = VaadinIcon.CHILD.create();
        childIcon.setSize("10px");
        childIcon.getStyle().set("color", "#10b981");
        Span enfSpan = new Span(String.valueOf(enfants));
        enfSpan.getStyle().set("font-size", "0.7rem").set("font-weight", "600").set("color", "#10b981");

        badge.add(userIcon, conjSpan, childIcon, enfSpan);
        return badge;
    }

    private void updateGrid() {
        String searchTerm = searchField.getValue();
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            grid.setItems(agentService.findAll());
        } else {
            // Use fuzzy search for flexible matching
            grid.setItems(agentService.fuzzySearch(searchTerm));
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
            updateStats();
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

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        // Warning icon
        Div iconContainer = new Div();
        iconContainer.getStyle()
            .set("width", "60px")
            .set("height", "60px")
            .set("border-radius", "50%")
            .set("background", "#fee2e2")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("margin", "0 auto 1rem");

        Icon warningIcon = VaadinIcon.EXCLAMATION_CIRCLE.create();
        warningIcon.setSize("30px");
        warningIcon.getStyle().set("color", "#dc2626");
        iconContainer.add(warningIcon);

        Span message = new Span("Êtes-vous sûr de vouloir supprimer l'adhérent " +
            agent.getNOM_AG() + " " + agent.getPR_AG() + " ?");
        message.getStyle().set("text-align", "center").set("display", "block");

        Span warning = new Span("Cette action supprimera également tous les conjoints et enfants associés.");
        warning.getStyle()
            .set("color", "#dc2626")
            .set("font-size", "0.85rem")
            .set("text-align", "center")
            .set("display", "block")
            .set("margin-top", "0.5rem");

        content.add(iconContainer, message, warning);
        confirmDialog.add(content);

        Button confirmButton = new Button("Supprimer", e -> {
            deleteAgent(agent);
            confirmDialog.close();
        });
        confirmButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> confirmDialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        confirmDialog.getFooter().add(cancelButton, confirmButton);
        confirmDialog.open();
    }

    private void deleteAgent(AdhAgent agent) {
        try {
            agentService.delete(agent);
            updateGrid();
            updateStats();
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
}
