package com.fosagri.application.views.admin;

import com.fosagri.application.entities.PrestationRef;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;

public class PrestationFormDialog extends Dialog {

    private final Binder<PrestationRef> binder = new Binder<>(PrestationRef.class);
    private final PrestationRef prestation;
    private final Consumer<PrestationRef> saveCallback;
    private final boolean isNew;

    // Form fields
    private TextField labelField;
    private ComboBox<String> typeCombo;
    private TextArea descriptionArea;
    private ComboBox<String> statutCombo;
    private DatePicker dateDuPicker;
    private DatePicker dateAuPicker;
    private IntegerField nombreLimitField;
    private Checkbox openCheckbox;
    private Checkbox isArabicCheckbox;
    private Checkbox isAttachedCheckbox;
    private ComboBox<String> isAdhCombo;

    public PrestationFormDialog(PrestationRef prestation, Consumer<PrestationRef> saveCallback) {
        this.prestation = prestation != null ? prestation : new PrestationRef();
        this.saveCallback = saveCallback;
        this.isNew = prestation == null;

        setHeaderTitle(isNew ? "Nouvelle Prestation" : "Modifier Prestation");
        setWidth("700px");
        setCloseOnOutsideClick(false);

        createForm();
        createFooter();
        bindFields();

        if (!isNew) {
            binder.readBean(this.prestation);
        }
    }

    private void createForm() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        // Label
        labelField = new TextField("Label");
        labelField.setRequired(true);
        labelField.setWidthFull();

        // Type
        typeCombo = new ComboBox<>("Type");
        typeCombo.setItems("Adhérents", "Enfants", "Sondage", "FORMATION", "CONSULTATION", "AIDE");
        typeCombo.setRequired(true);

        // Description
        descriptionArea = new TextArea("Description");
        descriptionArea.setWidthFull();
        descriptionArea.setHeight("100px");

        // Statut
        statutCombo = new ComboBox<>("Statut");
        statutCombo.setItems("ACTIVE", "INACTIVE", "ARCHIVEE");

        // Date Du
        dateDuPicker = new DatePicker("Date début");

        // Date Au
        dateAuPicker = new DatePicker("Date fin");

        // Nombre Limit
        nombreLimitField = new IntegerField("Limite d'inscriptions");
        nombreLimitField.setMin(0);
        nombreLimitField.setValue(0);
        nombreLimitField.setStepButtonsVisible(true);

        // Open checkbox
        openCheckbox = new Checkbox("Ouvert aux inscriptions");

        // Is Arabic
        isArabicCheckbox = new Checkbox("Formulaire en arabe");

        // Is Attached
        isAttachedCheckbox = new Checkbox("Pièces jointes requises");

        // Is Adh
        isAdhCombo = new ComboBox<>("Cible");
        isAdhCombo.setItems("OUI", "OUI,OUIONSSA", "OUI,OUIONSSA,OUIONCA", "OUIONSSA", "OUIONCA", "ALL", "ADH_ONLY");
        isAdhCombo.setHelperText("Qui peut postuler à cette prestation");

        // Add fields to form
        formLayout.add(labelField, 2);
        formLayout.add(typeCombo, statutCombo);
        formLayout.add(descriptionArea, 2);
        formLayout.add(dateDuPicker, dateAuPicker);
        formLayout.add(nombreLimitField, isAdhCombo);

        // Checkboxes in horizontal layout
        HorizontalLayout checkboxes = new HorizontalLayout(openCheckbox, isArabicCheckbox, isAttachedCheckbox);
        checkboxes.setSpacing(true);
        checkboxes.getStyle().set("margin-top", "1rem");

        content.add(formLayout, checkboxes);
        add(content);
    }

    private void createFooter() {
        Button cancelBtn = new Button("Annuler", e -> close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button saveBtn = new Button("Enregistrer", e -> save());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(cancelBtn, saveBtn);
    }

    private void bindFields() {
        binder.forField(labelField)
            .asRequired("Le label est obligatoire")
            .bind(PrestationRef::getLabel, PrestationRef::setLabel);

        binder.forField(typeCombo)
            .asRequired("Le type est obligatoire")
            .bind(PrestationRef::getType, PrestationRef::setType);

        binder.forField(descriptionArea)
            .bind(PrestationRef::getDescription, PrestationRef::setDescription);

        binder.forField(statutCombo)
            .bind(PrestationRef::getStatut, PrestationRef::setStatut);

        binder.forField(dateDuPicker)
            .bind(
                p -> p.getDateDu() != null ? p.getDateDu().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null,
                (p, v) -> p.setDateDu(v != null ? Date.from(v.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null)
            );

        binder.forField(dateAuPicker)
            .bind(
                p -> p.getDateAu() != null ? p.getDateAu().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null,
                (p, v) -> p.setDateAu(v != null ? Date.from(v.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null)
            );

        binder.forField(nombreLimitField)
            .bind(PrestationRef::getNombreLimit, PrestationRef::setNombreLimit);

        binder.forField(openCheckbox)
            .bind(PrestationRef::isOpen, PrestationRef::setOpen);

        binder.forField(isArabicCheckbox)
            .bind(PrestationRef::isIsarabic, PrestationRef::setIsarabic);

        binder.forField(isAttachedCheckbox)
            .bind(PrestationRef::isIsattached, PrestationRef::setIsattached);

        binder.forField(isAdhCombo)
            .bind(PrestationRef::getIs_adh, PrestationRef::setIs_adh);
    }

    private void save() {
        try {
            binder.writeBean(prestation);
            saveCallback.accept(prestation);
            close();
        } catch (ValidationException e) {
            Notification.show("Veuillez corriger les erreurs", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
