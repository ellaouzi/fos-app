package com.fosagri.application.views.welcome;

import com.fosagri.application.security.AuthenticatedUser;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Bienvenue sur votre espace adhérent")
@Route(value = "", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.HOME_SOLID)
@PermitAll
public class WelcomeView extends VerticalLayout {

    public WelcomeView(AuthenticatedUser authenticatedUser) {
        addClassName("welcome-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "linear-gradient(180deg, #f8fafc 0%, #e2e8f0 100%)");

        // Header Section
        add(createHeaderSection(authenticatedUser));

        // Stats Section
        add(createStatsSection());

        // Services Section
        add(createServicesSection());

        // Quick Actions Section
        add(createQuickActionsSection());

        // Footer Info
        add(createFooterSection());
    }

    private VerticalLayout createHeaderSection(AuthenticatedUser authenticatedUser) {
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setPadding(true);
        header.setSpacing(false);
        header.getStyle()
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 50%, #d4a949 100%)")
            .set("border-radius", "16px")
            .set("color", "white")
            .set("padding", "2.5rem")
            .set("margin-bottom", "1.5rem")
            .set("box-shadow", "0 10px 40px rgba(59, 107, 53, 0.3)");

        // Welcome message with user name
        String userName = authenticatedUser.get()
            .map(u -> {
                if (u.getFullname() != null && !u.getFullname().isEmpty()) {
                    return u.getFullname();
                } else if (u.getPrenom() != null && !u.getPrenom().isEmpty()) {
                    return u.getPrenom();
                } else {
                    return u.getUsername();
                }
            })
            .orElse("Adhérent");

        H1 title = new H1("Bienvenue sur votre espace adhérent");
        title.getStyle()
            .set("color", "white")
            .set("margin", "0")
            .set("font-size", "2rem")
            .set("font-weight", "700")
            .set("text-align", "center");

        Paragraph greeting = new Paragraph("Bonjour " + userName + " !");
        greeting.getStyle()
            .set("color", "rgba(255,255,255,0.9)")
            .set("font-size", "1.25rem")
            .set("margin", "0.5rem 0 1rem 0");

        Paragraph subtitle = new Paragraph(
            "Fondation pour la Promotion des Œuvres Sociales du Personnel du Ministère de l'Agriculture"
        );
        subtitle.getStyle()
            .set("color", "rgba(255,255,255,0.85)")
            .set("font-size", "1rem")
            .set("margin", "0")
            .set("text-align", "center")
            .set("max-width", "600px");

        header.add(title, greeting, subtitle);
        return header;
    }

    private HorizontalLayout createStatsSection() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        stats.setSpacing(true);
        stats.getStyle().set("flex-wrap", "wrap").set("gap", "1rem");

        stats.add(
            createStatCard(VaadinIcon.USERS, "Adhérents", "Gestion complète", "#3b6b35"),
            createStatCard(VaadinIcon.HEART, "Prestations", "Santé & Social", "#2c5aa0"),
            createStatCard(VaadinIcon.ACADEMY_CAP, "Éducation", "Bourses & Formation", "#8b5cf6"),
            createStatCard(VaadinIcon.HOME, "Logement", "Projets immobiliers", "#d4a949")
        );

        return stats;
    }

    private VerticalLayout createStatCard(VaadinIcon iconType, String title, String description, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setPadding(true);
        card.setSpacing(false);
        card.setWidth("200px");
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 4px 15px rgba(0,0,0,0.08)")
            .set("padding", "1.5rem")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("cursor", "pointer");

        Icon icon = iconType.create();
        icon.setSize("32px");
        icon.getStyle().set("color", color);

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
            .set("font-weight", "600")
            .set("font-size", "1.1rem")
            .set("color", "#1e293b")
            .set("margin-top", "0.75rem");

        Span descSpan = new Span(description);
        descSpan.getStyle()
            .set("font-size", "0.85rem")
            .set("color", "#64748b")
            .set("margin-top", "0.25rem");

        card.add(icon, titleSpan, descSpan);
        return card;
    }

    private VerticalLayout createServicesSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("box-shadow", "0 4px 15px rgba(0,0,0,0.05)")
            .set("padding", "2rem")
            .set("margin-top", "1rem");

        H2 sectionTitle = new H2("Nos Services");
        sectionTitle.getStyle()
            .set("color", "#1e293b")
            .set("margin", "0 0 1.5rem 0")
            .set("font-size", "1.5rem");

        HorizontalLayout services = new HorizontalLayout();
        services.setWidthFull();
        services.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        services.getStyle().set("flex-wrap", "wrap").set("gap", "1rem");

        services.add(
            createServiceCard("Prévoyance Médico-Sociale",
                "Soutien face aux aléas de santé avec nos conventions médicales",
                VaadinIcon.STETHOSCOPE, "#ef4444"),
            createServiceCard("Culture, Loisirs & Voyages",
                "Estivage, colonies de vacances, Omra et voyages organisés",
                VaadinIcon.AIRPLANE, "#3b82f6"),
            createServiceCard("Formation & Scolarisation",
                "Bourses d'excellence et primes de rentrée scolaire",
                VaadinIcon.BOOK, "#8b5cf6"),
            createServiceCard("Club Agriculture",
                "Piscine couverte, salle fitness et espaces détente",
                VaadinIcon.GROUP, "#10b981")
        );

        section.add(sectionTitle, services);
        return section;
    }

    private VerticalLayout createServiceCard(String title, String description, VaadinIcon iconType, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setSpacing(false);
        card.setWidth("280px");
        card.getStyle()
            .set("background", "linear-gradient(135deg, " + color + "10, " + color + "05)")
            .set("border", "1px solid " + color + "20")
            .set("border-radius", "12px")
            .set("padding", "1.25rem")
            .set("transition", "transform 0.2s");

        HorizontalLayout header = new HorizontalLayout();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setSpacing(true);

        Icon icon = iconType.create();
        icon.setSize("24px");
        icon.getStyle().set("color", color);

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
            .set("font-weight", "600")
            .set("font-size", "1rem")
            .set("color", "#1e293b");

        header.add(icon, titleSpan);

        Paragraph desc = new Paragraph(description);
        desc.getStyle()
            .set("font-size", "0.875rem")
            .set("color", "#64748b")
            .set("margin", "0.75rem 0 0 0")
            .set("line-height", "1.5");

        card.add(header, desc);
        return card;
    }

    private VerticalLayout createQuickActionsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setWidthFull();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("box-shadow", "0 4px 15px rgba(0,0,0,0.05)")
            .set("padding", "2rem")
            .set("margin-top", "1rem");

        H2 sectionTitle = new H2("Accès Rapide");
        sectionTitle.getStyle()
            .set("color", "#1e293b")
            .set("margin", "0 0 1.5rem 0")
            .set("font-size", "1.5rem");

        HorizontalLayout links = new HorizontalLayout();
        links.setWidthFull();
        links.setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        links.getStyle().set("flex-wrap", "wrap").set("gap", "1rem");

        links.add(
            createQuickLink("Tableau de bord", "dashboard", VaadinIcon.DASHBOARD, "#3b6b35"),
            createQuickLink("Mes demandes", "demandes", VaadinIcon.FILE_TEXT_O, "#2c5aa0"),
            createQuickLink("Recherche", "search", VaadinIcon.SEARCH, "#8b5cf6"),
            createQuickLink("Réclamations", "reclamations", VaadinIcon.COMMENT, "#f59e0b"),
            createQuickLink("Mes fichiers", "fichiers", VaadinIcon.FOLDER_OPEN, "#10b981")
        );

        section.add(sectionTitle, links);
        return section;
    }

    private Anchor createQuickLink(String text, String route, VaadinIcon iconType, String color) {
        HorizontalLayout content = new HorizontalLayout();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(true);
        content.setPadding(true);
        content.getStyle()
            .set("background", color)
            .set("border-radius", "10px")
            .set("padding", "0.875rem 1.5rem")
            .set("color", "white")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("box-shadow", "0 4px 15px " + color + "40");

        Icon icon = iconType.create();
        icon.setSize("20px");
        icon.getStyle().set("color", "white");

        Span label = new Span(text);
        label.getStyle()
            .set("font-weight", "500")
            .set("font-size", "0.95rem");

        content.add(icon, label);

        Anchor link = new Anchor(route, content);
        link.getElement().setAttribute("router-link", true);
        link.getStyle().set("text-decoration", "none");

        return link;
    }

    private VerticalLayout createFooterSection() {
        VerticalLayout footer = new VerticalLayout();
        footer.setWidthFull();
        footer.setAlignItems(FlexComponent.Alignment.CENTER);
        footer.setPadding(true);
        footer.getStyle()
            .set("background", "#f1f5f9")
            .set("border-radius", "12px")
            .set("padding", "1.5rem")
            .set("margin-top", "1.5rem");

        H3 contactTitle = new H3("Contact FOS-Agri");
        contactTitle.getStyle()
            .set("color", "#1e293b")
            .set("margin", "0 0 1rem 0")
            .set("font-size", "1.1rem");

        HorizontalLayout contactInfo = new HorizontalLayout();
        contactInfo.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        contactInfo.getStyle().set("flex-wrap", "wrap").set("gap", "2rem");

        contactInfo.add(
            createContactItem(VaadinIcon.PHONE, "05 37 66 55 40"),
            createContactItem(VaadinIcon.ENVELOPE, "fos-agri@fos-agri.ma"),
            createContactItem(VaadinIcon.GLOBE, "www.fos-agri.ma")
        );

        footer.add(contactTitle, contactInfo);
        return footer;
    }

    private HorizontalLayout createContactItem(VaadinIcon iconType, String text) {
        HorizontalLayout item = new HorizontalLayout();
        item.setAlignItems(FlexComponent.Alignment.CENTER);
        item.setSpacing(true);

        Icon icon = iconType.create();
        icon.setSize("18px");
        icon.getStyle().set("color", "#3b6b35");

        Span textSpan = new Span(text);
        textSpan.getStyle()
            .set("color", "#475569")
            .set("font-size", "0.9rem");

        item.add(icon, textSpan);
        return item;
    }
}
