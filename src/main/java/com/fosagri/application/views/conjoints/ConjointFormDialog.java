package com.fosagri.application.views.conjoints;

import com.fosagri.application.components.FileUploadComponent;
import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.utils.AgeUtils;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;

public class ConjointFormDialog extends Dialog {

    private final Binder<AdhConjoint> binder = new Binder<>(AdhConjoint.class);
    private final AdhConjoint conjoint;
    private final Consumer<AdhConjoint> saveCallback;
    private final Runnable closeCallback;

    private TextField codAgField;
    private TextField numConjField;
    private TextField nomField;
    private TextField prenomField;
    private TextField cinField;
    private Select<String> sexeSelect;
    private DatePicker naissanceField;
    private DatePicker mariageField;
    private Select<String> situationSelect;
    private EmailField emailField;
    private TextField telephoneField;
    private TextField adresseField;
    private TextField villeField;
    private Checkbox valideCheckbox;
    
    // File upload components
    private FileUploadComponent conjointPhotoUpload;
    private FileUploadComponent cinImageUpload; // Only for >18 years
    private FileUploadComponent acteMariagePhotoUpload;

    public ConjointFormDialog(AdhConjoint conjoint, Consumer<AdhConjoint> saveCallback, Runnable closeCallback) {
        this.conjoint = conjoint;
        this.saveCallback = saveCallback;
        this.closeCallback = closeCallback;

        setHeaderTitle(conjoint.getAdhConjointId() == 0 ? "Nouveau Conjoint" : "Modifier Conjoint");
        setWidth("900px");
        setHeight("700px");

        createTabbedForm();
        createButtons();
        bindFields();
        
        binder.readBean(conjoint);
        loadExistingFiles();
    }

    private void createTabbedForm() {
        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(false);

        // Create tabs
        Tab infoTab = new Tab("Informations générales");
        Tab documentsTab = new Tab("Documents et photos");
        Tabs tabs = new Tabs(infoTab, documentsTab);
        
        // Create layouts for each tab
        VerticalLayout infoLayout = createInfoForm();
        VerticalLayout documentsLayout = createDocumentsForm();
        
        // Initially show info tab
        documentsLayout.setVisible(false);
        
        tabs.addSelectedChangeListener(event -> {
            if (event.getSelectedTab() == infoTab) {
                infoLayout.setVisible(true);
                documentsLayout.setVisible(false);
            } else {
                infoLayout.setVisible(false);
                documentsLayout.setVisible(true);
            }
        });

        mainLayout.add(tabs, infoLayout, documentsLayout);
        add(mainLayout);
    }
    
    private VerticalLayout createInfoForm() {
        VerticalLayout layout = new VerticalLayout();
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        // Create form fields
        codAgField = new TextField("Code Agent");
        codAgField.setRequired(true);

        numConjField = new TextField("Numéro Conjoint");

        nomField = new TextField("Nom");
        nomField.setRequired(true);

        prenomField = new TextField("Prénom");
        prenomField.setRequired(true);

        cinField = new TextField("CIN");

        sexeSelect = new Select<>();
        sexeSelect.setLabel("Sexe");
        sexeSelect.setItems("M", "F");

        naissanceField = new DatePicker("Date de naissance");

        mariageField = new DatePicker("Date de mariage");

        situationSelect = new Select<>();
        situationSelect.setLabel("Situation");
        situationSelect.setItems("Marié(e)", "Divorcé(e)", "Veuf(ve)");

        emailField = new EmailField("Email");

        telephoneField = new TextField("Téléphone");

        adresseField = new TextField("Adresse");

        villeField = new TextField("Ville");

        valideCheckbox = new Checkbox("Validé");
        valideCheckbox.setValue(true);

        // Add fields to form
        formLayout.add(
            codAgField, numConjField,
            nomField, prenomField,
            cinField, sexeSelect,
            naissanceField, mariageField,
            situationSelect, emailField,
            telephoneField, adresseField,
            villeField, valideCheckbox
        );

        layout.add(formLayout);
        return layout;
    }
    
    private VerticalLayout createDocumentsForm() {
        VerticalLayout layout = new VerticalLayout();
        
        // Create file upload components
        conjointPhotoUpload = new FileUploadComponent(
            "Photo du conjoint", 
            "image/jpeg,image/png,image/gif", 
            5, 
            true,
            true  // isAvatar = true for profile photos
        );
        
        cinImageUpload = new FileUploadComponent(
            "Photo de la CIN",
            "image/jpeg,image/png,image/gif,application/pdf", 
            10, 
            true
        );
        
        acteMariagePhotoUpload = new FileUploadComponent(
            "Photo de l'acte de mariage", 
            "application/pdf,image/jpeg,image/png", 
            10, 
            false
        );
        
        // Age-based visibility for CIN image
        updateCinImageVisibility();
        
        // Add listener to birth date to update CIN visibility
        naissanceField.addValueChangeListener(event -> updateCinImageVisibility());
        
        layout.add(conjointPhotoUpload, cinImageUpload, acteMariagePhotoUpload);
        return layout;
    }
    
    private void updateCinImageVisibility() {
        if (cinImageUpload != null && naissanceField.getValue() != null) {
            try {
                Date birthDate = Date.from(naissanceField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                boolean showCinImage = AgeUtils.isAdult(birthDate);
                cinImageUpload.setVisible(showCinImage);
            } catch (Exception e) {
                // If there's an issue with date conversion, hide CIN image by default
                cinImageUpload.setVisible(false);
            }
        } else if (cinImageUpload != null) {
            cinImageUpload.setVisible(false);
        }
    }
    
    private void loadExistingFiles() {
        if (conjoint.getConjoint_photo() != null) {
            conjointPhotoUpload.setExistingFile(conjoint.getConjoint_photo(), conjoint.getConjoint_photo_filename(), conjoint.getConjoint_photo_contentType());
        }
        if (conjoint.getCin_image() != null) {
            cinImageUpload.setExistingFile(conjoint.getCin_image(), conjoint.getCin_image_filename(), conjoint.getCin_image_contentType());
        }
        if (conjoint.getActe_mariage_photo() != null) {
            acteMariagePhotoUpload.setExistingFile(conjoint.getActe_mariage_photo(), conjoint.getActe_mariage_photo_filename(), conjoint.getActe_mariage_photo_contentType());
        }
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
            .bind(AdhConjoint::getCodAg, AdhConjoint::setCodAg);

        binder.forField(numConjField)
            .bind(AdhConjoint::getNUM_CONJ, AdhConjoint::setNUM_CONJ);

        binder.forField(nomField)
            .asRequired("Nom requis")
            .bind(AdhConjoint::getNOM_CONJ, AdhConjoint::setNOM_CONJ);

        binder.forField(prenomField)
            .asRequired("Prénom requis")
            .bind(AdhConjoint::getPR_CONJ, AdhConjoint::setPR_CONJ);

        binder.forField(cinField)
            .bind(AdhConjoint::getCIN_CONJ, AdhConjoint::setCIN_CONJ);

        binder.forField(sexeSelect)
            .bind(AdhConjoint::getSex_CONJ, AdhConjoint::setSex_CONJ);

        binder.forField(naissanceField)
            .bind(
                conjoint -> conjoint.getDat_N_CONJ() != null ? 
                    conjoint.getDat_N_CONJ().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null,
                (conjoint, localDate) -> conjoint.setDat_N_CONJ(
                    localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null
                )
            );

        binder.forField(mariageField)
            .bind(
                conjoint -> conjoint.getDat_MAR() != null ? 
                    conjoint.getDat_MAR().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null,
                (conjoint, localDate) -> conjoint.setDat_MAR(
                    localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null
                )
            );

        binder.forField(situationSelect)
            .bind(AdhConjoint::getSit_CJ, AdhConjoint::setSit_CJ);

        binder.forField(emailField)
            .bind(AdhConjoint::getEmail, AdhConjoint::setEmail);

        binder.forField(telephoneField)
            .bind(AdhConjoint::getTele, AdhConjoint::setTele);

        binder.forField(adresseField)
            .bind(AdhConjoint::getAdrs_POSTALE, AdhConjoint::setAdrs_POSTALE);

        binder.forField(villeField)
            .bind(AdhConjoint::getVille, AdhConjoint::setVille);

        binder.forField(valideCheckbox)
            .bind(AdhConjoint::isValide, AdhConjoint::setValide);
    }

    private void save() {
        try {
            conjoint.setUpdated(new Date());
            binder.writeBean(conjoint);
            
            // Save uploaded files
            saveUploadedFiles();
            
            saveCallback.accept(conjoint);
            close();
        } catch (ValidationException e) {
            // Validation errors are shown automatically
        }
    }
    
    private void saveUploadedFiles() {
        // Save conjoint photo
        FileUploadComponent.FileUploadData conjointPhoto = conjointPhotoUpload.getCurrentFile();
        if (conjointPhoto != null) {
            conjoint.setConjoint_photo(conjointPhoto.getData());
            conjoint.setConjoint_photo_filename(conjointPhoto.getFileName());
            conjoint.setConjoint_photo_contentType(conjointPhoto.getContentType());
        }
        
        // Save CIN image (only if adult)
        FileUploadComponent.FileUploadData cinImage = cinImageUpload.getCurrentFile();
        if (cinImage != null && naissanceField.getValue() != null) {
            try {
                Date birthDate = Date.from(naissanceField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                if (AgeUtils.isAdult(birthDate)) {
                    conjoint.setCin_image(cinImage.getData());
                    conjoint.setCin_image_filename(cinImage.getFileName());
                    conjoint.setCin_image_contentType(cinImage.getContentType());
                }
            } catch (Exception e) {
                // Skip CIN image save if date conversion fails
            }
        }
        
        // Save acte de mariage photo
        FileUploadComponent.FileUploadData acteMariage = acteMariagePhotoUpload.getCurrentFile();
        if (acteMariage != null) {
            conjoint.setActe_mariage_photo(acteMariage.getData());
            conjoint.setActe_mariage_photo_filename(acteMariage.getFileName());
            conjoint.setActe_mariage_photo_contentType(acteMariage.getContentType());
        }
    }
}