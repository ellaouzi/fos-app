package com.fosagri.application.views.login;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Connexion | FOS AGRI")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Simple gradient background
        getStyle()
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 50%, #d4a949 100%)")
            .set("padding", "2rem");

        // White card container
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("padding", "2.5rem")
            .set("box-shadow", "0 20px 60px rgba(0,0,0,0.3)")
            .set("max-width", "400px")
            .set("width", "100%");

        // Logo
        Image logo = new Image("images/logo_FosAgri.png", "FOS-Agri");
        logo.setWidth("120px");
        logo.setHeight("120px");
        logo.getStyle().set("display", "block").set("margin", "0 auto 1rem auto");

        // Title
        H1 title = new H1("FOS-Agri");
        title.getStyle()
            .set("color", "#3b6b35")
            .set("text-align", "center")
            .set("margin", "0 0 0.5rem 0")
            .set("font-size", "1.8rem");

        Paragraph subtitle = new Paragraph("E-Services");
        subtitle.getStyle()
            .set("color", "#2c5aa0")
            .set("text-align", "center")
            .set("margin", "0 0 1.5rem 0")
            .set("font-weight", "600");

        // Configure login form
        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        LoginI18n i18n = LoginI18n.createDefault();
        LoginI18n.Form form = i18n.getForm();
        form.setTitle("Connexion");
        form.setUsername("Nom d'utilisateur");
        form.setPassword("Mot de passe");
        form.setSubmit("Se connecter");
        i18n.setForm(form);

        LoginI18n.ErrorMessage error = i18n.getErrorMessage();
        error.setTitle("Échec de la connexion");
        error.setMessage("Identifiants incorrects");
        i18n.setErrorMessage(error);

        loginForm.setI18n(i18n);

        // Build card
        VerticalLayout cardContent = new VerticalLayout(logo, title, subtitle, loginForm);
        cardContent.setAlignItems(FlexComponent.Alignment.CENTER);
        cardContent.setPadding(false);
        cardContent.setSpacing(false);
        card.add(cardContent);

        // Footer
        Paragraph footer = new Paragraph("© 2024 FOS-Agri");
        footer.getStyle()
            .set("color", "white")
            .set("margin-top", "1.5rem");

        add(card, footer);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
