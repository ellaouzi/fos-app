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

/**
 * Modern main layout with grouped navigation.
 */
@Layout
@PermitAll
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private H1 viewTitle;
    private final AuthenticatedUser authenticatedUser;
    private boolean isAdmin = false;

    public MainLayout(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        // Check if user is admin
        Optional<Utilisateur> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            Utilisateur user = maybeUser.get();
            isAdmin = user.getAuthorities() != null &&
                user.getAuthorities().stream()
                    .anyMatch(a -> "ADMIN".equals(a.getRole()));
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
        if ("login".equals(path) || path.isEmpty()) {
            setDrawerOpened(false);
            getElement().getStyle().set("--vaadin-app-layout-drawer-offset-size", "0px");
        }
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu toggle");
        toggle.getStyle()
            .set("color", "#3b6b35")
            .set("margin-left", "0.5rem");

        viewTitle = new H1();
        viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);
        viewTitle.getStyle()
            .set("color", "#1e293b")
            .set("font-weight", "600");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle()
            .set("padding", "0 1.5rem")
            .set("background", "white")
            .set("border-bottom", "1px solid #e2e8f0")
            .set("height", "64px");

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

        Optional<Utilisateur> maybeUser = authenticatedUser.get();

        if (maybeUser.isPresent()) {
            Utilisateur user = maybeUser.get();

            String displayName = user.getPrenom() != null && user.getNom() != null
                ? user.getPrenom() + " " + user.getNom()
                : user.getUsername();

            // Role badge
            String role = user.getAuthorities() != null && !user.getAuthorities().isEmpty()
                ? user.getAuthorities().get(0).getRole()
                : "USER";

            Span roleBadge = new Span(role);
            roleBadge.getStyle()
                .set("background", isAdmin ? "linear-gradient(135deg, #3b6b35, #2c5aa0)" : "#e2e8f0")
                .set("color", isAdmin ? "white" : "#64748b")
                .set("padding", "4px 12px")
                .set("border-radius", "20px")
                .set("font-size", "0.75rem")
                .set("font-weight", "600");

            Avatar avatar = new Avatar(displayName);
            avatar.getStyle()
                .set("cursor", "pointer")
                .set("--vaadin-avatar-size", "38px");

            if (isAdmin) {
                avatar.setColorIndex(1);
            }

            MenuBar menuBar = new MenuBar();
            menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

            HorizontalLayout userDisplay = new HorizontalLayout(avatar);
            userDisplay.setAlignItems(FlexComponent.Alignment.CENTER);
            userDisplay.setSpacing(true);
            userDisplay.getStyle().set("cursor", "pointer");

            var menuItem = menuBar.addItem(userDisplay);
            var subMenu = menuItem.getSubMenu();

            // User info in submenu
            Div userInfoDiv = new Div();
            userInfoDiv.getStyle()
                .set("padding", "0.75rem 1rem")
                .set("border-bottom", "1px solid #e2e8f0")
                .set("min-width", "200px");

            Span nameSpan = new Span(displayName);
            nameSpan.getStyle()
                .set("display", "block")
                .set("font-weight", "600")
                .set("color", "#1e293b");

            Span roleSpan = new Span(role);
            roleSpan.getStyle()
                .set("display", "block")
                .set("font-size", "0.8rem")
                .set("color", "#64748b");

            userInfoDiv.add(nameSpan, roleSpan);
            subMenu.addItem(userInfoDiv);

            subMenu.addItem("Se déconnecter", e -> authenticatedUser.logout());

            userMenu.add(roleBadge, menuBar);
        }

        return userMenu;
    }

    private void addDrawerContent() {
        // Brand header with gradient
        Div brandHeader = new Div();
        brandHeader.getStyle()
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 100%)")
            .set("padding", "1.5rem")
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "1rem");

        com.vaadin.flow.component.html.Image logo = new com.vaadin.flow.component.html.Image(
            "images/logo_FosAgri.png", "FOS-Agri Logo");
        logo.setWidth("50px");
        logo.setHeight("50px");
        logo.getStyle()
            .set("object-fit", "contain")
            .set("background", "white")
            .set("border-radius", "12px")
            .set("padding", "6px");

        Div brandText = new Div();
        Span appName = new Span("FOS-Agri");
        appName.getStyle()
            .set("color", "white")
            .set("font-size", "1.4rem")
            .set("font-weight", "700")
            .set("display", "block");

        Span appSubtitle = new Span("E-Services");
        appSubtitle.getStyle()
            .set("color", "rgba(255,255,255,0.8)")
            .set("font-size", "0.85rem")
            .set("display", "block");

        brandText.add(appName, appSubtitle);
        brandHeader.add(logo, brandText);

        // Navigation container
        VerticalLayout navContainer = new VerticalLayout();
        navContainer.setPadding(true);
        navContainer.setSpacing(false);
        navContainer.getStyle().set("padding", "1rem");

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
            .set("padding", "1rem");

        Optional<Utilisateur> maybeUser = authenticatedUser.get();
        if (maybeUser.isPresent()) {
            Utilisateur user = maybeUser.get();

            HorizontalLayout userRow = new HorizontalLayout();
            userRow.setWidthFull();
            userRow.setAlignItems(FlexComponent.Alignment.CENTER);
            userRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

            String displayName = user.getPrenom() != null && user.getNom() != null
                ? user.getPrenom() + " " + user.getNom()
                : user.getUsername();

            Avatar avatar = new Avatar(displayName);
            avatar.getStyle().set("--vaadin-avatar-size", "36px");
            if (isAdmin) {
                avatar.setColorIndex(1);
            }

            Div userInfo = new Div();
            Span userName = new Span(displayName);
            userName.getStyle()
                .set("display", "block")
                .set("font-weight", "600")
                .set("font-size", "0.9rem")
                .set("color", "#1e293b");

            String role = user.getAuthorities() != null && !user.getAuthorities().isEmpty()
                ? user.getAuthorities().get(0).getRole()
                : "USER";
            Span userRole = new Span(role);
            userRole.getStyle()
                .set("display", "block")
                .set("font-size", "0.75rem")
                .set("color", "#64748b");

            userInfo.add(userName, userRole);

            HorizontalLayout leftPart = new HorizontalLayout(avatar, userInfo);
            leftPart.setAlignItems(FlexComponent.Alignment.CENTER);
            leftPart.setSpacing(true);

            Button logoutBtn = new Button(VaadinIcon.SIGN_OUT.create());
            logoutBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            logoutBtn.getStyle()
                .set("color", "#64748b")
                .set("cursor", "pointer");
            logoutBtn.addClickListener(e -> authenticatedUser.logout());

            userRow.add(leftPart, logoutBtn);
            footer.add(userRow);
        }

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
