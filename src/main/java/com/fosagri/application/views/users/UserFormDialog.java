package com.fosagri.application.views.users;

import com.fosagri.application.model.Utilisateur;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.Date;
import java.util.function.Consumer;

public class UserFormDialog extends Dialog {

    private final Binder<Utilisateur> binder = new Binder<>(Utilisateur.class);
    private final Utilisateur user;
    private final Consumer<Utilisateur> saveCallback;
    private final Runnable closeCallback;

    private TextField usernameField;
    private PasswordField passwordField;
    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField cinField;
    private TextField pprField;
    private TextField adhidField;
    private Select<String> isadhSelect;
    private Checkbox enabledCheckbox;

    public UserFormDialog(Utilisateur user, Consumer<Utilisateur> saveCallback, Runnable closeCallback) {
        this.user = user;
        this.saveCallback = saveCallback;
        this.closeCallback = closeCallback;

        setHeaderTitle(user.getId() == null ? "Nouvel Utilisateur" : "Modifier Utilisateur");
        setWidth("600px");
        setHeight("500px");

        createForm();
        createButtons();
        bindFields();
        
        binder.readBean(user);
    }

    private void createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        // Create form fields
        usernameField = new TextField("Nom d'utilisateur");
        usernameField.setRequired(true);

        passwordField = new PasswordField("Mot de passe");
        if (user.getId() == null) {
            passwordField.setRequired(true);
        }

        nomField = new TextField("Nom");
        nomField.setRequired(true);

        prenomField = new TextField("Prénom");
        prenomField.setRequired(true);

        emailField = new EmailField("Email");
        emailField.setRequired(true);

        cinField = new TextField("CIN");
        cinField.setRequired(true);

        pprField = new TextField("PPR");

        adhidField = new TextField("ID Adhérent");

        isadhSelect = new Select<>();
        isadhSelect.setLabel("Est adhérent");
        isadhSelect.setItems("OUI", "NON");
        isadhSelect.setValue("NON");

        enabledCheckbox = new Checkbox("Compte activé");
        enabledCheckbox.setValue(true);

        // Add fields to form
        formLayout.add(
            usernameField, passwordField,
            nomField, prenomField,
            emailField, cinField,
            pprField, adhidField,
            isadhSelect, enabledCheckbox
        );

        add(formLayout);
    }

    private void createButtons() {
        Button saveButton = new Button("Sauvegarder", e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Annuler", e -> close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        getFooter().add(buttonLayout);
    }

    private void bindFields() {
        binder.forField(usernameField)
            .asRequired("Nom d'utilisateur requis")
            .bind(Utilisateur::getUsername, Utilisateur::setUsername);

        binder.forField(passwordField)
            .bind(Utilisateur::getPassword, Utilisateur::setPassword);

        binder.forField(nomField)
            .asRequired("Nom requis")
            .bind(Utilisateur::getNom, Utilisateur::setNom);

        binder.forField(prenomField)
            .asRequired("Prénom requis")
            .bind(Utilisateur::getPrenom, Utilisateur::setPrenom);

        binder.forField(emailField)
            .asRequired("Email requis")
            .bind(Utilisateur::getEmail, Utilisateur::setEmail);

        binder.forField(cinField)
            .asRequired("CIN requis")
            .bind(Utilisateur::getCin, Utilisateur::setCin);

        binder.forField(pprField)
            .bind(Utilisateur::getPpr, Utilisateur::setPpr);

        binder.forField(adhidField)
            .bind(Utilisateur::getAdhid, Utilisateur::setAdhid);

        binder.forField(isadhSelect)
            .bind(Utilisateur::getIsadh, Utilisateur::setIsadh);

        binder.forField(enabledCheckbox)
            .bind(Utilisateur::isEnabled, Utilisateur::setEnabled);
    }

    private void save() {
        try {
            if (user.getId() == null) {
                user.setCreated(new Date());
            }
            user.setUpdated(new Date());
            
            binder.writeBean(user);
            saveCallback.accept(user);
            close();
        } catch (ValidationException e) {
            // Validation errors are shown automatically
        }
    }
}