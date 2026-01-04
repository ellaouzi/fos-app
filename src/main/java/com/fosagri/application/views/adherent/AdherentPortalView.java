package com.fosagri.application.views.adherent;

import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.entities.Reclamation;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.model.AdhEnfant;
import com.fosagri.application.security.AuthenticatedUser;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.service.AdhEnfantService;
import com.fosagri.application.service.AdhConjointService;
import com.fosagri.application.services.DemandePrestationService;
import com.fosagri.application.services.ModificationDemandeService;
import com.fosagri.application.services.PrestationRefService;
import com.fosagri.application.services.PrestationFieldService;
import com.fosagri.application.services.ReclamationService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.TabSheetVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@PageTitle("Espace Adhérent")
@Route("espace-adherent")
@Menu(order = 1, icon = LineAwesomeIconUrl.USER_CIRCLE)
@RolesAllowed({"USER", "ADHERENT"})
public class AdherentPortalView extends VerticalLayout {

    private final PrestationRefService prestationService;
    private final PrestationFieldService prestationFieldService;
    private final AdhAgentService agentService;
    private final AdhEnfantService enfantService;
    private final AdhConjointService conjointService;
    private final DemandePrestationService demandeService;
    private final ReclamationService reclamationService;
    private final ModificationDemandeService modificationService;
    private final AuthenticatedUser authenticatedUser;

    private AdhAgent currentAgent;

    // Data providers
    private final ListDataProvider<PrestationRef> prestationDataProvider = new ListDataProvider<>(new ArrayList<>());
    private final ListDataProvider<DemandePrestation> demandesDataProvider = new ListDataProvider<>(new ArrayList<>());
    private final ListDataProvider<AdhConjoint> conjointsDataProvider = new ListDataProvider<>(new ArrayList<>());
    private final ListDataProvider<AdhEnfant> enfantsDataProvider = new ListDataProvider<>(new ArrayList<>());
    private final ListDataProvider<Reclamation> reclamationsDataProvider = new ListDataProvider<>(new ArrayList<>());

    private HorizontalLayout statsLayout;
    private TabSheet tabSheet;

    public AdherentPortalView(PrestationRefService prestationService,
                              PrestationFieldService prestationFieldService,
                              AdhAgentService agentService,
                              AdhEnfantService enfantService,
                              AdhConjointService conjointService,
                              DemandePrestationService demandeService,
                              ReclamationService reclamationService,
                              ModificationDemandeService modificationService,
                              AuthenticatedUser authenticatedUser) {
        this.prestationService = prestationService;
        this.prestationFieldService = prestationFieldService;
        this.agentService = agentService;
        this.enfantService = enfantService;
        this.conjointService = conjointService;
        this.demandeService = demandeService;
        this.reclamationService = reclamationService;
        this.modificationService = modificationService;
        this.authenticatedUser = authenticatedUser;

        // Modern styling
        addClassName("adherent-portal");
        getStyle()
            .set("background", "#f1f5f9")
            .set("min-height", "100vh")
            .set("padding", "1rem");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        Optional<AdhAgent> agentOpt = authenticatedUser.getLinkedAgent();
        if (agentOpt.isPresent()) {
            currentAgent = agentOpt.get();
            createModernView();
        } else {
            createNoProfileView();
        }
    }

    private void createNoProfileView() {
        VerticalLayout container = new VerticalLayout();
        container.setSizeFull();
        container.setAlignItems(FlexComponent.Alignment.CENTER);
        container.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        Div card = createCard();
        card.getStyle()
            .set("max-width", "500px")
            .set("text-align", "center")
            .set("padding", "3rem");

        Icon icon = VaadinIcon.USER_CARD.create();
        icon.setSize("100px");
        icon.getStyle().set("color", "#3b6b35");

        H2 title = new H2("Profil Non Configuré");
        title.getStyle()
            .set("color", "#1e293b")
            .set("margin-top", "1.5rem");

        Paragraph message = new Paragraph(
            "Votre compte n'est pas encore lié à un profil adhérent. " +
            "Contactez l'administrateur avec votre CIN ou ID Adhérent."
        );
        message.getStyle()
            .set("color", "#64748b")
            .set("line-height", "1.6");

        card.add(icon, title, message);
        container.add(card);
        add(container);
    }

    private void createModernView() {
        // Header section
        add(createHeader());

        // Stats section
        add(createStatsSection());

        // TabSheet for content
        add(createTabSheet());

        // Load data
        refreshAllData();
    }

    private Component createHeader() {
        Div headerCard = new Div();
        headerCard.getStyle()
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 100%)")
            .set("margin-bottom", "1rem")
            .set("padding", "1rem 1.5rem")
            .set("border-radius", "12px")
            .set("box-shadow", "0 4px 15px rgba(59, 107, 53, 0.3)")
            .set("width", "100%")
            .set("box-sizing", "border-box");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Left side - User info
        HorizontalLayout userInfo = new HorizontalLayout();
        userInfo.setAlignItems(FlexComponent.Alignment.CENTER);
        userInfo.setSpacing(true);

        // Avatar with photo or initials fallback
        Component avatar = createAgentAvatar("50px", "1.2rem");

        // Name and ID - compact
        VerticalLayout nameSection = new VerticalLayout();
        nameSection.setPadding(false);
        nameSection.setSpacing(false);

        Span userName = new Span(currentAgent.getNOM_AG() + " " + currentAgent.getPR_AG());
        userName.getStyle()
            .set("color", "white")
            .set("font-size", "1.1rem")
            .set("font-weight", "600");

        Span userId = new Span("ID: " + (currentAgent.getIdAdh() != null ? currentAgent.getIdAdh() : currentAgent.getCIN_AG()));
        userId.getStyle()
            .set("color", "rgba(255,255,255,0.8)")
            .set("font-size", "0.8rem");

        nameSection.add(userName, userId);
        userInfo.add(avatar, nameSection);

        // Right side - Quick info and logout
        HorizontalLayout rightSection = new HorizontalLayout();
        rightSection.setAlignItems(FlexComponent.Alignment.CENTER);
        rightSection.setSpacing(true);
        rightSection.getStyle().set("gap", "0.75rem");

        HorizontalLayout quickInfo = new HorizontalLayout();
        quickInfo.setSpacing(true);
        quickInfo.getStyle().set("gap", "0.5rem");

        if (currentAgent.getMail() != null) {
            quickInfo.add(createQuickInfoItem(VaadinIcon.ENVELOPE, currentAgent.getMail()));
        }
        if (currentAgent.getNum_Tel() != null) {
            quickInfo.add(createQuickInfoItem(VaadinIcon.PHONE, currentAgent.getNum_Tel()));
        }

        // Logout button
        Button logoutButton = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        logoutButton.getStyle()
            .set("background", "rgba(255,255,255,0.2)")
            .set("color", "white")
            .set("border", "1px solid rgba(255,255,255,0.3)")
            .set("cursor", "pointer");
        logoutButton.addClickListener(e -> authenticatedUser.logout());

        rightSection.add(quickInfo, logoutButton);
        header.add(userInfo, rightSection);
        headerCard.add(header);
        return headerCard;
    }

    private Component createQuickInfoItem(VaadinIcon iconType, String text) {
        HorizontalLayout item = new HorizontalLayout();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setSpacing(false);
        item.getStyle()
            .set("background", "rgba(255,255,255,0.15)")
            .set("padding", "0.35rem 0.75rem")
            .set("border-radius", "2rem")
            .set("gap", "0.4rem");

        Icon icon = iconType.create();
        icon.setSize("14px");
        icon.getStyle().set("color", "white");

        Span label = new Span(text);
        label.getStyle()
            .set("color", "white")
            .set("font-size", "0.75rem");

        item.add(icon, label);
        return item;
    }

    private Component createStatsSection() {
        statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        statsLayout.getStyle()
            .set("gap", "1rem")
            .set("margin-bottom", "1rem")
            .set("flex-wrap", "wrap");

        updateStats();
        return statsLayout;
    }

    private void updateStats() {
        statsLayout.removeAll();

        if (currentAgent == null) return;

        try {
            long totalDemandes = demandeService.countByAgent(currentAgent);
            long totalReclamations = reclamationService.countByAgent(currentAgent);
            int conjointsCount = conjointsDataProvider.getItems().size();
            int enfantsCount = enfantsDataProvider.getItems().size();

            statsLayout.add(
                createStatCard("Demandes", String.valueOf(totalDemandes), VaadinIcon.FILE_TEXT, "#3b82f6"),
                createStatCard("Réclamations", String.valueOf(totalReclamations), VaadinIcon.EXCLAMATION_CIRCLE, "#ef4444"),
                createStatCard("Conjoints", String.valueOf(conjointsCount), VaadinIcon.USERS, "#8b5cf6"),
                createStatCard("Enfants", String.valueOf(enfantsCount), VaadinIcon.CHILD, "#10b981")
            );
        } catch (Exception e) {
            // Handle error silently
        }
    }

    private Component createStatCard(String label, String value, VaadinIcon iconType, String color) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
            .set("padding", "1rem 1.25rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.75rem")
            .set("min-width", "160px")
            .set("flex", "1");

        // Icon container
        Div iconContainer = new Div();
        iconContainer.getStyle()
            .set("width", "42px")
            .set("height", "42px")
            .set("border-radius", "10px")
            .set("background", color + "15")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("flex-shrink", "0");

        Icon icon = iconType.create();
        icon.setSize("20px");
        icon.getStyle().set("color", color);
        iconContainer.add(icon);

        // Text content
        Div textContent = new Div();
        textContent.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "1.5rem")
            .set("font-weight", "700")
            .set("color", "#1e293b")
            .set("line-height", "1.2");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.85rem");

        textContent.add(valueSpan, labelSpan);
        card.add(iconContainer, textContent);
        return card;
    }

    private Component createTabSheet() {
        tabSheet = new TabSheet();
        tabSheet.setWidthFull();
        tabSheet.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
            .set("overflow", "hidden")
            .set("--lumo-primary-color", "#3b6b35");

        // Add tabs with icons
        tabSheet.add(createTabWithIcon("Mon Profil", VaadinIcon.USER), createProfilContent());
        tabSheet.add(createTabWithIcon("Famille", VaadinIcon.FAMILY), createFamilleContent());
        tabSheet.add(createTabWithIcon("Documents", VaadinIcon.FILE_O), createDocumentsContent());
        tabSheet.add(createTabWithIcon("Prestations", VaadinIcon.GIFT), createPrestationsContent());
        tabSheet.add(createTabWithIcon("Mes Demandes", VaadinIcon.FILE_TEXT_O), createDemandesContent());
        tabSheet.add(createTabWithIcon("Réclamations", VaadinIcon.COMMENT_ELLIPSIS), createReclamationsContent());

        return tabSheet;
    }

    private Tab createTabWithIcon(String label, VaadinIcon iconType) {
        Icon icon = iconType.create();
        icon.setSize("18px");
        icon.getStyle().set("margin-right", "0.5rem");

        HorizontalLayout tabContent = new HorizontalLayout(icon, new Span(label));
        tabContent.setAlignItems(FlexComponent.Alignment.CENTER);
        tabContent.setSpacing(false);
        tabContent.getStyle().set("gap", "0.5rem");

        return new Tab(tabContent);
    }

    // ================== PROFIL CONTENT ==================
    private Component createProfilContent() {
        Div content = new Div();
        content.getStyle()
            .set("padding", "1.5rem")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "1.5rem");

        // Profile Header Card
        content.add(createProfileHeaderCard());

        // Two-column layout for info sections
        Div sectionsGrid = new Div();
        sectionsGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(350px, 1fr))")
            .set("gap", "1.5rem");

        // Identity Section
        sectionsGrid.add(createInfoSection("Identité", "#6366f1",
            createInfoRow("CIN", currentAgent.getCIN_AG(), VaadinIcon.CREDIT_CARD),
            createInfoRow("ID Adhérent", currentAgent.getIdAdh(), VaadinIcon.BARCODE),
            createInfoRow("Sexe", formatSexe(currentAgent.getSex_AG()), VaadinIcon.USER)
        ));

        // Contact Section
        sectionsGrid.add(createInfoSection("Contact", "#10b981",
            createInfoRow("Email", currentAgent.getMail(), VaadinIcon.ENVELOPE),
            createInfoRow("Téléphone", currentAgent.getNum_Tel(), VaadinIcon.PHONE)
        ));

        // Location Section
        sectionsGrid.add(createInfoSection("Adresse", "#f59e0b",
            createInfoRow("Ville", currentAgent.getVille(), VaadinIcon.MAP_MARKER),
            createInfoRow("Adresse", currentAgent.getAdresse(), VaadinIcon.HOME)
        ));

        // Personal Section
        sectionsGrid.add(createInfoSection("Situation", "#ec4899",
            createInfoRow("Situation Familiale", formatSituationFamiliale(currentAgent.getSituation_familiale()), VaadinIcon.HEART),
            createInfoRow("Date de naissance", formatDate(currentAgent.getNaissance()), VaadinIcon.CALENDAR)
        ));

        content.add(sectionsGrid);
        return content;
    }

    private Component createProfileHeaderCard() {
        Div card = new Div();
        card.getStyle()
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 100%)")
            .set("border-radius", "16px")
            .set("padding", "2rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "1.5rem")
            .set("box-shadow", "0 10px 40px rgba(59, 107, 53, 0.3)");

        // Large Avatar with photo or initials fallback
        Component avatar = createAgentAvatar("100px", "2.5rem");

        // User Info
        Div userInfo = new Div();
        userInfo.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "0.5rem");

        Span fullName = new Span(currentAgent.getNOM_AG() + " " + currentAgent.getPR_AG());
        fullName.getStyle()
            .set("color", "white")
            .set("font-size", "1.75rem")
            .set("font-weight", "700");

        Div badgesRow = new Div();
        badgesRow.getStyle()
            .set("display", "flex")
            .set("gap", "0.75rem")
            .set("flex-wrap", "wrap");

        // ID Badge
        Span idBadge = new Span("ID: " + (currentAgent.getIdAdh() != null ? currentAgent.getIdAdh() : "-"));
        idBadge.getStyle()
            .set("background", "rgba(255,255,255,0.2)")
            .set("color", "white")
            .set("padding", "0.35rem 0.75rem")
            .set("border-radius", "2rem")
            .set("font-size", "0.85rem");

        // Status Badge
        Span statusBadge = new Span("Adhérent Actif");
        statusBadge.getStyle()
            .set("background", "#d4a949")
            .set("color", "#1a1a1a")
            .set("padding", "0.35rem 0.75rem")
            .set("border-radius", "2rem")
            .set("font-size", "0.85rem")
            .set("font-weight", "500");

        badgesRow.add(idBadge, statusBadge);
        userInfo.add(fullName, badgesRow);

        // Edit button
        Button editBtn = new Button("Modifier", VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        editBtn.getStyle()
            .set("background", "rgba(255,255,255,0.2)")
            .set("color", "white")
            .set("border", "1px solid rgba(255,255,255,0.3)")
            .set("margin-left", "auto");
        editBtn.addClickListener(e -> openProfileEditDialog());

        HorizontalLayout cardContent = new HorizontalLayout(avatar, userInfo, editBtn);
        cardContent.setWidthFull();
        cardContent.setAlignItems(FlexComponent.Alignment.CENTER);
        cardContent.expand(userInfo);

        card.add(cardContent);
        return card;
    }

    private void openProfileEditDialog() {
        AdherentProfileEditDialog dialog = new AdherentProfileEditDialog(currentAgent, agentService, modificationService);
        dialog.addSaveListener(event -> {
            currentAgent = event.getAgent();
            refreshAllData();
            // Note: notification is now shown in the dialog itself
        });
        dialog.open();
    }

    private Component createInfoSection(String title, String accentColor, Component... rows) {
        Div section = new Div();
        section.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("overflow", "hidden")
            .set("border", "1px solid #e2e8f0");

        // Section Header
        Div header = new Div();
        header.getStyle()
            .set("background", accentColor + "10")
            .set("padding", "0.75rem 1rem")
            .set("border-bottom", "1px solid #e2e8f0")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.5rem");

        Div colorBar = new Div();
        colorBar.getStyle()
            .set("width", "4px")
            .set("height", "20px")
            .set("background", accentColor)
            .set("border-radius", "2px");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
            .set("font-weight", "600")
            .set("color", "#1e293b")
            .set("font-size", "0.95rem");

        header.add(colorBar, titleSpan);

        // Section Body
        Div body = new Div();
        body.getStyle().set("padding", "0");

        for (Component row : rows) {
            body.add(row);
        }

        section.add(header, body);
        return section;
    }

    private Component createInfoRow(String label, String value, VaadinIcon iconType) {
        Div row = new Div();
        row.getStyle()
            .set("display", "flex")
            .set("align-items", "center")
            .set("padding", "0.875rem 1rem")
            .set("border-bottom", "1px solid #f1f5f9")
            .set("gap", "0.75rem");

        // Icon
        Div iconBox = new Div();
        iconBox.getStyle()
            .set("width", "36px")
            .set("height", "36px")
            .set("border-radius", "8px")
            .set("background", "#f1f5f9")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("flex-shrink", "0");

        Icon icon = iconType.create();
        icon.setSize("18px");
        icon.getStyle().set("color", "#64748b");
        iconBox.add(icon);

        // Text
        Div textBox = new Div();
        textBox.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("flex", "1")
            .set("min-width", "0");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("font-size", "0.75rem")
            .set("color", "#94a3b8")
            .set("text-transform", "uppercase")
            .set("letter-spacing", "0.05em");

        Span valueSpan = new Span(value != null && !value.isEmpty() ? value : "-");
        valueSpan.getStyle()
            .set("font-weight", "500")
            .set("color", "#1e293b")
            .set("font-size", "0.95rem")
            .set("overflow", "hidden")
            .set("text-overflow", "ellipsis");

        textBox.add(labelSpan, valueSpan);
        row.add(iconBox, textBox);
        return row;
    }

    private String formatSexe(String sexe) {
        if (sexe == null) return "-";
        switch (sexe.toUpperCase()) {
            case "M": return "Masculin";
            case "F": return "Féminin";
            default: return sexe;
        }
    }

    private String formatSituationFamiliale(String situation) {
        if (situation == null) return "-";
        switch (situation.toUpperCase()) {
            case "M": return "Marié(e)";
            case "C": return "Célibataire";
            case "D": return "Divorcé(e)";
            case "V": return "Veuf(ve)";
            default: return situation;
        }
    }

    private String formatDate(java.util.Date date) {
        if (date == null) return "-";
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }

    // ================== FAMILLE CONTENT ==================
    private Component createFamilleContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        // Conjoints section with header
        HorizontalLayout conjointsHeader = new HorizontalLayout();
        conjointsHeader.setWidthFull();
        conjointsHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        conjointsHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H3 conjointsTitle = new H3("Conjoints");
        conjointsTitle.getStyle().set("color", "#1e293b").set("margin", "0");

        Button addConjointBtn = new Button("Ajouter", VaadinIcon.PLUS.create());
        addConjointBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addConjointBtn.addClickListener(e -> openConjointEditDialog(null));

        conjointsHeader.add(conjointsTitle, addConjointBtn);

        Grid<AdhConjoint> conjointsGrid = createConjointsGrid();
        conjointsGrid.setHeight("250px");

        // Enfants section with header
        HorizontalLayout enfantsHeader = new HorizontalLayout();
        enfantsHeader.setWidthFull();
        enfantsHeader.setAlignItems(FlexComponent.Alignment.CENTER);
        enfantsHeader.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        enfantsHeader.getStyle().set("margin-top", "1.5rem");

        H3 enfantsTitle = new H3("Enfants");
        enfantsTitle.getStyle().set("color", "#1e293b").set("margin", "0");

        Button addEnfantBtn = new Button("Ajouter", VaadinIcon.PLUS.create());
        addEnfantBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        addEnfantBtn.addClickListener(e -> openEnfantEditDialog(null));

        enfantsHeader.add(enfantsTitle, addEnfantBtn);

        Grid<AdhEnfant> enfantsGrid = createEnfantsGrid();
        enfantsGrid.setHeight("250px");

        content.add(conjointsHeader, conjointsGrid, enfantsHeader, enfantsGrid);
        return content;
    }

    private Grid<AdhConjoint> createConjointsGrid() {
        Grid<AdhConjoint> grid = new Grid<>(AdhConjoint.class, false);
        grid.setDataProvider(conjointsDataProvider);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Photo column
        grid.addComponentColumn(c -> createGridAvatar(
            c.getConjoint_photo(),
            c.getConjoint_photo_filename(),
            c.getConjoint_photo_contentType(),
            getInitials(c.getNOM_CONJ(), c.getPR_CONJ()),
            VaadinIcon.USER
        )).setHeader("").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(AdhConjoint::getNOM_CONJ).setHeader("Nom").setAutoWidth(true);
        grid.addColumn(AdhConjoint::getPR_CONJ).setHeader("Prénom").setAutoWidth(true);
        grid.addColumn(AdhConjoint::getCIN_CONJ).setHeader("CIN").setAutoWidth(true);
        grid.addColumn(c -> c.getDat_N_CONJ() != null ? sdf.format(c.getDat_N_CONJ()) : "-").setHeader("Naissance").setAutoWidth(true);
        grid.addColumn(AdhConjoint::getTele).setHeader("Téléphone").setAutoWidth(true);
        grid.addComponentColumn(c -> createStatusBadge(c.isValide())).setHeader("Statut").setAutoWidth(true);
        grid.addComponentColumn(c -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e -> openConjointEditDialog(c));
            return editBtn;
        }).setHeader("").setAutoWidth(true).setFlexGrow(0);

        return grid;
    }

    private void openConjointEditDialog(AdhConjoint conjoint) {
        AdherentConjointEditDialog dialog = new AdherentConjointEditDialog(conjoint, currentAgent, conjointService, modificationService);
        dialog.addSaveListener(c -> {
            refreshConjointsData();
            updateStats();
            // Note: notification is now shown in the dialog itself
        });
        dialog.open();
    }

    private Grid<AdhEnfant> createEnfantsGrid() {
        Grid<AdhEnfant> grid = new Grid<>(AdhEnfant.class, false);
        grid.setDataProvider(enfantsDataProvider);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        // Photo column
        grid.addComponentColumn(e -> createGridAvatar(
            e.getEnfant_photo(),
            e.getEnfant_photo_filename(),
            e.getEnfant_photo_contentType(),
            getInitials(e.getNom_pac(), e.getPr_pac()),
            VaadinIcon.CHILD
        )).setHeader("").setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(AdhEnfant::getNom_pac).setHeader("Nom").setAutoWidth(true);
        grid.addColumn(AdhEnfant::getPr_pac).setHeader("Prénom").setAutoWidth(true);
        grid.addColumn(e -> e.getDat_n_pac() != null ? sdf.format(e.getDat_n_pac()) : "-").setHeader("Naissance").setAutoWidth(true);
        grid.addColumn(AdhEnfant::getSex_pac).setHeader("Sexe").setAutoWidth(true);
        grid.addColumn(AdhEnfant::getNiv_INSTRUCTION).setHeader("Niveau").setAutoWidth(true);
        grid.addComponentColumn(e -> createStatusBadge(e.isValide())).setHeader("Statut").setAutoWidth(true);
        grid.addComponentColumn(e -> {
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE, ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(ev -> openEnfantEditDialog(e));
            return editBtn;
        }).setHeader("").setAutoWidth(true).setFlexGrow(0);

        return grid;
    }

    private void openEnfantEditDialog(AdhEnfant enfant) {
        AdherentEnfantEditDialog dialog = new AdherentEnfantEditDialog(enfant, currentAgent, enfantService, modificationService);
        dialog.addSaveListener(e -> {
            refreshEnfantsData();
            updateStats();
            // Note: notification is now shown in the dialog itself
        });
        dialog.open();
    }

    // ================== DOCUMENTS CONTENT ==================
    private Component createDocumentsContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        H3 title = new H3("Mes Documents");
        title.getStyle().set("color", "#1e293b");

        // Info message
        Div infoCard = new Div();
        infoCard.getStyle()
            .set("background", "#f0f7f0")
            .set("border", "1px solid #9dc29d")
            .set("border-radius", "8px")
            .set("padding", "1rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.75rem");

        Icon infoIcon = VaadinIcon.INFO_CIRCLE.create();
        infoIcon.setSize("24px");
        infoIcon.getStyle().set("color", "#3b6b35");

        Span infoText = new Span("Vos documents personnels et justificatifs sont consultables ici. " +
            "Les documents joints à vos demandes de prestations sont également archivés.");
        infoText.getStyle().set("color", "#2d5229").set("font-size", "0.9rem");

        infoCard.add(infoIcon, infoText);

        // Documents grid placeholder
        Div documentsSection = new Div();
        documentsSection.getStyle()
            .set("background", "white")
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "12px")
            .set("padding", "2rem")
            .set("text-align", "center")
            .set("margin-top", "1rem");

        Icon folderIcon = VaadinIcon.FOLDER_O.create();
        folderIcon.setSize("64px");
        folderIcon.getStyle().set("color", "#cbd5e1");

        Paragraph emptyMsg = new Paragraph("Aucun document disponible pour le moment.");
        emptyMsg.getStyle().set("color", "#64748b").set("margin-top", "1rem");

        documentsSection.add(folderIcon, emptyMsg);

        content.add(title, infoCard, documentsSection);
        return content;
    }

    // ================== PRESTATIONS CONTENT ==================
    private Component createPrestationsContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        // Toolbar
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H3 title = new H3("Prestations Disponibles");
        title.getStyle().set("color", "#1e293b").set("margin", "0");

        TextField searchField = new TextField();
        searchField.setPlaceholder("Rechercher...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.getStyle().set("--vaadin-input-field-border-radius", "2rem");
        searchField.addValueChangeListener(e -> {
            String filter = e.getValue();
            if (filter == null || filter.isEmpty()) {
                prestationDataProvider.clearFilters();
            } else {
                prestationDataProvider.setFilter(p ->
                    (p.getLabel() != null && p.getLabel().toLowerCase().contains(filter.toLowerCase())) ||
                    (p.getType() != null && p.getType().toLowerCase().contains(filter.toLowerCase()))
                );
            }
        });

        toolbar.add(title, searchField);

        // Grid
        Grid<PrestationRef> grid = new Grid<>(PrestationRef.class, false);
        grid.setDataProvider(prestationDataProvider);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeight("400px");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        grid.addColumn(PrestationRef::getLabel).setHeader("Prestation").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(PrestationRef::getType).setHeader("Type").setAutoWidth(true);
        grid.addComponentColumn(this::createAvailabilityBadge).setHeader("Disponibilité").setAutoWidth(true);
        grid.addColumn(p -> {
            if (p.getDateDu() != null && p.getDateAu() != null) {
                return sdf.format(p.getDateDu()) + " - " + sdf.format(p.getDateAu());
            }
            return "Sans limite";
        }).setHeader("Période").setAutoWidth(true);
        grid.addComponentColumn(this::createEligibilityBadge).setHeader("Éligibilité").setAutoWidth(true);
        grid.addComponentColumn(p -> {
            Button btn = new Button("Demander", VaadinIcon.PLUS_CIRCLE.create());
            btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            btn.getStyle().set("border-radius", "2rem");
            btn.addClickListener(e -> openDemandeDialog(p));
            if (!demandeService.canAgentApplyToPrestation(currentAgent, p)) {
                btn.setEnabled(false);
                btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            }
            return btn;
        }).setHeader("Action").setAutoWidth(true);

        content.add(toolbar, grid);
        return content;
    }

    // ================== DEMANDES CONTENT ==================
    private Component createDemandesContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        H3 title = new H3("Historique des Demandes");
        title.getStyle().set("color", "#1e293b");

        Grid<DemandePrestation> grid = new Grid<>(DemandePrestation.class, false);
        grid.setDataProvider(demandesDataProvider);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeight("400px");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        grid.addColumn(d -> d.getPrestation() != null ? d.getPrestation().getLabel() : "-").setHeader("Prestation").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(d -> d.getPrestation() != null ? d.getPrestation().getType() : "-").setHeader("Type").setAutoWidth(true);
        grid.addColumn(d -> d.getDateDemande() != null ? sdf.format(d.getDateDemande()) : "-").setHeader("Date").setAutoWidth(true);
        grid.addComponentColumn(this::createDemandeStatutBadge).setHeader("Statut").setAutoWidth(true);
        grid.addComponentColumn(d -> {
            Button btn = new Button(VaadinIcon.EYE.create());
            btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btn.addClickListener(e -> showDemandeDetails(d));
            return btn;
        }).setHeader("").setAutoWidth(true);

        content.add(title, grid);
        return content;
    }

    // ================== RECLAMATIONS CONTENT ==================
    private Component createReclamationsContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        H3 title = new H3("Mes Réclamations");
        title.getStyle().set("color", "#1e293b").set("margin", "0");

        Button newBtn = new Button("Nouvelle Réclamation", VaadinIcon.PLUS.create());
        newBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newBtn.getStyle().set("border-radius", "2rem");
        newBtn.addClickListener(e -> openNewReclamationDialog());

        toolbar.add(title, newBtn);

        Grid<Reclamation> grid = new Grid<>(Reclamation.class, false);
        grid.setDataProvider(reclamationsDataProvider);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeight("400px");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        grid.addColumn(Reclamation::getObjet).setHeader("Objet").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(r -> r.getType() != null ? r.getType().getLabel() : "-").setHeader("Type").setAutoWidth(true);
        grid.addColumn(r -> r.getDateCreation() != null ? sdf.format(r.getDateCreation()) : "-").setHeader("Date").setAutoWidth(true);
        grid.addComponentColumn(this::createReclamationStatutBadge).setHeader("Statut").setAutoWidth(true);
        grid.addComponentColumn(r -> {
            Button btn = new Button(VaadinIcon.EYE.create());
            btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            btn.addClickListener(e -> showReclamationDetails(r));
            return btn;
        }).setHeader("").setAutoWidth(true);

        content.add(toolbar, grid);
        return content;
    }

    // ================== HELPERS ==================
    private Div createCard() {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("box-shadow", "0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -1px rgba(0,0,0,0.06)")
            .set("padding", "1.5rem");
        return card;
    }

    private Component createStatusBadge(boolean valid) {
        Span badge = new Span(valid ? "Validé" : "En attente");
        badge.getStyle()
            .set("padding", "4px 12px")
            .set("border-radius", "9999px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500")
            .set("background", valid ? "#dcfce7" : "#fef3c7")
            .set("color", valid ? "#166534" : "#92400e");
        return badge;
    }

    private Span createAvailabilityBadge(PrestationRef p) {
        String status = getDateBasedStatus(p);
        Span badge = new Span(status);
        badge.getStyle()
            .set("padding", "4px 12px")
            .set("border-radius", "9999px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500");

        switch (status.toLowerCase()) {
            case "disponible":
            case "en cours":
                badge.getStyle().set("background", "#dcfce7").set("color", "#166534");
                break;
            case "à venir":
                badge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
                break;
            case "terminée":
                badge.getStyle().set("background", "#fee2e2").set("color", "#991b1b");
                break;
            default:
                badge.getStyle().set("background", "#f3f4f6").set("color", "#374151");
        }
        return badge;
    }

    private Span createEligibilityBadge(PrestationRef p) {
        boolean eligible = demandeService.canAgentApplyToPrestation(currentAgent, p);
        Span badge = new Span(eligible ? "Éligible" : "Non éligible");
        badge.getStyle()
            .set("padding", "4px 12px")
            .set("border-radius", "9999px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500")
            .set("background", eligible ? "#dcfce7" : "#fee2e2")
            .set("color", eligible ? "#166534" : "#991b1b");
        return badge;
    }

    private Span createDemandeStatutBadge(DemandePrestation d) {
        String statut = d.getStatut() != null ? d.getStatut() : "SOUMISE";
        Span badge = new Span(getStatutLabel(statut));
        badge.getStyle()
            .set("padding", "4px 12px")
            .set("border-radius", "9999px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500");

        switch (statut.toUpperCase()) {
            case "SOUMISE":
                badge.getStyle().set("background", "#fef3c7").set("color", "#92400e");
                break;
            case "EN_COURS":
                badge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
                break;
            case "ACCEPTEE":
                badge.getStyle().set("background", "#dcfce7").set("color", "#166534");
                break;
            case "REFUSEE":
                badge.getStyle().set("background", "#fee2e2").set("color", "#991b1b");
                break;
            default:
                badge.getStyle().set("background", "#f3f4f6").set("color", "#374151");
        }
        return badge;
    }

    private Span createReclamationStatutBadge(Reclamation r) {
        Reclamation.StatutReclamation statut = r.getStatut();
        String label = statut != null ? statut.getLabel() : "Nouvelle";
        Span badge = new Span(label);
        badge.getStyle()
            .set("padding", "4px 12px")
            .set("border-radius", "9999px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500");

        if (statut != null) {
            switch (statut) {
                case NOUVELLE:
                    badge.getStyle().set("background", "#dbeafe").set("color", "#1e40af");
                    break;
                case EN_COURS:
                    badge.getStyle().set("background", "#fef3c7").set("color", "#92400e");
                    break;
                case RESOLUE:
                    badge.getStyle().set("background", "#dcfce7").set("color", "#166534");
                    break;
                case REJETEE:
                    badge.getStyle().set("background", "#fee2e2").set("color", "#991b1b");
                    break;
                default:
                    badge.getStyle().set("background", "#f3f4f6").set("color", "#374151");
            }
        }
        return badge;
    }

    private String getStatutLabel(String statut) {
        switch (statut.toUpperCase()) {
            case "SOUMISE": return "Soumise";
            case "EN_COURS": return "En cours";
            case "ACCEPTEE": return "Acceptée";
            case "REFUSEE": return "Refusée";
            case "TERMINEE": return "Terminée";
            default: return statut;
        }
    }

    private String getDateBasedStatus(PrestationRef p) {
        Date dateDu = p.getDateDu();
        Date dateAu = p.getDateAu();

        if (dateDu == null && dateAu == null) {
            return p.isOpen() ? "Disponible" : "Non disponible";
        }

        LocalDateTime now = LocalDateTime.now();
        if (dateDu != null && dateAu != null) {
            LocalDateTime start = new java.sql.Timestamp(dateDu.getTime()).toLocalDateTime();
            LocalDateTime end = new java.sql.Timestamp(dateAu.getTime()).toLocalDateTime();

            if (now.isBefore(start.toLocalDate().atStartOfDay())) return "À venir";
            if (now.isAfter(end.toLocalDate().atStartOfDay())) return "Terminée";
            return "En cours";
        }
        return "Disponible";
    }

    // ================== DATA REFRESH ==================
    private void refreshAllData() {
        refreshConjointsData();
        refreshEnfantsData();
        refreshPrestationsData();
        refreshDemandesData();
        refreshReclamationsData();
        updateStats();
    }

    private void refreshConjointsData() {
        conjointsDataProvider.getItems().clear();
        if (currentAgent != null) {
            try {
                conjointsDataProvider.getItems().addAll(conjointService.findBasicInfoByAgent(currentAgent));
            } catch (Exception e) { }
        }
        conjointsDataProvider.refreshAll();
    }

    private void refreshEnfantsData() {
        enfantsDataProvider.getItems().clear();
        if (currentAgent != null) {
            try {
                enfantsDataProvider.getItems().addAll(enfantService.findBasicInfoByAgent(currentAgent));
            } catch (Exception e) { }
        }
        enfantsDataProvider.refreshAll();
    }

    private void refreshPrestationsData() {
        prestationDataProvider.getItems().clear();
        try {
            prestationDataProvider.getItems().addAll(prestationService.findActivePrestations());
        } catch (Exception e) { }
        prestationDataProvider.refreshAll();
    }

    private void refreshDemandesData() {
        demandesDataProvider.getItems().clear();
        if (currentAgent != null) {
            try {
                System.out.println("🔄 Refreshing demandes for agent: " + currentAgent.getNOM_AG() + " (ID: " + currentAgent.getAdhAgentId() + ")");
                List<DemandePrestation> demandes = demandeService.findByAgent(currentAgent);
                System.out.println("✅ Loaded " + demandes.size() + " demandes");
                demandesDataProvider.getItems().addAll(demandes);
            } catch (Exception e) {
                System.err.println("❌ Error loading demandes: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ currentAgent is null, cannot load demandes");
        }
        demandesDataProvider.refreshAll();
    }

    private void refreshReclamationsData() {
        reclamationsDataProvider.getItems().clear();
        if (currentAgent != null) {
            try {
                reclamationsDataProvider.getItems().addAll(reclamationService.findByAgent(currentAgent));
            } catch (Exception e) { }
        }
        reclamationsDataProvider.refreshAll();
    }

    // ================== DIALOGS ==================
    private void openDemandeDialog(PrestationRef prestation) {
        if (!demandeService.canAgentApplyToPrestation(currentAgent, prestation)) {
            Notification.show("Non éligible pour cette prestation", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        AdherentDemandeDialog dialog = new AdherentDemandeDialog(
            currentAgent, prestation, demandeService, enfantService, conjointService, prestationFieldService
        );
        dialog.addSaveListener(d -> {
            refreshDemandesData();
            updateStats();
            tabSheet.setSelectedIndex(3);
            Notification.show("Demande soumise avec succès", 4000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        dialog.open();
    }

    private void showDemandeDetails(DemandePrestation demande) {
        new DemandeDetailsDialog(demande).open();
    }

    private void openNewReclamationDialog() {
        AdherentReclamationDialog dialog = new AdherentReclamationDialog(currentAgent, reclamationService);
        dialog.addSaveListener(r -> {
            refreshReclamationsData();
            updateStats();
            Notification.show("Réclamation soumise avec succès", 4000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        dialog.open();
    }

    private void showReclamationDetails(Reclamation reclamation) {
        new AdherentReclamationDetailsDialog(reclamation).open();
    }

    // ================== AVATAR HELPERS ==================

    /**
     * Creates an avatar component for the agent with photo or initials fallback
     */
    private Component createAgentAvatar(String size, String fontSize) {
        return createAvatarFromPhoto(
            currentAgent.getAgent_photo(),
            currentAgent.getAgent_photo_filename(),
            currentAgent.getAgent_photo_contentType(),
            getInitials(currentAgent.getNOM_AG(), currentAgent.getPR_AG()),
            size, fontSize
        );
    }

    /**
     * Creates an avatar component for a conjoint with photo or initials fallback
     */
    private Component createConjointAvatar(AdhConjoint conjoint) {
        return createAvatarFromPhoto(
            conjoint.getConjoint_photo(),
            conjoint.getConjoint_photo_filename(),
            conjoint.getConjoint_photo_contentType(),
            getInitials(conjoint.getNOM_CONJ(), conjoint.getPR_CONJ()),
            "36px", "0.85rem"
        );
    }

    /**
     * Creates an avatar component for an enfant with photo or initials fallback
     */
    private Component createEnfantAvatar(AdhEnfant enfant) {
        return createAvatarFromPhoto(
            enfant.getEnfant_photo(),
            enfant.getEnfant_photo_filename(),
            enfant.getEnfant_photo_contentType(),
            getInitials(enfant.getNom_pac(), enfant.getPr_pac()),
            "36px", "0.85rem"
        );
    }

    /**
     * Creates an avatar from photo bytes or falls back to initials
     */
    private Component createAvatarFromPhoto(byte[] photoData, String filename, String contentType,
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
            // Use photo
            String safeFilename = filename != null ? filename : "photo.jpg";
            StreamResource resource = new StreamResource(safeFilename, () -> new ByteArrayInputStream(photoData));
            Image photo = new Image(resource, "Photo");
            photo.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("object-fit", "cover");
            avatar.add(photo);
            avatar.getStyle()
                .set("border", "2px solid rgba(255,255,255,0.3)");
        } else {
            // Use initials fallback
            avatar.getStyle()
                .set("background", "rgba(255,255,255,0.2)")
                .set("color", "white")
                .set("font-weight", "bold")
                .set("font-size", fontSize)
                .set("border", "2px solid rgba(255,255,255,0.3)");
            avatar.setText(initials);
        }

        return avatar;
    }

    /**
     * Creates an avatar for grids (light background version)
     */
    private Component createGridAvatar(byte[] photoData, String filename, String contentType,
            String initials, VaadinIcon fallbackIcon) {

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
            // Use photo
            String safeFilename = filename != null ? filename : "photo.jpg";
            StreamResource resource = new StreamResource(safeFilename, () -> new ByteArrayInputStream(photoData));
            Image photo = new Image(resource, "Photo");
            photo.getStyle()
                .set("width", "100%")
                .set("height", "100%")
                .set("object-fit", "cover");
            avatar.add(photo);
            avatar.getStyle()
                .set("border", "2px solid #e2e8f0");
        } else {
            // Use icon fallback
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
}
