package com.fosagri.application.views.resultats;

import com.fosagri.application.model.Resultat;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.function.Consumer;

public class ResultatFormDialog extends Dialog {

    private final Binder<Resultat> binder = new Binder<>(Resultat.class);
    private final Resultat resultat;
    private final Consumer<Resultat> saveCallback;
    private final Runnable closeCallback;

    private TextField pprField;
    private TextField idadhField;
    private TextField operationField;
    private Select<String> statutSelect;
    private NumberField operationIdField;

    public ResultatFormDialog(Resultat resultat, Consumer<Resultat> saveCallback, Runnable closeCallback) {
        this.resultat = resultat;
        this.saveCallback = saveCallback;
        this.closeCallback = closeCallback;

        setHeaderTitle(resultat.getId() == null ? "Nouveau Résultat" : "Modifier Résultat");
        setWidth("800px");
        setHeight("700px");

        createForm();
        createButtons();
        bindFields();
        
        binder.readBean(resultat);
    }

    private void createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        pprField = new TextField("PPR");
        pprField.setRequired(true);

        idadhField = new TextField("ID Adhérent");
        idadhField.setRequired(true);

        operationField = new TextField("Opération");
        operationField.setRequired(true);

        statutSelect = new Select<>();
        statutSelect.setLabel("Statut");
        statutSelect.setItems("En cours", "Terminé", "Annulé", "En attente", "Validé", "Rejeté");

        operationIdField = new NumberField("ID Opération");

        formLayout.add(
            pprField, idadhField,
            operationField, statutSelect,
            operationIdField
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
        binder.forField(pprField)
            .asRequired("PPR requis")
            .bind(Resultat::getPpr, Resultat::setPpr);

        binder.forField(idadhField)
            .asRequired("ID Adhérent requis")
            .bind(Resultat::getIdadh, Resultat::setIdadh);

        binder.forField(operationField)
            .asRequired("Opération requise")
            .bind(Resultat::getOperation, Resultat::setOperation);

        binder.forField(statutSelect)
            .asRequired("Statut requis")
            .bind(Resultat::getStatut, Resultat::setStatut);

        binder.forField(operationIdField)
            .bind(
                resultat -> resultat.getOperation_id() != null ? resultat.getOperation_id().doubleValue() : null,
                (resultat, value) -> resultat.setOperation_id(value != null ? value.longValue() : null)
            );
    }

    private void save() {
        try {
            binder.writeBean(resultat);
            saveCallback.accept(resultat);
            close();
        } catch (ValidationException e) {
            // Validation errors are shown automatically
        }
    }
}