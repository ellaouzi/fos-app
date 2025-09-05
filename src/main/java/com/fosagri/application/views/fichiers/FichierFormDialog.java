package com.fosagri.application.views.fichiers;

import com.fosagri.application.model.Fichier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;

public class FichierFormDialog extends Dialog {

    private final Binder<Fichier> binder = new Binder<>(Fichier.class);
    private final Fichier fichier;
    private final Consumer<Fichier> saveCallback;
    private final Runnable closeCallback;

    private TextField codAgField;
    private TextField idadhField;
    private TextField fileNameField;
    private TextArea designationField;
    private TextField extensionField;
    private TextField documentField;
    private DatePicker dateCreationField;

    public FichierFormDialog(Fichier fichier, Consumer<Fichier> saveCallback, Runnable closeCallback) {
        this.fichier = fichier;
        this.saveCallback = saveCallback;
        this.closeCallback = closeCallback;

        setHeaderTitle(fichier.getId() == null ? "Nouveau Fichier" : "Modifier Fichier");
        setWidth("800px");
        setHeight("700px");

        createForm();
        createButtons();
        bindFields();
        
        binder.readBean(fichier);
    }

    private void createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        codAgField = new TextField("Code Agent");
        codAgField.setRequired(true);

        idadhField = new TextField("ID Adhérent");

        fileNameField = new TextField("Nom du fichier");
        fileNameField.setRequired(true);

        designationField = new TextArea("Désignation");
        designationField.setHeight("100px");

        extensionField = new TextField("Extension");

        documentField = new TextField("Document");

        dateCreationField = new DatePicker("Date de création");

        formLayout.add(
            codAgField, idadhField,
            fileNameField, extensionField,
            documentField, dateCreationField,
            designationField
        );

        formLayout.setColspan(designationField, 2);

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
        binder.forField(codAgField)
            .asRequired("Code Agent requis")
            .bind(Fichier::getCod_ag, Fichier::setCod_ag);

        binder.forField(idadhField)
            .bind(Fichier::getIdadh, Fichier::setIdadh);

        binder.forField(fileNameField)
            .asRequired("Nom du fichier requis")
            .bind(Fichier::getFileName, Fichier::setFileName);

        binder.forField(designationField)
            .bind(Fichier::getDesignation, Fichier::setDesignation);

        binder.forField(extensionField)
            .bind(Fichier::getExtention, Fichier::setExtention);

        binder.forField(documentField)
            .bind(Fichier::getDocument, Fichier::setDocument);

        binder.forField(dateCreationField)
            .bind(
                fichier -> fichier.getCreated() != null ? 
                    fichier.getCreated().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null,
                (fichier, localDate) -> fichier.setCreated(
                    localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null
                )
            );
    }

    private void save() {
        try {
            if (fichier.getId() == null) {
                fichier.setCreated(new Date());
            }
            binder.writeBean(fichier);
            saveCallback.accept(fichier);
            close();
        } catch (ValidationException e) {
            // Validation errors are shown automatically
        }
    }
}