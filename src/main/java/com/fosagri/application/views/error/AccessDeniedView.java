package com.fosagri.application.views.error;

import com.fosagri.application.security.AuthenticatedUser;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@PageTitle("Accès Refusé")
@Route(value = "access-denied", layout = MainLayout.class)
@AnonymousAllowed
public class AccessDeniedView extends VerticalLayout implements BeforeEnterObserver {

    private final AuthenticatedUser authenticatedUser;

    public AccessDeniedView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;

        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        Div iconContainer = new Div();
        iconContainer.getStyle()
            .set("font-size", "5rem")
            .set("margin-bottom", "1rem");
        iconContainer.setText("\uD83D\uDEAB"); // Unicode for 🚫

        H1 title = new H1("Accès Refusé");
        title.getStyle()
            .set("color", "var(--lumo-error-color)")
            .set("margin", "0 0 0.5rem 0");

        Paragraph message = new Paragraph(
            "Vous n'avez pas les permissions nécessaires pour accéder à cette page."
        );
        message.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("text-align", "center")
            .set("max-width", "400px");

        Button homeButton = new Button("Retour à l'accueil", VaadinIcon.HOME.create());
        homeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        homeButton.addClickListener(e -> {
            String targetUrl = getDefaultUrlForUser();
            getUI().ifPresent(ui -> ui.navigate(targetUrl));
        });

        Button logoutButton = new Button("Déconnexion", VaadinIcon.SIGN_OUT.create());
        logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        logoutButton.addClickListener(e -> authenticatedUser.logout());

        add(iconContainer, title, message, homeButton, logoutButton);
    }

    private String getDefaultUrlForUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role) || "ADMIN".equals(role)) {
                    return "dashboard";
                }
            }
        }
        return "espace-adherent";
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // If not authenticated, redirect to login
        if (!authenticatedUser.isAuthenticated()) {
            event.forwardTo("login");
        }
    }
}
