package com.fosagri.application.views;

import com.fosagri.application.model.Utilisateur;
import com.fosagri.application.security.AuthenticatedUser;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.SvgIcon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.PermitAll;

import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * Modern main layout with grouped navigation.
 */
@Layout
@PermitAll
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private H1 viewTitle;
    private final AuthenticatedUser authenticatedUser;
    private boolean isAdmin = false;
    private boolean isAgent = false;

    public MainLayout(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        // Check if user is admin or agent
        Optional<Utilisateur> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            Utilisateur user = maybeUser.get();
            isAdmin = user.getAuthorities() != null &&
                user.getAuthorities().stream()
                    .anyMatch(a -> "ADMIN".equals(a.getRole()));
            // Check if user is linked to an agent profile
            isAgent = authenticatedUser.getLinkedAgent().isPresent();
        }

        // Only build layout if user is authenticated
        if (maybeUser.isPresent()) {
            setPrimarySection(Section.DRAWER);
            addDrawerContent();
            addHeaderContent();

            // Apply modern styling
            getElement().getStyle()
                .set("--vaadin-app-layout-drawer-width", "280px");
        }
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String path = event.getLocation().getPath();
        if ("login".equals(path)) {
            // Hide drawer for login
            setDrawerOpened(false);
            getElement().getStyle().set("--vaadin-app-layout-drawer-offset-size", "0px");
        } else {
            // Ensure drawer is open for all other pages
            setDrawerOpened(true);
            getElement().getStyle().remove("--vaadin-app-layout-drawer-offset-size");
        }
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        viewTitle.getStyle().set("font-weight", "600");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle()
            .set("padding", "0 1.5rem")
            .set("height", "64px");

        // Apply gradient style for both agents and admins
        header.getStyle()
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 100%)")
            .set("box-shadow", "0 4px 15px rgba(59, 107, 53, 0.3)");
        toggle.getStyle().set("color", "white");
        viewTitle.getStyle().set("color", "white");

        HorizontalLayout leftSection = new HorizontalLayout(toggle, viewTitle);
        leftSection.setAlignItems(FlexComponent.Alignment.CENTER);
        leftSection.setSpacing(true);

        header.add(leftSection, createUserMenu());

        addToNavbar(true, header);
    }

    private HorizontalLayout createUserMenu() {
        HorizontalLayout userMenu = new HorizontalLayout();
        userMenu.setAlignItems(FlexComponent.Alignment.CENTER);
        userMenu.setSpacing(true);
        userMenu.getStyle().set("gap", "0.75rem");

        Optional<Utilisateur> maybeUser = authenticatedUser.get();

        if (maybeUser.isPresent()) {
            Utilisateur user = maybeUser.get();

            String displayName = user.getPrenom() != null && user.getNom() != null
                ? user.getPrenom() + " " + user.getNom()
                : user.getUsername();

            // For agents, get agent info
            var linkedAgent = authenticatedUser.getLinkedAgent();

            // Avatar
            Avatar avatar = new Avatar(displayName);
            avatar.getStyle().set("--vaadin-avatar-size", "40px");

            // Style avatar with white border for gradient header
            avatar.getStyle()
                .set("border", "2px solid rgba(255,255,255,0.5)")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.2)");

            if (isAgent && !isAdmin && linkedAgent.isPresent()) {
                var agent = linkedAgent.get();
                String agentName = (agent.getPR_AG() != null ? agent.getPR_AG() : "") + " " +
                                   (agent.getNOM_AG() != null ? agent.getNOM_AG() : "");
                avatar.setName(agentName.trim());

                // Try to set agent photo if available (stored as byte array)
                if (agent.getAgent_photo() != null && agent.getAgent_photo().length > 0) {
                    String contentType = agent.getAgent_photo_contentType() != null
                        ? agent.getAgent_photo_contentType() : "image/jpeg";
                    String base64 = java.util.Base64.getEncoder().encodeToString(agent.getAgent_photo());
                    avatar.setImage("data:" + contentType + ";base64," + base64);
                }
            } else if (isAdmin) {
                avatar.setColorIndex(1);
            }

            // User info section
            VerticalLayout userInfo = new VerticalLayout();
            userInfo.setPadding(false);
            userInfo.setSpacing(false);

            if (isAgent && !isAdmin && linkedAgent.isPresent()) {
                var agent = linkedAgent.get();
                String agentName = (agent.getPR_AG() != null ? agent.getPR_AG() : "") + " " +
                                   (agent.getNOM_AG() != null ? agent.getNOM_AG() : "");

                Span nameSpan = new Span(agentName.trim());
                nameSpan.getStyle()
                    .set("font-weight", "600")
                    .set("color", "white")
                    .set("font-size", "0.95rem");

                String idText = "ID: " + (agent.getIdAdh() != null ? agent.getIdAdh() : agent.getCIN_AG());
                Span idSpan = new Span(idText);
                idSpan.getStyle()
                    .set("color", "rgba(255,255,255,0.8)")
                    .set("font-size", "0.75rem");

                userInfo.add(nameSpan, idSpan);
            } else {
                // Admin or regular user - white text for gradient header
                Span nameSpan = new Span(displayName);
                nameSpan.getStyle()
                    .set("font-weight", "600")
                    .set("color", "white")
                    .set("font-size", "0.95rem");

                if (isAdmin) {
                    Span roleSpan = new Span("Administrateur");
                    roleSpan.getStyle()
                        .set("color", "rgba(255,255,255,0.8)")
                        .set("font-size", "0.75rem");
                    userInfo.add(nameSpan, roleSpan);
                } else {
                    userInfo.add(nameSpan);
                }
            }

            // Logout button - white style for gradient header
            Button logoutBtn = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            logoutBtn.getStyle()
                .set("background", "rgba(255,255,255,0.2)")
                .set("color", "white")
                .set("border", "1px solid rgba(255,255,255,0.3)")
                .set("cursor", "pointer");
            logoutBtn.addClickListener(e -> authenticatedUser.logout());

            userMenu.add(avatar, userInfo, logoutBtn);
        }

        return userMenu;
    }

    private void addDrawerContent() {
        // Brand header with gradient - same height as main header (64px)
        Div brandHeader = new Div();
        brandHeader.getStyle()
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 100%)")
            .set("padding", "0 1rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.75rem")
            .set("height", "64px")
            .set("min-height", "64px")
            .set("box-sizing", "border-box");

        com.vaadin.flow.component.html.Image logo = new com.vaadin.flow.component.html.Image(
            "images/logo_FosAgri.png", "FOS-Agri Logo");
        logo.setWidth("40px");
        logo.setHeight("40px");
        logo.getStyle()
            .set("object-fit", "contain")
            .set("background", "white")
            .set("border-radius", "8px")
            .set("padding", "4px");

        Div brandText = new Div();
        Span appName = new Span("FOS-Agri");
        appName.getStyle()
            .set("color", "white")
            .set("font-size", "1.2rem")
            .set("font-weight", "700")
            .set("display", "block")
            .set("line-height", "1.2");

        Span appSubtitle = new Span("E-Services");
        appSubtitle.getStyle()
            .set("color", "rgba(255,255,255,0.8)")
            .set("font-size", "0.75rem")
            .set("display", "block")
            .set("line-height", "1.2");

        brandText.add(appName, appSubtitle);
        brandHeader.add(logo, brandText);

        // Navigation container
        VerticalLayout navContainer = new VerticalLayout();
        navContainer.setPadding(true);
        navContainer.setSpacing(false);
        navContainer.getStyle().set("padding", "1rem");

        // Show agent-specific menu for agents, or regular menu for admins
        if (isAgent && !isAdmin) {
            navContainer.add(createSectionLabel("ESPACE ADHÉRENT"));
            navContainer.add(createAgentNavigation());
        } else {
            // Main navigation section
            navContainer.add(createSectionLabel("MENU PRINCIPAL"));
            navContainer.add(createMainNavigation());

            // Admin section (only for admins)
            if (isAdmin) {
                navContainer.add(createSectionLabel("ADMINISTRATION"));
                navContainer.add(createAdminNavigation());
            }

            // Settings section
            navContainer.add(createSectionLabel("PARAMÈTRES"));
            navContainer.add(createSettingsNavigation());
        }

        Scroller scroller = new Scroller(navContainer);
        scroller.getStyle()
            .set("background", "#f8fafc")
            .set("flex", "1");

        addToDrawer(brandHeader, scroller, createFooter());
    }

    private Span createSectionLabel(String text) {
        Span label = new Span(text);
        label.getStyle()
            .set("color", "#94a3b8")
            .set("font-size", "0.7rem")
            .set("font-weight", "700")
            .set("letter-spacing", "0.05em")
            .set("margin-top", "1.25rem")
            .set("margin-bottom", "0.5rem")
            .set("display", "block");
        return label;
    }

    private VerticalLayout createAgentNavigation() {
        VerticalLayout nav = new VerticalLayout();
        nav.setPadding(false);
        nav.setSpacing(false);
        nav.getStyle().set("gap", "4px");

        // Agent menu items
        Map<String, String[]> agentMenuItems = new java.util.LinkedHashMap<>();
        agentMenuItems.put("dashboard", new String[]{"Tableau de bord", "DASHBOARD"});
        agentMenuItems.put("profil", new String[]{"Mon Profil", "USER"});
        agentMenuItems.put("famille", new String[]{"Famille", "FAMILY"});
        agentMenuItems.put("documents", new String[]{"Documents", "FILE_O"});
        agentMenuItems.put("prestations", new String[]{"Prestations", "GIFT"});
        agentMenuItems.put("demandes", new String[]{"Mes Demandes", "FILE_TEXT_O"});
        agentMenuItems.put("reclamations", new String[]{"Réclamations", "COMMENT_ELLIPSIS"});
        agentMenuItems.put("calendar", new String[]{"Calendrier", "CALENDAR"});
        agentMenuItems.put("contacts", new String[]{"Contacts", "PHONE"});
        agentMenuItems.put("search", new String[]{"Recherche", "SEARCH"});

        for (Map.Entry<String, String[]> entry : agentMenuItems.entrySet()) {
            String section = entry.getKey();
            String label = entry.getValue()[0];
            String iconName = entry.getValue()[1];

            Icon icon = VaadinIcon.valueOf(iconName).create();
            icon.setSize("18px");

            Button menuBtn = new Button(label, icon);
            menuBtn.setWidthFull();
            menuBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            menuBtn.addClassName("agent-menu-btn");
            menuBtn.getStyle()
                .set("justify-content", "flex-start")
                .set("text-align", "left")
                .set("padding", "0.75rem 1rem")
                .set("border-radius", "8px")
                .set("color", "#475569")
                .set("--_lumo-button-justify-content", "flex-start");

            menuBtn.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.navigate("espace-adherent",
                    com.vaadin.flow.router.QueryParameters.simple(Map.of("section", section))));
            });

            nav.add(menuBtn);
        }

        return nav;
    }

    private SideNav createMainNavigation() {
        SideNav nav = new SideNav();
        nav.getStyle()
            .set("--vaadin-side-nav-item-background-hover", "rgba(59, 107, 53, 0.1)")
            .set("--vaadin-side-nav-item-background-active", "rgba(59, 107, 53, 0.15)");

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();

        // Filter non-admin menu entries
        menuEntries.stream()
            .filter(entry -> !entry.path().startsWith("admin/") &&
                           !entry.path().equals("users") &&
                           !entry.path().equals("formbuilder"))
            .forEach(entry -> {
                SideNavItem item;
                if (entry.icon() != null) {
                    item = new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon()));
                } else {
                    item = new SideNavItem(entry.title(), entry.path());
                }
                styleNavItem(item);
                nav.addItem(item);
            });

        return nav;
    }

    private SideNav createAdminNavigation() {
        SideNav nav = new SideNav();
        nav.getStyle()
            .set("--vaadin-side-nav-item-background-hover", "rgba(44, 90, 160, 0.1)")
            .set("--vaadin-side-nav-item-background-active", "rgba(44, 90, 160, 0.15)");

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();

        // Filter admin menu entries
        menuEntries.stream()
            .filter(entry -> entry.path().startsWith("admin/"))
            .forEach(entry -> {
                SideNavItem item;
                if (entry.icon() != null) {
                    item = new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon()));
                } else {
                    item = new SideNavItem(entry.title(), entry.path());
                }
                styleNavItem(item);
                nav.addItem(item);
            });

        return nav;
    }

    private SideNav createSettingsNavigation() {
        SideNav nav = new SideNav();
        nav.getStyle()
            .set("--vaadin-side-nav-item-background-hover", "rgba(100, 116, 139, 0.1)")
            .set("--vaadin-side-nav-item-background-active", "rgba(100, 116, 139, 0.15)");

        List<MenuEntry> menuEntries = MenuConfiguration.getMenuEntries();

        // Filter settings menu entries
        menuEntries.stream()
            .filter(entry -> entry.path().equals("users") || entry.path().equals("formbuilder"))
            .forEach(entry -> {
                SideNavItem item;
                if (entry.icon() != null) {
                    item = new SideNavItem(entry.title(), entry.path(), new SvgIcon(entry.icon()));
                } else {
                    item = new SideNavItem(entry.title(), entry.path());
                }
                styleNavItem(item);
                nav.addItem(item);
            });

        return nav;
    }

    private void styleNavItem(SideNavItem item) {
        item.getStyle()
            .set("border-radius", "8px")
            .set("margin-bottom", "4px");
    }

    private Footer createFooter() {
        Footer footer = new Footer();
        footer.getStyle()
            .set("background", "white")
            .set("border-top", "1px solid #e2e8f0")
            .set("padding", "0.75rem 1rem")
            .set("text-align", "center");

        Span copyright = new Span("© 2024 FOS-Agri");
        copyright.getStyle()
            .set("color", "#94a3b8")
            .set("font-size", "0.75rem");

        footer.add(copyright);
        return footer;
    }

    @Override
    protected void afterNavigation() {
        super.afterNavigation();
        if (viewTitle != null) {
            viewTitle.setText(getCurrentPageTitle());
        }
    }

    private String getCurrentPageTitle() {
        return MenuConfiguration.getPageHeader(getContent()).orElse("");
    }
}
