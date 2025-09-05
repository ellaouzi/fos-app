package com.fosagri.application.views.enfants;

import com.fosagri.application.components.FileUploadComponent;
import com.fosagri.application.model.AdhEnfant;
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

public class EnfantFormDialog extends Dialog {

    private final Binder<AdhEnfant> binder = new Binder<>(AdhEnfant.class);
    private final AdhEnfant enfant;
    private final Consumer<AdhEnfant> saveCallback;
    private final Runnable closeCallback;

    private TextField codAgField;
    private TextField nomField;
    private TextField prenomField;
    private TextField cinField;
    private Select<String> sexeSelect;
    private DatePicker naissanceField;
    private Select<String> niveauInstructionSelect;
    private Select<String> lienParenteSelect;
    private TextField etatSanteField;
    private EmailField emailField;
    private TextField telephoneField;
    private TextField adresseField;
    private TextField nationaliteField;
    private Checkbox valideCheckbox;
    
    // File upload components
    private FileUploadComponent enfantPhotoUpload;
    private FileUploadComponent cinImageUpload; // Only for >18 years
    private FileUploadComponent attestationScolariteUpload;

    public EnfantFormDialog(AdhEnfant enfant, Consumer<AdhEnfant> saveCallback, Runnable closeCallback) {
        this.enfant = enfant;
        this.saveCallback = saveCallback;
        this.closeCallback = closeCallback;

        setHeaderTitle(enfant.getAdhEnfantId() == null ? "Nouvel Enfant" : "Modifier Enfant");
        setWidth("900px");
        setHeight("700px");

        createTabbedForm();
        createButtons();
        bindFields();
        
        binder.readBean(enfant);
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

        nomField = new TextField("Nom");
        nomField.setRequired(true);

        prenomField = new TextField("Prénom");
        prenomField.setRequired(true);

        cinField = new TextField("CIN");

        sexeSelect = new Select<>();
        sexeSelect.setLabel("Sexe");
        sexeSelect.setItems("M", "F");

        naissanceField = new DatePicker("Date de naissance");

        niveauInstructionSelect = new Select<>();
        niveauInstructionSelect.setLabel("Niveau d'instruction");
        niveauInstructionSelect.setItems("Aucun", "Primaire", "Collège", "Lycée", "Université", "Autres");

        lienParenteSelect = new Select<>();
        lienParenteSelect.setLabel("Lien de parenté");
        lienParenteSelect.setItems("Enfant", "Enfant adopté", "Pupille", "Autres");

        etatSanteField = new TextField("État de santé");

        emailField = new EmailField("Email");

        telephoneField = new TextField("Téléphone");

        adresseField = new TextField("Adresse postale");

        nationaliteField = new TextField("Nationalité");

        valideCheckbox = new Checkbox("Validé");
        valideCheckbox.setValue(true);

        // Add fields to form
        formLayout.add(
            codAgField, nomField,
            prenomField, cinField,
            sexeSelect, naissanceField,
            niveauInstructionSelect, lienParenteSelect,
            etatSanteField, emailField,
            telephoneField, adresseField,
            nationaliteField, valideCheckbox
        );

        layout.add(formLayout);
        return layout;
    }
    
    private VerticalLayout createDocumentsForm() {
        VerticalLayout layout = new VerticalLayout();
        
        // Create file upload components
        enfantPhotoUpload = new FileUploadComponent(
            "Photo de l'enfant", 
            "image/jpeg,image/png,image/gif", 
            5, 
            true,
            true  // isAvatar = true for profile photos
        );
        
        cinImageUpload = new FileUploadComponent(
            "Photo de la CIN (pour +18 ans)", 
            "image/jpeg,image/png,image/gif,application/pdf", 
            10, 
            true
        );
        
        attestationScolariteUpload = new FileUploadComponent(
            "Attestation de scolarité", 
            "application/pdf,image/jpeg,image/png", 
            10, 
            false
        );
        
        // Age-based visibility for CIN image
        updateCinImageVisibility();
        
        // Add listener to birth date to update CIN visibility
        naissanceField.addValueChangeListener(event -> updateCinImageVisibility());
        
        layout.add(enfantPhotoUpload, cinImageUpload, attestationScolariteUpload);
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
        if (enfant.getEnfant_photo() != null) {
            enfantPhotoUpload.setExistingFile(enfant.getEnfant_photo(), enfant.getEnfant_photo_filename(), enfant.getEnfant_photo_contentType());
        }
        if (enfant.getCin_image() != null) {
            cinImageUpload.setExistingFile(enfant.getCin_image(), enfant.getCin_image_filename(), enfant.getCin_image_contentType());
        }
        if (enfant.getAttestation_scolarite_photo() != null) {
            attestationScolariteUpload.setExistingFile(enfant.getAttestation_scolarite_photo(), enfant.getAttestation_scolarite_photo_filename(), enfant.getAttestation_scolarite_photo_contentType());
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
            .bind(AdhEnfant::getCodAg, AdhEnfant::setCodAg);

        binder.forField(nomField)
            .asRequired("Nom requis")
            .bind(AdhEnfant::getNom_pac, AdhEnfant::setNom_pac);

        binder.forField(prenomField)
            .asRequired("Prénom requis")
            .bind(AdhEnfant::getPr_pac, AdhEnfant::setPr_pac);

        binder.forField(cinField)
            .bind(AdhEnfant::getCin_PAC, AdhEnfant::setCin_PAC);

        binder.forField(sexeSelect)
            .bind(AdhEnfant::getSex_pac, AdhEnfant::setSex_pac);

        binder.forField(naissanceField)
            .bind(
                enfant -> enfant.getDat_n_pac() != null ? 
                    enfant.getDat_n_pac().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null,
                (enfant, localDate) -> enfant.setDat_n_pac(
                    localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null
                )
            );

        binder.forField(niveauInstructionSelect)
            .bind(AdhEnfant::getNiv_INSTRUCTION, AdhEnfant::setNiv_INSTRUCTION);

        binder.forField(lienParenteSelect)
            .bind(AdhEnfant::getLien_PAR, AdhEnfant::setLien_PAR);

        binder.forField(etatSanteField)
            .bind(AdhEnfant::getEtatSante, AdhEnfant::setEtatSante);

        binder.forField(emailField)
            .bind(AdhEnfant::getEmail, AdhEnfant::setEmail);

        binder.forField(telephoneField)
            .bind(AdhEnfant::getTele, AdhEnfant::setTele);

        binder.forField(adresseField)
            .bind(AdhEnfant::getAdrs_postale, AdhEnfant::setAdrs_postale);

        binder.forField(nationaliteField)
            .bind(AdhEnfant::getNationalite, AdhEnfant::setNationalite);

        binder.forField(valideCheckbox)
            .bind(AdhEnfant::isValide, AdhEnfant::setValide);
    }

    private void save() {
        try {
            enfant.setUpdated(new Date());
            if (enfant.getAdhEnfantId() == null) {
                enfant.setCreated(new Date());
            }
            binder.writeBean(enfant);
            
            // Save uploaded files
            saveUploadedFiles();
            
            saveCallback.accept(enfant);
            close();
        } catch (ValidationException e) {
            // Validation errors are shown automatically
        }
    }
    
    private void saveUploadedFiles() {
        // Save enfant photo
        FileUploadComponent.FileUploadData enfantPhoto = enfantPhotoUpload.getCurrentFile();
        if (enfantPhoto != null) {
            enfant.setEnfant_photo(enfantPhoto.getData());
            enfant.setEnfant_photo_filename(enfantPhoto.getFileName());
            enfant.setEnfant_photo_contentType(enfantPhoto.getContentType());
        }
        
        // Save CIN image (only if adult)
        FileUploadComponent.FileUploadData cinImage = cinImageUpload.getCurrentFile();
        if (cinImage != null && naissanceField.getValue() != null) {
            try {
                Date birthDate = Date.from(naissanceField.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                if (AgeUtils.isAdult(birthDate)) {
                    enfant.setCin_image(cinImage.getData());
                    enfant.setCin_image_filename(cinImage.getFileName());
                    enfant.setCin_image_contentType(cinImage.getContentType());
                }
            } catch (Exception e) {
                // Skip CIN image save if date conversion fails
            }
        }
        
        // Save attestation de scolarité
        FileUploadComponent.FileUploadData attestation = attestationScolariteUpload.getCurrentFile();
        if (attestation != null) {
            enfant.setAttestation_scolarite_photo(attestation.getData());
            enfant.setAttestation_scolarite_photo_filename(attestation.getFileName());
            enfant.setAttestation_scolarite_photo_contentType(attestation.getContentType());
        }
    }
}