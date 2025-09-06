package com.fosagri.application.views.prestations;

import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.services.PrestationRefService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PrestationRefFormDialog extends Dialog {
    
    private final PrestationRefService service;
    private final Binder<PrestationRef> binder = new Binder<>(PrestationRef.class);
    private final List<SaveListener> saveListeners = new ArrayList<>();
    private PrestationRef currentPrestation;
    
    private TextField labelField;
    private Select<String> typeField;
    private TextField valeursField;
    private TextArea descriptionField;
    private Select<String> isAdhField;
    private Checkbox openField;
    private DatePicker dateDuField;
    private DatePicker dateAuField;
    private Select<String> statutField;
    private IntegerField nombreLimitField;
    private Checkbox isarabicField;
    private Checkbox isattachedField;
    
    public PrestationRefFormDialog(PrestationRef prestationRef, PrestationRefService service) {
        this.service = service;
        this.currentPrestation = prestationRef;
        
        setHeaderTitle(prestationRef == null ? "Nouvelle Prestation" : "Modifier Prestation");
        setWidth("800px");
        setHeight("600px");
        setModal(true);
        setResizable(true);
        
        createForm();
        createButtons();
        
        if (prestationRef != null) {
            binder.setBean(prestationRef);
        } else {
            binder.setBean(new PrestationRef());
            // Valeurs par défaut
            openField.setValue(true);
            statutField.setValue("ACTIVE");
            isAdhField.setValue("ALL");
        }
    }
    
    private void createForm() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("600px", 2)
        );
        
        // Champs principaux
        labelField = new TextField("Label");
        labelField.setRequired(true);
        
        typeField = new Select<>();
        typeField.setLabel("Type");
        typeField.setItems("SERVICE", "FORMATION", "CONSULTATION", "AIDE", "SUBVENTION");
        
        valeursField = new TextField("Valeurs");
        
        descriptionField = new TextArea("Description");
        descriptionField.setMaxHeight("100px");
        
        isAdhField = new Select<>();
        isAdhField.setLabel("Destinataires");
        isAdhField.setItems("ALL", "ADH_ONLY", "NON_ADH_ONLY");
        isAdhField.setItemLabelGenerator(item -> {
            switch (item) {
                case "ALL": return "Tous";
                case "ADH_ONLY": return "Adhérents uniquement";
                case "NON_ADH_ONLY": return "Non-adhérents uniquement";
                default: return item;
            }
        });
        
        openField = new Checkbox("Prestation ouverte");
        
        dateDuField = new DatePicker("Date de début");
        dateAuField = new DatePicker("Date de fin");
        
        statutField = new Select<>();
        statutField.setLabel("Statut");
        statutField.setItems("ACTIVE", "INACTIVE", "ARCHIVE");
        
        nombreLimitField = new IntegerField("Nombre limite");
        nombreLimitField.setMin(0);
        nombreLimitField.setValue(0);
        
        isarabicField = new Checkbox("Version arabe");
        isattachedField = new Checkbox("Pièces jointes requises");
        
        // Configuration du binder
        binder.forField(labelField)
            .asRequired("Le label est obligatoire")
            .bind(PrestationRef::getLabel, PrestationRef::setLabel);
        
        binder.bind(typeField, PrestationRef::getType, PrestationRef::setType);
        binder.bind(valeursField, PrestationRef::getValeurs, PrestationRef::setValeurs);
        binder.bind(descriptionField, PrestationRef::getDescription, PrestationRef::setDescription);
        binder.bind(isAdhField, PrestationRef::getIs_adh, PrestationRef::setIs_adh);
        binder.bind(openField, PrestationRef::isOpen, PrestationRef::setOpen);
        
        binder.forField(dateDuField)
            .bind(
                prestationRef -> prestationRef.getDateDu() != null ? 
                    convertToLocalDate(prestationRef.getDateDu()) : null,
                (prestationRef, localDate) -> prestationRef.setDateDu(localDate != null ? 
                    Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null)
            );
        
        binder.forField(dateAuField)
            .bind(
                prestationRef -> prestationRef.getDateAu() != null ? 
                    convertToLocalDate(prestationRef.getDateAu()) : null,
                (prestationRef, localDate) -> prestationRef.setDateAu(localDate != null ? 
                    Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null)
            );
        
        binder.bind(statutField, PrestationRef::getStatut, PrestationRef::setStatut);
        binder.bind(nombreLimitField, PrestationRef::getNombreLimit, PrestationRef::setNombreLimit);
        binder.bind(isarabicField, PrestationRef::isIsarabic, PrestationRef::setIsarabic);
        binder.bind(isattachedField, PrestationRef::isIsattached, PrestationRef::setIsattached);
        
        formLayout.add(
            labelField, typeField,
            valeursField, descriptionField,
            isAdhField, openField,
            dateDuField, dateAuField,
            statutField, nombreLimitField,
            isarabicField, isattachedField
        );
        
        VerticalLayout content = new VerticalLayout(formLayout);
        content.setPadding(false);
        add(content);
    }
    
    private void createButtons() {
        Button saveButton = new Button("Enregistrer", e -> save());
        saveButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        saveButton.setIcon(VaadinIcon.CHECK.create());
        
        Button cancelButton = new Button("Annuler", e -> close());
        cancelButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        cancelButton.setIcon(VaadinIcon.CLOSE.create());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setSpacing(true);
        buttonLayout.setPadding(true);
        
        add(buttonLayout);
    }
    
    private LocalDate convertToLocalDate(Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        } else {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
    }
    
    private void save() {
        try {
            PrestationRef prestationRef = binder.getBean();
            
            // Validation business logic
            if (prestationRef.getLabel() == null || prestationRef.getLabel().trim().isEmpty()) {
                Notification.show("❌ Le label de la prestation est obligatoire", 
                                 3000, Notification.Position.MIDDLE);
                return;
            }
            
            if (prestationRef.getDateDu() != null && prestationRef.getDateAu() != null) {
                if (prestationRef.getDateDu().after(prestationRef.getDateAu())) {
                    Notification.show("❌ La date de début ne peut pas être postérieure à la date de fin", 
                                     4000, Notification.Position.MIDDLE);
                    return;
                }
            }
            
            // Check if this is a new prestation (no ID yet)
            boolean isNewPrestation = prestationRef.getId() == null;
            
            PrestationRef savedPrestation = service.save(prestationRef);
            
            // Show intermediate feedback for new prestations
            if (isNewPrestation) {
                Notification.show("🎉 Nouvelle prestation créée avec l'ID: " + savedPrestation.getId(), 
                                 2000, Notification.Position.MIDDLE);
            } else {
                Notification.show("✅ Prestation mise à jour avec succès", 
                                 2000, Notification.Position.MIDDLE);
            }
            
            saveListeners.forEach(listener -> listener.onSave(savedPrestation));
            close();
            
        } catch (Exception e) {
            Notification.show("❌ Erreur lors de l'enregistrement: " + e.getMessage(), 
                             5000, Notification.Position.MIDDLE);
        }
    }
    
    public void addSaveListener(SaveListener listener) {
        saveListeners.add(listener);
    }
    
    @FunctionalInterface
    public interface SaveListener {
        void onSave(PrestationRef prestationRef);
    }
}