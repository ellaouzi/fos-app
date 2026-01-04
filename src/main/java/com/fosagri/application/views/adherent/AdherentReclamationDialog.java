package com.fosagri.application.views.adherent;

import com.fosagri.application.entities.Reclamation;
import com.fosagri.application.entities.Reclamation.TypeReclamation;
import com.fosagri.application.entities.Reclamation.PrioriteReclamation;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.services.ReclamationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

import java.util.function.Consumer;

public class AdherentReclamationDialog extends Dialog {

    private final AdhAgent agent;
    private final ReclamationService reclamationService;

    private final TextField objetField = new TextField("Objet de la réclamation");
    private final TextArea detailField = new TextArea("Description détaillée");
    private final ComboBox<TypeReclamation> typeCombo = new ComboBox<>("Type de réclamation");
    private final ComboBox<PrioriteReclamation> prioriteCombo = new ComboBox<>("Priorité");

    private final MemoryBuffer buffer = new MemoryBuffer();
    private final Upload upload = new Upload(buffer);

    private Consumer<Reclamation> saveListener;

    public AdherentReclamationDialog(AdhAgent agent, ReclamationService reclamationService) {
        this.agent = agent;
        this.reclamationService = reclamationService;

        setHeaderTitle("Nouvelle Réclamation");
        setWidth("600px");
        setCloseOnOutsideClick(false);

        createForm();
        createButtons();
    }

    private void createForm() {
        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        H3 title = new H3("Soumettre une réclamation");
        title.getStyle().set("margin-top", "0");

        objetField.setWidthFull();
        objetField.setRequired(true);
        objetField.setMaxLength(200);
        objetField.setPlaceholder("Résumez votre réclamation en quelques mots");

        detailField.setWidthFull();
        detailField.setMinHeight("150px");
        detailField.setPlaceholder("Décrivez en détail votre réclamation...");

        typeCombo.setItems(TypeReclamation.values());
        typeCombo.setItemLabelGenerator(TypeReclamation::getLabel);
        typeCombo.setWidthFull();
        typeCombo.setRequired(true);
        typeCombo.setValue(TypeReclamation.AUTRE);

        prioriteCombo.setItems(PrioriteReclamation.values());
        prioriteCombo.setItemLabelGenerator(PrioriteReclamation::getLabel);
        prioriteCombo.setWidthFull();
        prioriteCombo.setValue(PrioriteReclamation.NORMALE);

        upload.setAcceptedFileTypes("application/pdf", "image/*", ".doc", ".docx");
        upload.setMaxFileSize(5 * 1024 * 1024); // 5MB
        upload.setDropAllowed(true);

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        formLayout.add(objetField, 2);
        formLayout.add(typeCombo, prioriteCombo);
        formLayout.add(detailField, 2);

        VerticalLayout uploadSection = new VerticalLayout();
        uploadSection.setPadding(false);
        uploadSection.setSpacing(false);
        uploadSection.add(new com.vaadin.flow.component.html.Span("Pièce jointe (optionnel)"));
        uploadSection.add(upload);

        content.add(title, formLayout, uploadSection);
        add(content);
    }

    private void createButtons() {
        Button cancelBtn = new Button("Annuler", e -> close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button submitBtn = new Button("Soumettre", e -> submitReclamation());
        submitBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttons = new HorizontalLayout(cancelBtn, submitBtn);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();

        getFooter().add(buttons);
    }

    private void submitReclamation() {
        if (!validateForm()) {
            return;
        }

        try {
            Reclamation reclamation = new Reclamation();
            reclamation.setAgent(agent);
            reclamation.setObjet(objetField.getValue().trim());
            reclamation.setDetail(detailField.getValue().trim());
            reclamation.setType(typeCombo.getValue());
            reclamation.setPriorite(prioriteCombo.getValue());

            // Handle file upload if present
            if (buffer.getFileName() != null && !buffer.getFileName().isEmpty()) {
                try {
                    java.io.InputStream inputStream = buffer.getInputStream();
                    byte[] fileContent = inputStream.readAllBytes();

                    java.util.Map<String, Object> fileData = new java.util.HashMap<>();
                    fileData.put("filename", buffer.getFileName());
                    fileData.put("contentType", "application/octet-stream");
                    fileData.put("size", fileContent.length);
                    fileData.put("content", java.util.Base64.getEncoder().encodeToString(fileContent));
                    fileData.put("uploadDate", new java.util.Date());

                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    reclamation.setFichierAttache(mapper.writeValueAsString(fileData));
                } catch (Exception ex) {
                    // Continue without file if there's an error
                }
            }

            Reclamation savedReclamation = reclamationService.save(reclamation);

            if (saveListener != null) {
                saveListener.accept(savedReclamation);
            }

            close();
        } catch (Exception e) {
            Notification notification = Notification.show(
                "Erreur lors de la soumission: " + e.getMessage(),
                5000, Notification.Position.TOP_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private boolean validateForm() {
        if (objetField.isEmpty()) {
            Notification.show("Veuillez saisir l'objet de la réclamation", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            objetField.focus();
            return false;
        }

        if (typeCombo.isEmpty()) {
            Notification.show("Veuillez sélectionner le type de réclamation", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
            typeCombo.focus();
            return false;
        }

        return true;
    }

    public void addSaveListener(Consumer<Reclamation> listener) {
        this.saveListener = listener;
    }
}
