package com.fosagri.application.views.error;

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
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.router.ParentLayout;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@ParentLayout(MainLayout.class)
public class RouteNotFoundError extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    public RouteNotFoundError() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getStyle().set("background-color", "var(--lumo-contrast-5pct)");

        Div iconContainer = new Div();
        iconContainer.getStyle()
            .set("font-size", "5rem")
            .set("margin-bottom", "1rem");
        iconContainer.setText("404");
        iconContainer.getStyle()
            .set("color", "var(--lumo-primary-color)")
            .set("font-weight", "bold");

        H1 title = new H1("Page Non Trouvée");
        title.getStyle()
            .set("color", "var(--lumo-body-text-color)")
            .set("margin", "0 0 0.5rem 0");

        Paragraph message = new Paragraph(
            "La page que vous recherchez n'existe pas ou a été déplacée."
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

        add(iconContainer, title, message, homeButton);
    }

    private String getDefaultUrlForUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                String role = authority.getAuthority();
                if ("ROLE_ADMIN".equals(role) || "ADMIN".equals(role)) {
                    return "dashboard";
                }
            }
            return "espace-adherent";
        }
        return "login";
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
