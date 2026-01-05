package com.fosagri.application.views.agents;

import com.fosagri.application.components.FileUploadComponent;
import com.fosagri.application.model.AdhAgent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Consumer;

public class AgentFormDialog extends Dialog {

    private final Binder<AdhAgent> binder = new Binder<>(AdhAgent.class);
    private final AdhAgent agent;
    private final Consumer<AdhAgent> saveCallback;
    private final Runnable closeCallback;

    private TextField idAdhField;
    private TextField codAgField;
    private TextField nomField;
    private TextField prenomField;
    private TextField nomArField;
    private TextField prenomArField;
    private TextField cinField;
    private Select<String> sexeSelect;
    private DatePicker naissanceField;
    private EmailField emailField;
    private TextField telephoneField;
    private TextField villeField;
    private TextField codePostalField;
    private TextField adresseField;
    private Select<String> situationFamilialeSelect;
    private Select<String> isAdhSelect;
    
    // File upload components
    private FileUploadComponent agentPhotoUpload;
    private FileUploadComponent cinImageUpload;
    private FileUploadComponent ribUpload;
    private FileUploadComponent ribPhotoUpload;

    public AgentFormDialog(AdhAgent agent, Consumer<AdhAgent> saveCallback, Runnable closeCallback) {
        this.agent = agent;
        this.saveCallback = saveCallback;
        this.closeCallback = closeCallback;

        setHeaderTitle(agent.getAdhAgentId() == 0 ? "Nouvel Agent" : "Modifier Agent");
        setWidth("900px");
        setHeight("700px");

        createTabbedForm();
        createButtons();
        bindFields();
        
        binder.readBean(agent);
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
        idAdhField = new TextField("ID Adhérent");
        idAdhField.setRequired(true);

        codAgField = new TextField("Code Agent");
        codAgField.setRequired(true);

        nomField = new TextField("Nom");
        nomField.setRequired(true);

        prenomField = new TextField("Prénom");
        prenomField.setRequired(true);

        nomArField = new TextField("الاسم العائلي (Nom en arabe)");
        nomArField.getStyle().set("direction", "rtl");

        prenomArField = new TextField("الاسم الشخصي (Prénom en arabe)");
        prenomArField.getStyle().set("direction", "rtl");

        cinField = new TextField("CIN");
        cinField.setRequired(true);

        sexeSelect = new Select<>();
        sexeSelect.setLabel("Sexe");
        sexeSelect.setItems("M", "F");

        naissanceField = new DatePicker("Date de naissance");

        emailField = new EmailField("Email");
        emailField.setRequired(true);

        telephoneField = new TextField("Téléphone");

        villeField = new TextField("Ville");

        codePostalField = new TextField("Code postal");

        adresseField = new TextField("Adresse");

        situationFamilialeSelect = new Select<>();
        situationFamilialeSelect.setLabel("Situation familiale");
        situationFamilialeSelect.setItems("Célibataire", "Marié(e)", "Divorcé(e)", "Veuf(ve)");

        isAdhSelect = new Select<>();
        isAdhSelect.setLabel("Est adhérent");
        isAdhSelect.setItems("OUI", "NON");
        isAdhSelect.setValue("OUI");

        // Add fields to form
        formLayout.add(
            idAdhField, codAgField,
            nomField, prenomField,
            nomArField, prenomArField,
            cinField, sexeSelect,
            naissanceField, emailField,
            telephoneField, villeField,
            codePostalField, adresseField,
            situationFamilialeSelect, isAdhSelect
        );

        layout.add(formLayout);
        return layout;
    }
    
    private VerticalLayout createDocumentsForm() {
        VerticalLayout layout = new VerticalLayout();
        
        // Create file upload components
        agentPhotoUpload = new FileUploadComponent(
            "Photo de l'agent", 
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
        
        ribUpload = new FileUploadComponent(
            "RIB (document)", 
            "application/pdf,image/jpeg,image/png", 
            10, 
            false
        );
        
        ribPhotoUpload = new FileUploadComponent(
            "Photo du RIB", 
            "image/jpeg,image/png,image/gif", 
            10, 
            true
        );
        
        layout.add(agentPhotoUpload, cinImageUpload, ribUpload, ribPhotoUpload);
        return layout;
    }
    
    private void loadExistingFiles() {
        if (agent.getAgent_photo() != null) {
            agentPhotoUpload.setExistingFile(agent.getAgent_photo(), agent.getAgent_photo_filename(), agent.getAgent_photo_contentType());
        }
        if (agent.getCin_image() != null) {
            cinImageUpload.setExistingFile(agent.getCin_image(), agent.getCin_image_filename(), agent.getCin_image_contentType());
        }
        if (agent.getRib() != null) {
            ribUpload.setExistingFile(agent.getRib(), agent.getRib_filename(), agent.getRib_contentType());
        }
        if (agent.getRib_photo() != null) {
            ribPhotoUpload.setExistingFile(agent.getRib_photo(), agent.getRib_photo_filename(), agent.getRib_photo_contentType());
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
        binder.forField(idAdhField)
            .asRequired("ID Adhérent requis")
            .bind(AdhAgent::getIdAdh, AdhAgent::setIdAdh);

        binder.forField(codAgField)
            .asRequired("Code Agent requis")
            .bind(AdhAgent::getCodAg, AdhAgent::setCodAg);

        binder.forField(nomField)
            .asRequired("Nom requis")
            .bind(AdhAgent::getNOM_AG, AdhAgent::setNOM_AG);

        binder.forField(prenomField)
            .asRequired("Prénom requis")
            .bind(AdhAgent::getPR_AG, AdhAgent::setPR_AG);

        binder.forField(nomArField)
            .bind(AdhAgent::getNOM_AG_AR, AdhAgent::setNOM_AG_AR);

        binder.forField(prenomArField)
            .bind(AdhAgent::getPR_AG_AR, AdhAgent::setPR_AG_AR);

        binder.forField(cinField)
            .asRequired("CIN requis")
            .bind(AdhAgent::getCIN_AG, AdhAgent::setCIN_AG);

        binder.forField(sexeSelect)
            .asRequired("Sexe requis")
            .bind(AdhAgent::getSex_AG, AdhAgent::setSex_AG);

        binder.forField(naissanceField)
            .bind(
                agent -> agent.getNaissance() != null ? 
                    agent.getNaissance().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null,
                (agent, localDate) -> agent.setNaissance(
                    localDate != null ? Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()) : null
                )
            );

        binder.forField(emailField)
            .asRequired("Email requis")
            .bind(AdhAgent::getMail, AdhAgent::setMail);

        binder.forField(telephoneField)
            .bind(AdhAgent::getNum_Tel, AdhAgent::setNum_Tel);

        binder.forField(villeField)
            .bind(AdhAgent::getVille, AdhAgent::setVille);

        binder.forField(codePostalField)
            .bind(AdhAgent::getCode_POSTE, AdhAgent::setCode_POSTE);

        binder.forField(adresseField)
            .bind(AdhAgent::getAdresse, AdhAgent::setAdresse);

        binder.forField(situationFamilialeSelect)
            .bind(AdhAgent::getSituation_familiale, AdhAgent::setSituation_familiale);

        binder.forField(isAdhSelect)
            .bind(AdhAgent::getIS_ADH, AdhAgent::setIS_ADH);
    }

    private void save() {
        try {
            agent.setUpdated(new Date());
            binder.writeBean(agent);
            
            // Save uploaded files
            saveUploadedFiles();
            
            saveCallback.accept(agent);
            close();
        } catch (ValidationException e) {
            // Validation errors are shown automatically
        }
    }
    
    private void saveUploadedFiles() {
        // Save agent photo
        FileUploadComponent.FileUploadData agentPhoto = agentPhotoUpload.getCurrentFile();
        if (agentPhoto != null) {
            agent.setAgent_photo(agentPhoto.getData());
            agent.setAgent_photo_filename(agentPhoto.getFileName());
            agent.setAgent_photo_contentType(agentPhoto.getContentType());
        }
        
        // Save CIN image
        FileUploadComponent.FileUploadData cinImage = cinImageUpload.getCurrentFile();
        if (cinImage != null) {
            agent.setCin_image(cinImage.getData());
            agent.setCin_image_filename(cinImage.getFileName());
            agent.setCin_image_contentType(cinImage.getContentType());
        }
        
        // Save RIB document
        FileUploadComponent.FileUploadData rib = ribUpload.getCurrentFile();
        if (rib != null) {
            agent.setRib(rib.getData());
            agent.setRib_filename(rib.getFileName());
            agent.setRib_contentType(rib.getContentType());
        }
        
        // Save RIB photo
        FileUploadComponent.FileUploadData ribPhoto = ribPhotoUpload.getCurrentFile();
        if (ribPhoto != null) {
            agent.setRib_photo(ribPhoto.getData());
            agent.setRib_photo_filename(ribPhoto.getFileName());
            agent.setRib_photo_contentType(ribPhoto.getContentType());
        }
    }
}