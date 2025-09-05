package com.fosagri.application.views.welcome;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Bienvenue")
@Route("welcome")
@Menu(order = 0, icon = LineAwesomeIconUrl.HOME_SOLID)
public class WelcomeView extends VerticalLayout {

    public WelcomeView() {
        addClassName("welcome-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.START);

        H1 title = new H1("Bienvenue dans FOS AGRI Data");
        Paragraph intro = new Paragraph(
            "Cette application vous aide à gérer les agents, leurs familles, les fichiers associés " +
            "et à suivre les opérations. Utilisez le menu pour naviguer entre les sections."
        );

        HorizontalLayout links = new HorizontalLayout();
        links.setSpacing(true);

        Anchor dashboardLink = new Anchor("dashboard", "Aller au tableau de bord");
        dashboardLink.getElement().setAttribute("router-link", true);

        Anchor agentsLink = new Anchor("agents", "Gérer les agents");
        agentsLink.getElement().setAttribute("router-link", true);

        Anchor usersLink = new Anchor("users", "Gérer les utilisateurs");
        usersLink.getElement().setAttribute("router-link", true);

        links.add(dashboardLink, agentsLink, usersLink);

        add(title, intro, links);
    }
}
