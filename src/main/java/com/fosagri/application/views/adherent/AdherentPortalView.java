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
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.details.DetailsVariant;
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
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
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
@Route(value = "espace-adherent", layout = MainLayout.class)
@Menu(order = 1, icon = LineAwesomeIconUrl.USER_CIRCLE)
@RolesAllowed({"USER", "ADHERENT"})
public class AdherentPortalView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private String pendingSection = null;

    private final PrestationRefService prestationService;
    private final PrestationFieldService prestationFieldService;
    private final AdhAgentService agentService;
    private final AdhEnfantService enfantService;
    private final AdhConjointService conjointService;
    private final DemandePrestationService demandeService;
    private final ReclamationService reclamationService;
    private final ModificationDemandeService modificationService;
    private final com.fosagri.application.services.EventService eventService;
    private final com.fosagri.application.services.FosAgriKnowledgeService knowledgeService;
    private final AuthenticatedUser authenticatedUser;

    private AdhAgent currentAgent;

    // Data providers
    private final ListDataProvider<PrestationRef> prestationDataProvider = new ListDataProvider<>(new ArrayList<>());
    private final ListDataProvider<DemandePrestation> demandesDataProvider = new ListDataProvider<>(new ArrayList<>());
    private final ListDataProvider<AdhConjoint> conjointsDataProvider = new ListDataProvider<>(new ArrayList<>());
    private final ListDataProvider<AdhEnfant> enfantsDataProvider = new ListDataProvider<>(new ArrayList<>());
    private final ListDataProvider<Reclamation> reclamationsDataProvider = new ListDataProvider<>(new ArrayList<>());

    private HorizontalLayout statsLayout;
    private Div contentArea;
    private VerticalLayout familleContentContainer;
    private String currentSection = "dashboard";
    private final java.util.Map<String, Button> menuButtons = new java.util.HashMap<>();

    public AdherentPortalView(PrestationRefService prestationService,
                              PrestationFieldService prestationFieldService,
                              AdhAgentService agentService,
                              AdhEnfantService enfantService,
                              AdhConjointService conjointService,
                              DemandePrestationService demandeService,
                              ReclamationService reclamationService,
                              ModificationDemandeService modificationService,
                              com.fosagri.application.services.EventService eventService,
                              com.fosagri.application.services.FosAgriKnowledgeService knowledgeService,
                              AuthenticatedUser authenticatedUser) {
        this.prestationService = prestationService;
        this.prestationFieldService = prestationFieldService;
        this.agentService = agentService;
        this.enfantService = enfantService;
        this.conjointService = conjointService;
        this.demandeService = demandeService;
        this.reclamationService = reclamationService;
        this.modificationService = modificationService;
        this.eventService = eventService;
        this.knowledgeService = knowledgeService;
        this.authenticatedUser = authenticatedUser;

        // Modern styling - full width and height content
        addClassName("adherent-portal");
        getStyle()
            .set("background", "#f1f5f9")
            .set("padding", "1rem")
            .set("width", "100%")
            .set("height", "100%")
            .set("box-sizing", "border-box")
            .set("display", "flex")
            .set("flex-direction", "column");
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

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Read section from query parameter for initial load
        String section = event.getLocation().getQueryParameters()
            .getParameters()
            .getOrDefault("section", java.util.List.of("profil"))
            .stream()
            .findFirst()
            .orElse("profil");

        pendingSection = section;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // Read section from query parameter after navigation (handles menu clicks)
        String section = event.getLocation().getQueryParameters()
            .getParameters()
            .getOrDefault("section", java.util.List.of("profil"))
            .stream()
            .findFirst()
            .orElse("profil");

        // Always navigate to section when query parameter is present
        if (contentArea != null) {
            showSection(section);
        }
    }

    private void createModernView() {
        // Load data first (without updating stats UI)
        loadAllData();

        // Stats section
        add(createStatsSection());

        // Main layout with sidebar and content
        add(createMainLayout());

        // Now update stats UI
        updateStats();
    }

    private void loadAllData() {
        refreshConjointsData();
        refreshEnfantsData();
        refreshPrestationsData();
        refreshDemandesData();
        refreshReclamationsData();
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
        if (statsLayout == null) return;
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

    private Component createMainLayout() {
        // Content area (no sidebar - menu is in MainLayout drawer)
        contentArea = new Div();
        contentArea.getStyle()
            .set("width", "100%")
            .set("flex", "1")
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
            .set("overflow", "auto")
            .set("min-height", "0")
            .set("box-sizing", "border-box");

        // Show section from query parameter or default to profil
        String initialSection = pendingSection != null ? pendingSection : "profil";
        showSection(initialSection);

        return contentArea;
    }

    private VerticalLayout createSidebar() {
        VerticalLayout sidebar = new VerticalLayout();
        sidebar.setPadding(true);
        sidebar.setSpacing(false);
        sidebar.setWidth("220px");
        sidebar.setAlignItems(FlexComponent.Alignment.STRETCH);
        sidebar.setDefaultHorizontalComponentAlignment(FlexComponent.Alignment.START);
        sidebar.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
            .set("padding", "1rem 0.5rem")
            .set("gap", "0.25rem")
            .set("flex-shrink", "0");

        // Menu title
        Span menuTitle = new Span("Menu");
        menuTitle.getStyle()
            .set("font-size", "0.75rem")
            .set("font-weight", "600")
            .set("color", "#94a3b8")
            .set("text-transform", "uppercase")
            .set("letter-spacing", "0.05em")
            .set("padding", "0.5rem 1rem")
            .set("margin-bottom", "0.5rem");
        sidebar.add(menuTitle);

        // Menu items
        sidebar.add(createMenuItem("dashboard", "Tableau de bord", VaadinIcon.DASHBOARD));
        sidebar.add(createMenuItem("profil", "Mon Profil", VaadinIcon.USER));
        sidebar.add(createMenuItem("famille", "Famille", VaadinIcon.FAMILY));
        sidebar.add(createMenuItem("documents", "Documents", VaadinIcon.FILE_O));
        sidebar.add(createMenuItem("prestations", "Prestations", VaadinIcon.GIFT));
        sidebar.add(createMenuItem("demandes", "Mes Demandes", VaadinIcon.FILE_TEXT_O));
        sidebar.add(createMenuItem("reclamations", "Réclamations", VaadinIcon.COMMENT_ELLIPSIS));
        sidebar.add(createMenuItem("calendar", "Calendrier", VaadinIcon.CALENDAR));
        sidebar.add(createMenuItem("contacts", "Contacts", VaadinIcon.PHONE));
        sidebar.add(createMenuItem("search", "Recherche", VaadinIcon.SEARCH));

        return sidebar;
    }

    private Button createMenuItem(String sectionId, String label, VaadinIcon iconType) {
        Icon icon = iconType.create();
        icon.setSize("18px");

        Button menuItem = new Button(label, icon);
        menuItem.setWidthFull();
        menuItem.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        menuItem.getStyle()
            .set("justify-content", "flex-start")
            .set("text-align", "left")
            .set("--_lumo-button-justify-content", "flex-start")
            .set("padding", "0.75rem 1rem")
            .set("border-radius", "8px")
            .set("font-weight", "500")
            .set("color", "#64748b")
            .set("transition", "all 0.2s ease");
        menuItem.getElement().getThemeList().add("align-left");

        menuItem.addClickListener(e -> showSection(sectionId));

        menuButtons.put(sectionId, menuItem);

        // Set initial active state
        if (sectionId.equals(currentSection)) {
            setMenuItemActive(menuItem, true);
        }

        return menuItem;
    }

    private void setMenuItemActive(Button menuItem, boolean active) {
        if (active) {
            menuItem.getStyle()
                .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 100%)")
                .set("color", "white");
        } else {
            menuItem.getStyle()
                .set("background", "transparent")
                .set("color", "#64748b");
        }
    }

    private void showSection(String sectionId) {
        currentSection = sectionId;

        // Update content
        if (contentArea == null) return;
        contentArea.removeAll();
        switch (sectionId) {
            case "dashboard":
                contentArea.add(createDashboardContent());
                break;
            case "profil":
                contentArea.add(createProfilContent());
                break;
            case "famille":
                contentArea.add(createFamilleContent());
                break;
            case "documents":
                contentArea.add(createDocumentsContent());
                break;
            case "prestations":
                contentArea.add(createPrestationsContent());
                break;
            case "demandes":
                contentArea.add(createDemandesContent());
                break;
            case "reclamations":
                contentArea.add(createReclamationsContent());
                break;
            case "calendar":
                contentArea.add(createCalendarContent());
                break;
            case "contacts":
                contentArea.add(createContactsContent());
                break;
            case "search":
                contentArea.add(createSearchContent());
                break;
        }
    }

    // ================== DASHBOARD CONTENT ==================
    private Component createDashboardContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        // Welcome message
        H2 welcome = new H2("Bienvenue, " + (currentAgent.getPR_AG() != null ? currentAgent.getPR_AG() : "") + " !");
        welcome.getStyle().set("color", "#1e293b").set("margin", "0");

        Span subtitle = new Span("Voici un aperçu de votre espace adhérent");
        subtitle.getStyle().set("color", "#64748b");

        content.add(welcome, subtitle);

        // Stats cards
        HorizontalLayout statsRow = new HorizontalLayout();
        statsRow.setWidthFull();
        statsRow.setSpacing(true);
        statsRow.getStyle().set("flex-wrap", "wrap").set("gap", "1rem");

        int conjointsCount = conjointsDataProvider.getItems().size();
        int enfantsCount = enfantsDataProvider.getItems().size();
        int demandesCount = demandesDataProvider.getItems().size();
        int reclamationsCount = reclamationsDataProvider.getItems().size();

        statsRow.add(createDashboardStatCard("Famille", String.valueOf(conjointsCount + enfantsCount), "membres", VaadinIcon.FAMILY, "#3b6b35"));
        statsRow.add(createDashboardStatCard("Demandes", String.valueOf(demandesCount), "soumises", VaadinIcon.FILE_TEXT_O, "#2c5aa0"));
        statsRow.add(createDashboardStatCard("Réclamations", String.valueOf(reclamationsCount), "en cours", VaadinIcon.COMMENT_ELLIPSIS, "#f59e0b"));
        statsRow.add(createDashboardStatCard("Prestations", String.valueOf(prestationDataProvider.getItems().size()), "disponibles", VaadinIcon.GIFT, "#10b981"));

        content.add(statsRow);

        // Quick actions
        H3 actionsTitle = new H3("Actions rapides");
        actionsTitle.getStyle().set("color", "#1e293b").set("margin-top", "1rem");
        content.add(actionsTitle);

        HorizontalLayout actionsRow = new HorizontalLayout();
        actionsRow.setSpacing(true);
        actionsRow.getStyle().set("flex-wrap", "wrap").set("gap", "1rem");

        actionsRow.add(createQuickActionButton("Nouvelle demande", VaadinIcon.PLUS, "#3b6b35", () -> showSection("prestations")));
        actionsRow.add(createQuickActionButton("Voir mes demandes", VaadinIcon.LIST, "#2c5aa0", () -> showSection("demandes")));
        actionsRow.add(createQuickActionButton("Modifier profil", VaadinIcon.EDIT, "#64748b", () -> showSection("profil")));
        actionsRow.add(createQuickActionButton("Contacter support", VaadinIcon.PHONE, "#f59e0b", () -> showSection("contacts")));

        content.add(actionsRow);

        return content;
    }

    private Div createDashboardStatCard(String title, String value, String subtitle, VaadinIcon iconType, String color) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "12px")
            .set("padding", "1.25rem")
            .set("min-width", "180px")
            .set("flex", "1");

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setWidthFull();

        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("color", "#64748b").set("font-size", "0.875rem");

        Div iconBox = new Div();
        iconBox.getStyle()
            .set("width", "36px")
            .set("height", "36px")
            .set("border-radius", "8px")
            .set("background", color + "15")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");
        Icon icon = iconType.create();
        icon.setSize("18px");
        icon.getStyle().set("color", color);
        iconBox.add(icon);

        header.add(titleSpan, iconBox);

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("font-size", "2rem")
            .set("font-weight", "700")
            .set("color", "#1e293b")
            .set("display", "block")
            .set("margin-top", "0.5rem");

        Span subtitleSpan = new Span(subtitle);
        subtitleSpan.getStyle().set("color", "#94a3b8").set("font-size", "0.8rem");

        card.add(header, valueSpan, subtitleSpan);
        return card;
    }

    private Button createQuickActionButton(String label, VaadinIcon iconType, String color, Runnable action) {
        Button btn = new Button(label, iconType.create());
        btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btn.getStyle()
            .set("background", color)
            .set("border-radius", "8px");
        btn.addClickListener(e -> action.run());
        return btn;
    }

    // ================== CALENDAR CONTENT ==================
    private java.time.YearMonth currentYearMonth = java.time.YearMonth.now();
    private Div calendarGrid;
    private Span monthYearLabel;
    private VerticalLayout eventsListContainer;

    private Component createCalendarContent() {
        HorizontalLayout content = new HorizontalLayout();
        content.setSizeFull();
        content.setSpacing(true);
        content.getStyle().set("padding", "1.5rem");

        // Left side - Calendar
        VerticalLayout calendarSection = new VerticalLayout();
        calendarSection.setWidth("65%");
        calendarSection.setPadding(true);
        calendarSection.setSpacing(true);
        calendarSection.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)");

        // Calendar navigation
        HorizontalLayout calendarNav = new HorizontalLayout();
        calendarNav.setWidthFull();
        calendarNav.setAlignItems(FlexComponent.Alignment.CENTER);
        calendarNav.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button prevMonth = new Button(VaadinIcon.ANGLE_LEFT.create());
        prevMonth.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        prevMonth.addClickListener(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        Button nextMonth = new Button(VaadinIcon.ANGLE_RIGHT.create());
        nextMonth.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextMonth.addClickListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        Button todayBtn = new Button("Aujourd'hui");
        todayBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        todayBtn.addClickListener(e -> {
            currentYearMonth = java.time.YearMonth.now();
            updateCalendar();
        });

        monthYearLabel = new Span();
        monthYearLabel.getStyle()
            .set("font-size", "1.25rem")
            .set("font-weight", "600")
            .set("color", "#1e293b");

        HorizontalLayout navLeft = new HorizontalLayout(prevMonth, nextMonth, todayBtn);
        navLeft.setAlignItems(FlexComponent.Alignment.CENTER);
        navLeft.setSpacing(true);

        calendarNav.add(navLeft, monthYearLabel);
        calendarSection.add(calendarNav);

        // Day headers
        Div dayHeaders = new Div();
        dayHeaders.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(7, 1fr)")
            .set("gap", "2px")
            .set("margin-bottom", "0.5rem");

        String[] days = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (String day : days) {
            Span dayLabel = new Span(day);
            dayLabel.getStyle()
                .set("text-align", "center")
                .set("font-weight", "600")
                .set("color", "#64748b")
                .set("font-size", "0.85rem")
                .set("padding", "0.5rem");
            dayHeaders.add(dayLabel);
        }
        calendarSection.add(dayHeaders);

        // Calendar grid
        calendarGrid = new Div();
        calendarGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(7, 1fr)")
            .set("gap", "2px");
        calendarSection.add(calendarGrid);

        // Right side - Events list
        VerticalLayout eventsSection = new VerticalLayout();
        eventsSection.setWidth("35%");
        eventsSection.setPadding(true);
        eventsSection.setSpacing(true);
        eventsSection.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)")
            .set("overflow-y", "auto");

        H3 eventsTitle = new H3("Événements à venir");
        eventsTitle.getStyle()
            .set("margin", "0 0 1rem 0")
            .set("color", "#1e293b");

        eventsListContainer = new VerticalLayout();
        eventsListContainer.setPadding(false);
        eventsListContainer.setSpacing(true);

        eventsSection.add(eventsTitle, eventsListContainer);

        content.add(calendarSection, eventsSection);

        // Initial render
        updateCalendar();
        refreshEventsList();

        return content;
    }

    private void updateCalendar() {
        if (calendarGrid == null || monthYearLabel == null) return;

        // Update month/year label
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.FRENCH);
        String monthYear = currentYearMonth.format(formatter);
        monthYearLabel.setText(monthYear.substring(0, 1).toUpperCase() + monthYear.substring(1));

        // Clear and rebuild calendar grid
        calendarGrid.removeAll();

        // Get events for the month from EventService
        java.util.List<com.fosagri.application.entities.Event> monthEvents =
            eventService.findByMonth(currentYearMonth.getYear(), currentYearMonth.getMonthValue());

        java.time.LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
        int daysInMonth = currentYearMonth.lengthOfMonth();
        java.time.LocalDate today = java.time.LocalDate.now();

        // Empty cells before first day
        for (int i = 1; i < dayOfWeek; i++) {
            Div emptyCell = new Div();
            emptyCell.getStyle()
                .set("min-height", "80px")
                .set("background", "#f8fafc");
            calendarGrid.add(emptyCell);
        }

        // Day cells
        for (int day = 1; day <= daysInMonth; day++) {
            java.time.LocalDate date = currentYearMonth.atDay(day);
            calendarGrid.add(createDayCell(date, monthEvents, today));
        }
    }

    private Div createDayCell(java.time.LocalDate date, java.util.List<com.fosagri.application.entities.Event> monthEvents, java.time.LocalDate today) {
        Div cell = new Div();
        cell.getStyle()
            .set("min-height", "80px")
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "4px")
            .set("padding", "0.25rem")
            .set("cursor", "pointer")
            .set("transition", "background 0.2s");

        boolean isToday = date.equals(today);
        boolean isWeekend = date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY ||
                           date.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;

        if (isToday) {
            cell.getStyle().set("background", "#dbeafe").set("border-color", "#3b82f6");
        } else if (isWeekend) {
            cell.getStyle().set("background", "#fef3c7");
        } else {
            cell.getStyle().set("background", "white");
        }

        // Day number
        Span dayNumber = new Span(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyle()
            .set("font-weight", isToday ? "700" : "500")
            .set("color", isToday ? "#1d4ed8" : "#374151")
            .set("font-size", "0.9rem");
        cell.add(dayNumber);

        // Events for this day
        java.util.List<com.fosagri.application.entities.Event> dayEvents = monthEvents.stream()
            .filter(e -> e.getDateDebut() != null && e.getDateFin() != null)
            .filter(e -> !date.isBefore(e.getDateDebut()) && !date.isAfter(e.getDateFin()))
            .limit(3)
            .collect(java.util.stream.Collectors.toList());

        for (com.fosagri.application.entities.Event event : dayEvents) {
            Div eventTag = new Div();
            eventTag.setText(truncateText(event.getTitre(), 12));
            eventTag.getStyle()
                .set("background", event.getCouleur() != null ? event.getCouleur() : "#3b6b35")
                .set("color", "white")
                .set("font-size", "0.7rem")
                .set("padding", "1px 4px")
                .set("border-radius", "3px")
                .set("margin-top", "2px")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");
            cell.add(eventTag);
        }

        // Show more indicator
        long totalEvents = monthEvents.stream()
            .filter(e -> e.getDateDebut() != null && e.getDateFin() != null)
            .filter(e -> !date.isBefore(e.getDateDebut()) && !date.isAfter(e.getDateFin()))
            .count();
        if (totalEvents > 3) {
            Span more = new Span("+" + (totalEvents - 3) + " plus");
            more.getStyle()
                .set("font-size", "0.65rem")
                .set("color", "#6b7280")
                .set("margin-top", "2px");
            cell.add(more);
        }

        // Click handler to show day events
        cell.addClickListener(e -> showDayEventsDialog(date));

        return cell;
    }

    private void showDayEventsDialog(java.time.LocalDate date) {
        java.util.List<com.fosagri.application.entities.Event> events = eventService.findByDate(date);

        com.vaadin.flow.component.dialog.Dialog dialog = new com.vaadin.flow.component.dialog.Dialog();
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy", java.util.Locale.FRENCH);
        dialog.setHeaderTitle("Événements du " + date.format(dateFormatter));
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        if (events.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement pour cette date");
            noEvents.getStyle().set("color", "#6b7280");
            content.add(noEvents);
        } else {
            for (com.fosagri.application.entities.Event event : events) {
                content.add(createEventCard(event));
            }
        }

        dialog.add(content);
        dialog.getFooter().add(new Button("Fermer", e -> dialog.close()));
        dialog.open();
    }

    private void refreshEventsList() {
        if (eventsListContainer == null) return;
        eventsListContainer.removeAll();

        java.util.List<com.fosagri.application.entities.Event> upcoming = eventService.findUpcoming();

        if (upcoming.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement à venir");
            noEvents.getStyle().set("color", "#6b7280");
            eventsListContainer.add(noEvents);
        } else {
            upcoming.stream().limit(10).forEach(event -> {
                eventsListContainer.add(createEventCard(event));
            });
        }
    }

    private Div createEventCard(com.fosagri.application.entities.Event event) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#f8fafc")
            .set("border-radius", "8px")
            .set("padding", "1rem")
            .set("border-left", "4px solid " + (event.getCouleur() != null ? event.getCouleur() : "#3b6b35"));

        // Title
        Span title = new Span(event.getTitre());
        title.getStyle()
            .set("font-weight", "600")
            .set("color", "#1e293b")
            .set("display", "block");

        // Date
        String dateText = formatEventDate(event);
        Span date = new Span(dateText);
        date.getStyle()
            .set("font-size", "0.85rem")
            .set("color", "#64748b")
            .set("display", "block")
            .set("margin-top", "0.25rem");

        // Type badge
        if (event.getType() != null) {
            Span typeBadge = new Span(event.getType().getLabel());
            typeBadge.getStyle()
                .set("background", "#e0f2fe")
                .set("color", "#0369a1")
                .set("padding", "0.15rem 0.5rem")
                .set("border-radius", "10px")
                .set("font-size", "0.75rem")
                .set("margin-top", "0.5rem")
                .set("display", "inline-block");
            card.add(title, date, typeBadge);
        } else {
            card.add(title, date);
        }

        // Location if available
        if (event.getLieu() != null && !event.getLieu().isEmpty()) {
            HorizontalLayout locationRow = new HorizontalLayout();
            locationRow.setSpacing(true);
            locationRow.setAlignItems(FlexComponent.Alignment.CENTER);
            locationRow.getStyle().set("margin-top", "0.5rem");

            Icon locationIcon = VaadinIcon.MAP_MARKER.create();
            locationIcon.setSize("14px");
            locationIcon.getStyle().set("color", "#6b7280");

            Span location = new Span(event.getLieu());
            location.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "#6b7280");

            locationRow.add(locationIcon, location);
            card.add(locationRow);
        }

        return card;
    }

    private String formatEventDate(com.fosagri.application.entities.Event event) {
        java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy", java.util.Locale.FRENCH);
        java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        StringBuilder sb = new StringBuilder();

        if (event.getDateDebut() != null) {
            if (event.isMultiDay() && event.getDateFin() != null) {
                sb.append(event.getDateDebut().format(dateFormatter))
                  .append(" - ")
                  .append(event.getDateFin().format(dateFormatter));
            } else {
                sb.append(event.getDateDebut().format(dateFormatter));
            }

            if (!event.isJourneeEntiere() && event.getHeureDebut() != null) {
                sb.append(" à ").append(event.getHeureDebut().format(timeFormatter));
                if (event.getHeureFin() != null) {
                    sb.append(" - ").append(event.getHeureFin().format(timeFormatter));
                }
            } else if (event.isJourneeEntiere()) {
                sb.append(" (Journée entière)");
            }
        }

        return sb.toString();
    }

    private String truncateText(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 1) + "…";
    }

    // ================== CONTACTS CONTENT ==================
    private Component createContactsContent() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(true);
        content.setSpacing(true);

        H3 title = new H3("Contacts");
        title.getStyle().set("color", "#1e293b");

        Span description = new Span("Contactez-nous pour toute question ou assistance.");
        description.getStyle().set("color", "#64748b");

        // Contact cards
        Div cardsGrid = new Div();
        cardsGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fit, minmax(280px, 1fr))")
            .set("gap", "1rem")
            .set("margin-top", "1rem");

        cardsGrid.add(createContactCard("Service Adhérents", "05 22 XX XX XX", "adherents@fosagri.ma", VaadinIcon.USERS, "#3b6b35"));
        cardsGrid.add(createContactCard("Service Prestations", "05 22 XX XX XX", "prestations@fosagri.ma", VaadinIcon.GIFT, "#2c5aa0"));
        cardsGrid.add(createContactCard("Support Technique", "05 22 XX XX XX", "support@fosagri.ma", VaadinIcon.COG, "#64748b"));
        cardsGrid.add(createContactCard("Réclamations", "05 22 XX XX XX", "reclamations@fosagri.ma", VaadinIcon.COMMENT, "#f59e0b"));

        content.add(title, description, cardsGrid);
        return content;
    }

    private Div createContactCard(String name, String phone, String email, VaadinIcon iconType, String color) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "12px")
            .set("padding", "1.5rem")
            .set("transition", "all 0.2s ease");

        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("border-color", color).set("box-shadow", "0 4px 12px rgba(0,0,0,0.1)");
        });
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("border-color", "#e2e8f0").set("box-shadow", "none");
        });

        // Icon
        Div iconBox = new Div();
        iconBox.getStyle()
            .set("width", "48px")
            .set("height", "48px")
            .set("border-radius", "12px")
            .set("background", color + "15")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("margin-bottom", "1rem");
        Icon icon = iconType.create();
        icon.setSize("24px");
        icon.getStyle().set("color", color);
        iconBox.add(icon);

        Span nameSpan = new Span(name);
        nameSpan.getStyle()
            .set("display", "block")
            .set("font-weight", "600")
            .set("color", "#1e293b")
            .set("font-size", "1.1rem")
            .set("margin-bottom", "0.75rem");

        // Phone
        HorizontalLayout phoneRow = new HorizontalLayout();
        phoneRow.setAlignItems(FlexComponent.Alignment.CENTER);
        phoneRow.setSpacing(false);
        phoneRow.getStyle().set("gap", "0.5rem").set("margin-bottom", "0.5rem");
        Icon phoneIcon = VaadinIcon.PHONE.create();
        phoneIcon.setSize("14px");
        phoneIcon.getStyle().set("color", "#64748b");
        Span phoneSpan = new Span(phone);
        phoneSpan.getStyle().set("color", "#64748b").set("font-size", "0.9rem");
        phoneRow.add(phoneIcon, phoneSpan);

        // Email
        HorizontalLayout emailRow = new HorizontalLayout();
        emailRow.setAlignItems(FlexComponent.Alignment.CENTER);
        emailRow.setSpacing(false);
        emailRow.getStyle().set("gap", "0.5rem");
        Icon emailIcon = VaadinIcon.ENVELOPE.create();
        emailIcon.setSize("14px");
        emailIcon.getStyle().set("color", "#64748b");
        Span emailSpan = new Span(email);
        emailSpan.getStyle().set("color", "#64748b").set("font-size", "0.9rem");
        emailRow.add(emailIcon, emailSpan);

        card.add(iconBox, nameSpan, phoneRow, emailRow);
        return card;
    }

    // ================== SEARCH CONTENT ==================
    // ================== SEARCH CONTENT ==================
    private TextField searchTextField;
    private VerticalLayout searchResultsContainer;
    private com.vaadin.flow.component.orderedlayout.FlexLayout searchCategoryButtons;
    private com.vaadin.flow.component.orderedlayout.FlexLayout searchTypeButtons;
    private Span searchResultsCount;
    private String searchSelectedCategory = "all";
    private String searchSelectedType = "all";

    private Component createSearchContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        content.getStyle().set("max-width", "1200px").set("margin", "0 auto");

        // Search field
        searchTextField = createSearchTextField();

        // Quick suggestions
        HorizontalLayout suggestions = new HorizontalLayout();
        suggestions.getStyle()
            .set("flex-wrap", "wrap")
            .set("gap", "0.5rem")
            .set("margin-bottom", "1rem");

        String[] quickSearches = {"Club", "Bourses", "Logement", "INWI", "Assurance"};
        for (String search : quickSearches) {
            Button btn = new Button(search);
            btn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            btn.getStyle()
                .set("background", "rgba(59, 107, 53, 0.1)")
                .set("color", "#3b6b35")
                .set("border", "none")
                .set("border-radius", "20px")
                .set("cursor", "pointer");
            btn.addClickListener(e -> {
                searchTextField.setValue(search);
                performKnowledgeSearch();
            });
            suggestions.add(btn);
        }

        // Category filters
        searchCategoryButtons = createSearchCategoryFilters();

        // Type filters
        searchTypeButtons = createSearchTypeFilters();

        // Results count
        searchResultsCount = new Span();
        searchResultsCount.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.9rem")
            .set("margin-bottom", "1rem");

        // Results container
        searchResultsContainer = new VerticalLayout();
        searchResultsContainer.setPadding(false);
        searchResultsContainer.setSpacing(true);

        content.add(searchTextField, suggestions, searchCategoryButtons, searchTypeButtons, searchResultsCount, searchResultsContainer);

        // Initial load
        performKnowledgeSearch();

        return content;
    }

    private TextField createSearchTextField() {
        TextField field = new TextField();
        field.setPlaceholder("Rechercher des prestations, services, partenaires...");
        field.setWidthFull();
        field.setClearButtonVisible(true);
        field.setPrefixComponent(VaadinIcon.SEARCH.create());
        field.getStyle()
            .set("--vaadin-input-field-border-radius", "12px")
            .set("--vaadin-input-field-background", "white")
            .set("font-size", "1.1rem")
            .set("margin-bottom", "1rem");

        field.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> performKnowledgeSearch());
        field.addValueChangeListener(e -> performKnowledgeSearch());

        return field;
    }

    private com.vaadin.flow.component.orderedlayout.FlexLayout createSearchCategoryFilters() {
        com.vaadin.flow.component.orderedlayout.FlexLayout layout = new com.vaadin.flow.component.orderedlayout.FlexLayout();
        layout.getStyle()
            .set("flex-wrap", "wrap")
            .set("gap", "0.5rem")
            .set("margin-bottom", "1rem");

        for (com.fosagri.application.services.FosAgriKnowledgeService.Category category : knowledgeService.getCategories()) {
            Button btn = new Button(category.name());
            btn.getStyle()
                .set("border-radius", "20px")
                .set("padding", "0.5rem 1rem")
                .set("font-weight", "500")
                .set("transition", "all 0.2s ease");

            if (category.id().equals(searchSelectedCategory)) {
                btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            } else {
                btn.getStyle()
                    .set("background", "white")
                    .set("color", "#475569")
                    .set("border", "1px solid #e2e8f0");
            }

            btn.addClickListener(e -> {
                searchSelectedCategory = category.id();
                updateSearchCategoryButtons();
                performKnowledgeSearch();
            });

            layout.add(btn);
        }

        return layout;
    }

    private void updateSearchCategoryButtons() {
        int index = 0;
        for (com.fosagri.application.services.FosAgriKnowledgeService.Category category : knowledgeService.getCategories()) {
            Button btn = (Button) searchCategoryButtons.getComponentAt(index);
            btn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);

            if (category.id().equals(searchSelectedCategory)) {
                btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btn.getStyle().remove("background").remove("color").remove("border");
            } else {
                btn.getStyle()
                    .set("background", "white")
                    .set("color", "#475569")
                    .set("border", "1px solid #e2e8f0");
            }
            index++;
        }
    }

    private com.vaadin.flow.component.orderedlayout.FlexLayout createSearchTypeFilters() {
        com.vaadin.flow.component.orderedlayout.FlexLayout layout = new com.vaadin.flow.component.orderedlayout.FlexLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.getStyle()
            .set("gap", "0.5rem")
            .set("margin-bottom", "1rem");

        Icon filterIcon = VaadinIcon.FILTER.create();
        filterIcon.setSize("16px");
        filterIcon.getStyle().set("color", "#64748b");

        Span label = new Span("Type:");
        label.getStyle().set("color", "#64748b").set("font-size", "0.9rem");

        layout.add(filterIcon, label);

        String[][] types = {{"all", "Tous"}, {"page", "Pages Web"}, {"pdf", "Documents PDF"}, {"partner", "Partenaires"}};

        for (String[] type : types) {
            Button btn = new Button(type[1]);
            btn.getStyle()
                .set("border-radius", "8px")
                .set("padding", "0.3rem 0.75rem")
                .set("font-size", "0.85rem")
                .set("transition", "all 0.2s ease");

            if (type[0].equals(searchSelectedType)) {
                btn.getStyle()
                    .set("background", "#1e293b")
                    .set("color", "white");
            } else {
                btn.getStyle()
                    .set("background", "#f1f5f9")
                    .set("color", "#475569");
            }

            final String typeId = type[0];
            btn.addClickListener(e -> {
                searchSelectedType = typeId;
                updateSearchTypeButtons();
                performKnowledgeSearch();
            });

            layout.add(btn);
        }

        return layout;
    }

    private void updateSearchTypeButtons() {
        String[][] types = {{"all", "Tous"}, {"page", "Pages Web"}, {"pdf", "Documents PDF"}, {"partner", "Partenaires"}};
        int index = 2; // Skip filter icon and label

        for (String[] type : types) {
            Button btn = (Button) searchTypeButtons.getComponentAt(index);

            if (type[0].equals(searchSelectedType)) {
                btn.getStyle()
                    .set("background", "#1e293b")
                    .set("color", "white");
            } else {
                btn.getStyle()
                    .set("background", "#f1f5f9")
                    .set("color", "#475569");
            }
            index++;
        }
    }

    private void performKnowledgeSearch() {
        String query = searchTextField.getValue();
        java.util.List<com.fosagri.application.services.FosAgriKnowledgeService.SearchResult> results =
            knowledgeService.search(query, searchSelectedCategory, searchSelectedType);

        searchResultsCount.setText(results.size() + " résultat" + (results.size() > 1 ? "s" : "") +
            " trouvé" + (results.size() > 1 ? "s" : "") +
            (query != null && !query.isEmpty() ? " pour \"" + query + "\"" : ""));

        searchResultsContainer.removeAll();

        if (results.isEmpty()) {
            searchResultsContainer.add(createSearchEmptyState());
        } else {
            for (com.fosagri.application.services.FosAgriKnowledgeService.SearchResult result : results) {
                searchResultsContainer.add(createSearchResultCard(result));
            }
        }
    }

    private Component createSearchResultCard(com.fosagri.application.services.FosAgriKnowledgeService.SearchResult result) {
        com.fosagri.application.services.FosAgriKnowledgeService.ContentItem item = result.item();

        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("padding", "1.25rem")
            .set("box-shadow", "0 2px 12px rgba(0,0,0,0.06)")
            .set("border", "1px solid #f1f5f9")
            .set("transition", "all 0.3s ease")
            .set("cursor", "pointer");

        HorizontalLayout content = new HorizontalLayout();
        content.setWidthFull();
        content.setAlignItems(FlexComponent.Alignment.START);
        content.setSpacing(true);

        // Icon
        Div iconContainer = new Div();
        iconContainer.getStyle()
            .set("width", "48px")
            .set("height", "48px")
            .set("border-radius", "12px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("flex-shrink", "0");

        Icon icon;
        String bgColor;
        switch (item.type()) {
            case "pdf":
                icon = VaadinIcon.FILE_TEXT.create();
                bgColor = "#fee2e2";
                icon.getStyle().set("color", "#dc2626");
                break;
            case "partner":
                icon = VaadinIcon.HANDSHAKE.create();
                bgColor = "#d1fae5";
                icon.getStyle().set("color", "#059669");
                break;
            default:
                icon = VaadinIcon.GLOBE.create();
                bgColor = "#dbeafe";
                icon.getStyle().set("color", "#2563eb");
        }
        iconContainer.getStyle().set("background", bgColor);
        icon.setSize("24px");
        iconContainer.add(icon);

        // Content
        VerticalLayout textContent = new VerticalLayout();
        textContent.setPadding(false);
        textContent.setSpacing(false);
        textContent.getStyle().set("flex", "1");

        // Badges row
        HorizontalLayout badges = new HorizontalLayout();
        badges.setSpacing(true);
        badges.getStyle().set("margin-bottom", "0.5rem");

        Span typeBadge = createSearchTypeBadge(item.type());
        badges.add(typeBadge);

        if (result.relevance() > 0) {
            Span relevanceBadge = new Span("Pertinence: " + Math.min(100, result.relevance() / 2) + "%");
            relevanceBadge.getStyle()
                .set("background", "#dcfce7")
                .set("color", "#166534")
                .set("padding", "2px 8px")
                .set("border-radius", "10px")
                .set("font-size", "0.7rem")
                .set("font-weight", "500");
            badges.add(relevanceBadge);
        }

        // Title
        Span title = new Span(item.title());
        title.getStyle()
            .set("font-weight", "600")
            .set("font-size", "1.1rem")
            .set("color", "#1e293b")
            .set("display", "block")
            .set("margin-bottom", "0.25rem");

        // Description
        Span description = new Span(item.description());
        description.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.9rem")
            .set("display", "block")
            .set("margin-bottom", "0.5rem");

        // Keywords
        com.vaadin.flow.component.orderedlayout.FlexLayout keywords = new com.vaadin.flow.component.orderedlayout.FlexLayout();
        keywords.getStyle().set("flex-wrap", "wrap").set("gap", "4px");

        if (item.keywords() != null) {
            for (int i = 0; i < Math.min(5, item.keywords().size()); i++) {
                Span keyword = new Span(item.keywords().get(i));
                keyword.getStyle()
                    .set("background", "#f1f5f9")
                    .set("color", "#64748b")
                    .set("padding", "2px 8px")
                    .set("border-radius", "4px")
                    .set("font-size", "0.75rem");
                keywords.add(keyword);
            }
        }

        textContent.add(badges, title, description, keywords);

        // Action button
        com.vaadin.flow.component.html.Anchor link = new com.vaadin.flow.component.html.Anchor(item.url(), "");
        link.setTarget("_blank");

        Button actionBtn = new Button(item.type().equals("pdf") ? "Télécharger" : "Visiter");
        actionBtn.setIcon(item.type().equals("pdf") ? VaadinIcon.DOWNLOAD.create() : VaadinIcon.EXTERNAL_LINK.create());
        actionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionBtn.getStyle()
            .set("border-radius", "10px")
            .set("flex-shrink", "0");

        link.add(actionBtn);

        content.add(iconContainer, textContent, link);
        card.add(content);

        return card;
    }

    private Span createSearchTypeBadge(String type) {
        Span badge = new Span();
        badge.getStyle()
            .set("padding", "3px 10px")
            .set("border-radius", "10px")
            .set("font-size", "0.7rem")
            .set("font-weight", "600")
            .set("text-transform", "uppercase");

        switch (type) {
            case "pdf":
                badge.setText("PDF");
                badge.getStyle().set("background", "#fee2e2").set("color", "#dc2626");
                break;
            case "partner":
                badge.setText("Partenaire");
                badge.getStyle().set("background", "#d1fae5").set("color", "#059669");
                break;
            default:
                badge.setText("Page Web");
                badge.getStyle().set("background", "#dbeafe").set("color", "#2563eb");
        }

        return badge;
    }

    private Component createSearchEmptyState() {
        VerticalLayout empty = new VerticalLayout();
        empty.setAlignItems(FlexComponent.Alignment.CENTER);
        empty.setPadding(true);
        empty.getStyle().set("padding", "3rem");

        Icon icon = VaadinIcon.SEARCH.create();
        icon.setSize("64px");
        icon.getStyle().set("color", "#cbd5e1");

        H3 title = new H3("Aucun résultat trouvé");
        title.getStyle().set("color", "#64748b").set("margin", "1rem 0 0.5rem 0");

        Paragraph hint = new Paragraph("Essayez de modifier vos critères de recherche");
        hint.getStyle().set("color", "#94a3b8");

        empty.add(icon, title, hint);
        return empty;
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
        familleContentContainer = new VerticalLayout();
        familleContentContainer.setPadding(true);
        familleContentContainer.setSpacing(true);
        familleContentContainer.getStyle().set("gap", "1rem");

        buildFamilleContent();
        return familleContentContainer;
    }

    private void buildFamilleContent() {
        familleContentContainer.removeAll();

        // Family summary card - full width
        Component summaryCard = createFamilySummaryCard();
        familleContentContainer.add(summaryCard);

        // Horizontal layout for side-by-side accordions
        HorizontalLayout accordionsRow = new HorizontalLayout();
        accordionsRow.setWidthFull();
        accordionsRow.setSpacing(true);
        accordionsRow.getStyle().set("gap", "1rem");

        // Left: Conjoints accordion (green - app primary color)
        Accordion conjointsAccordion = new Accordion();
        conjointsAccordion.setWidthFull();
        int conjointsCount = conjointsDataProvider.getItems().size();
        AccordionPanel conjointsPanel = createFamilyAccordionPanel(
            "Conjoint(s)",
            VaadinIcon.USER_HEART,
            "#3b6b35",
            conjointsCount,
            () -> openConjointEditDialog(null),
            createConjointCards()
        );
        conjointsAccordion.add(conjointsPanel);
        conjointsAccordion.open(conjointsPanel);

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setPadding(false);
        leftColumn.setSpacing(false);
        leftColumn.setWidth("50%");
        leftColumn.add(conjointsAccordion);

        // Right: Enfants accordion (blue - app secondary color)
        Accordion enfantsAccordion = new Accordion();
        enfantsAccordion.setWidthFull();
        int enfantsCount = enfantsDataProvider.getItems().size();
        AccordionPanel enfantsPanel = createFamilyAccordionPanel(
            "Enfant(s)",
            VaadinIcon.CHILD,
            "#2c5aa0",
            enfantsCount,
            () -> openEnfantEditDialog(null),
            createEnfantCards()
        );
        enfantsAccordion.add(enfantsPanel);
        enfantsAccordion.open(enfantsPanel);

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setPadding(false);
        rightColumn.setSpacing(false);
        rightColumn.setWidth("50%");
        rightColumn.add(enfantsAccordion);

        accordionsRow.add(leftColumn, rightColumn);
        familleContentContainer.add(accordionsRow);
    }

    private AccordionPanel createFamilyAccordionPanel(String title, VaadinIcon iconType, String color,
                                                       int count, Runnable onAddClick, Component cardsContent) {
        // Create custom summary with icon, title, count badge, and add button
        HorizontalLayout summary = new HorizontalLayout();
        summary.setAlignItems(FlexComponent.Alignment.CENTER);
        summary.setWidthFull();
        summary.setSpacing(true);
        summary.getStyle().set("gap", "0.75rem");

        // Icon with colored background
        Div iconBox = new Div();
        iconBox.getStyle()
            .set("width", "36px")
            .set("height", "36px")
            .set("border-radius", "10px")
            .set("background", color + "15")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("flex-shrink", "0");

        Icon icon = iconType.create();
        icon.setSize("18px");
        icon.getStyle().set("color", color);
        iconBox.add(icon);

        // Title
        Span titleSpan = new Span(title);
        titleSpan.getStyle()
            .set("font-weight", "600")
            .set("font-size", "1rem")
            .set("color", "#1e293b");

        // Count badge
        Span countBadge = new Span(String.valueOf(count));
        countBadge.getStyle()
            .set("background", color)
            .set("color", "white")
            .set("padding", "0.2rem 0.6rem")
            .set("border-radius", "12px")
            .set("font-size", "0.75rem")
            .set("font-weight", "600")
            .set("min-width", "24px")
            .set("text-align", "center");

        // Spacer
        Div spacer = new Div();
        spacer.getStyle().set("flex", "1");

        // Add button
        Button addBtn = new Button("Ajouter", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
        addBtn.getStyle()
            .set("background", color)
            .set("border-radius", "8px");
        addBtn.addClickListener(e -> onAddClick.run());

        summary.add(iconBox, titleSpan, countBadge, spacer, addBtn);

        // Create panel with content
        VerticalLayout panelContent = new VerticalLayout();
        panelContent.setPadding(true);
        panelContent.setSpacing(false);
        panelContent.getStyle()
            .set("background", "#f8fafc")
            .set("border-radius", "0 0 12px 12px")
            .set("padding", "1rem");
        panelContent.add(cardsContent);

        AccordionPanel panel = new AccordionPanel(summary, panelContent);
        panel.addThemeVariants(DetailsVariant.FILLED);
        panel.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.06)")
            .set("margin-bottom", "0.5rem")
            .set("--vaadin-accordion-panel-border-color", "transparent");

        return panel;
    }

    private Component createFamilySummaryCard() {
        Div card = new Div();
        card.getStyle()
            .set("width", "100%")
            .set("box-sizing", "border-box")
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 100%)")
            .set("border-radius", "16px")
            .set("padding", "1.5rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "space-between")
            .set("box-shadow", "0 4px 15px rgba(59, 107, 53, 0.3)");

        // Left: Title and description
        Div leftSection = new Div();
        leftSection.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("gap", "0.5rem");

        Span title = new Span("Ma Famille");
        title.getStyle()
            .set("color", "white")
            .set("font-size", "1.5rem")
            .set("font-weight", "700");

        Span subtitle = new Span("Gérez les informations de votre famille");
        subtitle.getStyle()
            .set("color", "rgba(255,255,255,0.8)")
            .set("font-size", "0.9rem");

        leftSection.add(title, subtitle);

        // Right: Family stats
        Div rightSection = new Div();
        rightSection.getStyle()
            .set("display", "flex")
            .set("gap", "1.5rem");

        int conjointsCount = conjointsDataProvider.getItems().size();
        int enfantsCount = enfantsDataProvider.getItems().size();

        rightSection.add(createFamilyStatBubble(String.valueOf(conjointsCount), "Conjoint(s)", VaadinIcon.USER_HEART));
        rightSection.add(createFamilyStatBubble(String.valueOf(enfantsCount), "Enfant(s)", VaadinIcon.CHILD));

        card.add(leftSection, rightSection);
        return card;
    }

    private Component createFamilyStatBubble(String value, String label, VaadinIcon iconType) {
        Div bubble = new Div();
        bubble.getStyle()
            .set("background", "rgba(255,255,255,0.2)")
            .set("border-radius", "12px")
            .set("padding", "1rem 1.25rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.75rem")
            .set("backdrop-filter", "blur(10px)");

        Div iconBox = new Div();
        iconBox.getStyle()
            .set("width", "40px")
            .set("height", "40px")
            .set("border-radius", "10px")
            .set("background", "rgba(255,255,255,0.25)")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

        Icon icon = iconType.create();
        icon.setSize("20px");
        icon.getStyle().set("color", "white");
        iconBox.add(icon);

        Div textBox = new Div();
        textBox.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
            .set("color", "white")
            .set("font-size", "1.5rem")
            .set("font-weight", "700")
            .set("line-height", "1");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("color", "rgba(255,255,255,0.8)")
            .set("font-size", "0.75rem");

        textBox.add(valueSpan, labelSpan);
        bubble.add(iconBox, textBox);
        return bubble;
    }

    private Component createConjointCards() {
        Div container = new Div();
        container.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fill, minmax(320px, 1fr))")
            .set("gap", "1rem");

        List<AdhConjoint> conjoints = new ArrayList<>(conjointsDataProvider.getItems());

        if (conjoints.isEmpty()) {
            container.add(createEmptyFamilyState("Aucun conjoint enregistré", "Ajoutez votre conjoint pour compléter votre dossier famille.", VaadinIcon.USER_HEART));
        } else {
            for (AdhConjoint c : conjoints) {
                container.add(createConjointCard(c));
            }
        }

        return container;
    }

    private Component createConjointCard(AdhConjoint conjoint) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#fafafa")
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "12px")
            .set("padding", "1rem")
            .set("display", "flex")
            .set("gap", "1rem")
            .set("transition", "all 0.2s ease")
            .set("cursor", "pointer");

        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("border-color", "#3b6b35").set("box-shadow", "0 4px 12px rgba(59, 107, 53, 0.15)");
        });
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("border-color", "#e2e8f0").set("box-shadow", "none");
        });

        // Avatar
        Component avatar = createFamilyMemberAvatar(
            conjoint.getConjoint_photo(),
            conjoint.getConjoint_photo_filename(),
            conjoint.getConjoint_photo_contentType(),
            getInitials(conjoint.getNOM_CONJ(), conjoint.getPR_CONJ()),
            "#3b6b35"
        );

        // Info section
        Div infoSection = new Div();
        infoSection.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("flex", "1")
            .set("gap", "0.5rem")
            .set("min-width", "0");

        // Name row with status
        HorizontalLayout nameRow = new HorizontalLayout();
        nameRow.setAlignItems(FlexComponent.Alignment.CENTER);
        nameRow.setSpacing(false);
        nameRow.getStyle().set("gap", "0.5rem");

        Span name = new Span((conjoint.getNOM_CONJ() != null ? conjoint.getNOM_CONJ() : "") + " " +
                             (conjoint.getPR_CONJ() != null ? conjoint.getPR_CONJ() : ""));
        name.getStyle()
            .set("font-weight", "600")
            .set("color", "#1e293b")
            .set("font-size", "1rem")
            .set("white-space", "nowrap")
            .set("overflow", "hidden")
            .set("text-overflow", "ellipsis");

        Component status = createMiniStatusBadge(conjoint.isValide());
        nameRow.add(name, status);

        // Arabic name (if exists)
        Div arabicName = new Div();
        if (conjoint.getNom_CONJ_A() != null || conjoint.getPr_CONJ_A() != null) {
            arabicName.setText((conjoint.getNom_CONJ_A() != null ? conjoint.getNom_CONJ_A() : "") + " " +
                              (conjoint.getPr_CONJ_A() != null ? conjoint.getPr_CONJ_A() : ""));
            arabicName.getStyle()
                .set("color", "#64748b")
                .set("font-size", "0.85rem")
                .set("direction", "rtl")
                .set("text-align", "left");
        }

        // Details grid
        Div detailsGrid = new Div();
        detailsGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "1fr 1fr")
            .set("gap", "0.4rem");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        detailsGrid.add(createDetailItem(VaadinIcon.CREDIT_CARD, conjoint.getCIN_CONJ()));
        detailsGrid.add(createDetailItem(VaadinIcon.CALENDAR, conjoint.getDat_N_CONJ() != null ? sdf.format(conjoint.getDat_N_CONJ()) : "-"));
        detailsGrid.add(createDetailItem(VaadinIcon.PHONE, conjoint.getTele()));
        detailsGrid.add(createDetailItem(VaadinIcon.USER, formatSexe(conjoint.getSex_CONJ())));

        infoSection.add(nameRow, arabicName, detailsGrid);

        // Edit button
        Button editBtn = new Button(VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        editBtn.getStyle()
            .set("color", "#3b6b35")
            .set("align-self", "flex-start");
        editBtn.addClickListener(e -> openConjointEditDialog(conjoint));

        card.add(avatar, infoSection, editBtn);
        return card;
    }

    private Component createEnfantCards() {
        Div container = new Div();
        container.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(auto-fill, minmax(320px, 1fr))")
            .set("gap", "1rem");

        List<AdhEnfant> enfants = new ArrayList<>(enfantsDataProvider.getItems());

        if (enfants.isEmpty()) {
            container.add(createEmptyFamilyState("Aucun enfant enregistré", "Ajoutez vos enfants pour compléter votre dossier famille.", VaadinIcon.CHILD));
        } else {
            for (AdhEnfant e : enfants) {
                container.add(createEnfantCard(e));
            }
        }

        return container;
    }

    private Component createEnfantCard(AdhEnfant enfant) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#fafafa")
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "12px")
            .set("padding", "1rem")
            .set("display", "flex")
            .set("gap", "1rem")
            .set("transition", "all 0.2s ease")
            .set("cursor", "pointer");

        card.getElement().addEventListener("mouseenter", e -> {
            card.getStyle().set("border-color", "#2c5aa0").set("box-shadow", "0 4px 12px rgba(44, 90, 160, 0.15)");
        });
        card.getElement().addEventListener("mouseleave", e -> {
            card.getStyle().set("border-color", "#e2e8f0").set("box-shadow", "none");
        });

        // Avatar
        Component avatar = createFamilyMemberAvatar(
            enfant.getEnfant_photo(),
            enfant.getEnfant_photo_filename(),
            enfant.getEnfant_photo_contentType(),
            getInitials(enfant.getNom_pac(), enfant.getPr_pac()),
            "#2c5aa0"
        );

        // Info section
        Div infoSection = new Div();
        infoSection.getStyle()
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("flex", "1")
            .set("gap", "0.5rem")
            .set("min-width", "0");

        // Name row with status
        HorizontalLayout nameRow = new HorizontalLayout();
        nameRow.setAlignItems(FlexComponent.Alignment.CENTER);
        nameRow.setSpacing(false);
        nameRow.getStyle().set("gap", "0.5rem");

        Span name = new Span((enfant.getNom_pac() != null ? enfant.getNom_pac() : "") + " " +
                             (enfant.getPr_pac() != null ? enfant.getPr_pac() : ""));
        name.getStyle()
            .set("font-weight", "600")
            .set("color", "#1e293b")
            .set("font-size", "1rem")
            .set("white-space", "nowrap")
            .set("overflow", "hidden")
            .set("text-overflow", "ellipsis");

        Component status = createMiniStatusBadge(enfant.isValide());
        nameRow.add(name, status);

        // Arabic name (if exists)
        Div arabicName = new Div();
        if (enfant.getNom_PAC_A() != null || enfant.getPr_PAC_A() != null) {
            arabicName.setText((enfant.getNom_PAC_A() != null ? enfant.getNom_PAC_A() : "") + " " +
                              (enfant.getPr_PAC_A() != null ? enfant.getPr_PAC_A() : ""));
            arabicName.getStyle()
                .set("color", "#64748b")
                .set("font-size", "0.85rem")
                .set("direction", "rtl")
                .set("text-align", "left");
        }

        // Details grid
        Div detailsGrid = new Div();
        detailsGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "1fr 1fr")
            .set("gap", "0.4rem");

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        detailsGrid.add(createDetailItem(VaadinIcon.CALENDAR, enfant.getDat_n_pac() != null ? sdf.format(enfant.getDat_n_pac()) : "-"));
        detailsGrid.add(createDetailItem(VaadinIcon.USER, formatSexe(enfant.getSex_pac())));
        detailsGrid.add(createDetailItem(VaadinIcon.ACADEMY_CAP, enfant.getNiv_INSTRUCTION() != null ? enfant.getNiv_INSTRUCTION() : "-"));
        detailsGrid.add(createDetailItem(VaadinIcon.FAMILY, enfant.getLien_PAR() != null ? enfant.getLien_PAR() : "Enfant"));

        infoSection.add(nameRow, arabicName, detailsGrid);

        // Edit button
        Button editBtn = new Button(VaadinIcon.EDIT.create());
        editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        editBtn.getStyle()
            .set("color", "#2c5aa0")
            .set("align-self", "flex-start");
        editBtn.addClickListener(ev -> openEnfantEditDialog(enfant));

        card.add(avatar, infoSection, editBtn);
        return card;
    }

    private Component createFamilyMemberAvatar(byte[] photoData, String filename, String contentType,
                                                String initials, String color) {
        Div avatar = new Div();
        avatar.getStyle()
            .set("width", "60px")
            .set("height", "60px")
            .set("border-radius", "12px")
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
            avatar.getStyle().set("border", "2px solid " + color + "30");
        } else {
            avatar.getStyle()
                .set("background", color + "15")
                .set("color", color)
                .set("font-weight", "700")
                .set("font-size", "1.25rem")
                .set("border", "2px solid " + color + "30");
            avatar.setText(initials);
        }

        return avatar;
    }

    private Component createDetailItem(VaadinIcon iconType, String value) {
        HorizontalLayout item = new HorizontalLayout();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setSpacing(false);
        item.getStyle().set("gap", "0.35rem");

        Icon icon = iconType.create();
        icon.setSize("12px");
        icon.getStyle().set("color", "#94a3b8");

        Span text = new Span(value != null && !value.isEmpty() ? value : "-");
        text.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.8rem")
            .set("white-space", "nowrap")
            .set("overflow", "hidden")
            .set("text-overflow", "ellipsis");

        item.add(icon, text);
        return item;
    }

    private Component createMiniStatusBadge(boolean valid) {
        Span badge = new Span(valid ? "Validé" : "En attente");
        badge.getStyle()
            .set("padding", "2px 8px")
            .set("border-radius", "9999px")
            .set("font-size", "0.65rem")
            .set("font-weight", "500")
            .set("background", valid ? "#dcfce7" : "#fef3c7")
            .set("color", valid ? "#166534" : "#92400e");
        return badge;
    }

    private Component createEmptyFamilyState(String title, String message, VaadinIcon iconType) {
        Div container = new Div();
        container.getStyle()
            .set("text-align", "center")
            .set("padding", "2rem")
            .set("background", "#f8fafc")
            .set("border-radius", "12px")
            .set("border", "2px dashed #e2e8f0");

        Icon icon = iconType.create();
        icon.setSize("48px");
        icon.getStyle().set("color", "#cbd5e1");

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
            .set("display", "block")
            .set("color", "#64748b")
            .set("font-weight", "500")
            .set("margin-top", "1rem");

        Span messageSpan = new Span(message);
        messageSpan.getStyle()
            .set("display", "block")
            .set("color", "#94a3b8")
            .set("font-size", "0.85rem")
            .set("margin-top", "0.5rem");

        container.add(icon, titleSpan, messageSpan);
        return container;
    }

    private void openConjointEditDialog(AdhConjoint conjoint) {
        AdherentConjointEditDialog dialog = new AdherentConjointEditDialog(conjoint, currentAgent, conjointService, modificationService);
        dialog.addSaveListener(c -> {
            refreshConjointsData();
            refreshFamilleTab();
            updateStats();
        });
        dialog.open();
    }

    private void openEnfantEditDialog(AdhEnfant enfant) {
        AdherentEnfantEditDialog dialog = new AdherentEnfantEditDialog(enfant, currentAgent, enfantService, modificationService);
        dialog.addSaveListener(e -> {
            refreshEnfantsData();
            refreshFamilleTab();
            updateStats();
        });
        dialog.open();
    }

    private void refreshFamilleTab() {
        // Rebuild the famille content if currently viewing famille section
        if ("famille".equals(currentSection) && familleContentContainer != null) {
            buildFamilleContent();
        }
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
            showSection("demandes");
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
