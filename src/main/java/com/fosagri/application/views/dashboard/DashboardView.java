package com.fosagri.application.views.dashboard;

import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.service.UtilisateurService;
import com.fosagri.application.services.DemandePrestationService;
import com.fosagri.application.services.PrestationRefService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Tableau de bord")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.CHART_LINE_SOLID)
public class DashboardView extends VerticalLayout {

    @Autowired
    private AdhAgentService agentService;
    
    @Autowired
    private UtilisateurService utilisateurService;
    
    @Autowired
    private DemandePrestationService demandeService;
    
    @Autowired
    private PrestationRefService prestationService;

    public DashboardView(AdhAgentService agentService, UtilisateurService utilisateurService,
                        DemandePrestationService demandeService, PrestationRefService prestationService) {
        this.agentService = agentService;
        this.utilisateurService = utilisateurService;
        this.demandeService = demandeService;
        this.prestationService = prestationService;
        
        addClassName("dashboard-view");
        setSizeFull();
        setPadding(true);
        
        createHeader();
        createStatistics();
        createWelcomeSection();
    }

    private void createHeader() {
        H1 title = new H1("Tableau de bord - FOS AGRI");
        title.addClassName("dashboard-title");
        add(title);
    }

    private void createStatistics() {
        long agentCount = agentService.count();
        long userCount = utilisateurService.count();
        long prestationCount = prestationService.count();
        long demandeCount = demandeService.count();

        Div agentCard = createStatCard("Agents", String.valueOf(agentCount), VaadinIcon.USERS);
        Div userCard = createStatCard("Utilisateurs", String.valueOf(userCount), VaadinIcon.USER);
        Div prestationCard = createStatCard("Prestations", String.valueOf(prestationCount), VaadinIcon.CLIPBOARD);
        Div demandeCard = createStatCard("Demandes", String.valueOf(demandeCount), VaadinIcon.FILE_TEXT);

        HorizontalLayout statsLayout = new HorizontalLayout(agentCard, userCard, prestationCard, demandeCard);
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);
        
        add(statsLayout);
    }

    private Div createStatCard(String title, String value, VaadinIcon icon) {
        Div card = new Div();
        card.addClassName("stat-card");
        card.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-l)")
            .set("text-align", "center")
            .set("min-height", "120px")
            .set("display", "flex")
            .set("flex-direction", "column")
            .set("justify-content", "center");

        icon.create().getStyle()
            .set("font-size", "2em")
            .set("color", "var(--lumo-primary-color)")
            .set("margin-bottom", "var(--lumo-space-s)");

        H3 titleElement = new H3(title);
        titleElement.getStyle().set("margin", "0").set("color", "var(--lumo-secondary-text-color)");

        Div valueElement = new Div(value);
        valueElement.getStyle()
            .set("font-size", "2em")
            .set("font-weight", "bold")
            .set("color", "var(--lumo-primary-text-color)");

        card.add(icon.create(), titleElement, valueElement);
        return card;
    }

    private void createWelcomeSection() {
        Div welcomeCard = new Div();
        welcomeCard.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-l)")
            .set("margin-top", "var(--lumo-space-l)");

        H3 welcomeTitle = new H3("Bienvenue dans FOS AGRI Data Management");
        
        Paragraph welcomeText = new Paragraph(
            "Cette application vous permet de gérer les données des agents agricoles, " +
            "leurs familles, les documents associés, les prestations et les demandes. " +
            "Utilisez le menu de navigation pour accéder aux différentes fonctionnalités."
        );

        welcomeCard.add(welcomeTitle, welcomeText);
        add(welcomeCard);
    }
}