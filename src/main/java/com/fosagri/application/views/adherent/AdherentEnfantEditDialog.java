package com.fosagri.application.views.adherent;

import com.fosagri.application.dto.DocumentUpload;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.model.AdhEnfant;
import com.fosagri.application.service.AdhEnfantService;
import com.fosagri.application.services.ModificationDemandeService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

public class AdherentEnfantEditDialog extends Dialog {

    private final AdhEnfant enfant;
    private final AdhAgent agent;
    private final AdhEnfantService enfantService;
    private final ModificationDemandeService modificationService;
    private final boolean isNew;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private final TextField nom = new TextField("Nom");
    private final TextField prenom = new TextField("Prénom");
    private final TextField nomAr = new TextField("الاسم العائلي (Nom en arabe)");
    private final TextField prenomAr = new TextField("الاسم الشخصي (Prénom en arabe)");
    private final DatePicker dateNaissance = new DatePicker("Date de naissance");
    private final ComboBox<String> sexe = new ComboBox<>("Sexe");
    private final TextField niveauInstruction = new TextField("Niveau d'instruction");

    // Document fields - store uploaded data
    private byte[] newPhoto;
    private String newPhotoFilename;
    private String newPhotoContentType;

    private byte[] newCinImage;
    private String newCinFilename;
    private String newCinContentType;

    private byte[] newAttestationScolarite;
    private String newAttestationScolariteFilename;
    private String newAttestationScolariteContentType;

    // UI containers for document previews
    private final Div photoPreviewContainer = new Div();
    private final Div cinPreviewContainer = new Div();
    private final Div attestationPreviewContainer = new Div();

    public AdherentEnfantEditDialog(AdhEnfant enfant, AdhAgent agent, AdhEnfantService enfantService,
                                     ModificationDemandeService modificationService) {
        this.agent = agent;
        this.enfantService = enfantService;
        this.modificationService = modificationService;
        this.isNew = (enfant == null);
        this.enfant = enfant != null ? enfant : new AdhEnfant();

        setHeaderTitle(isNew ? "Ajouter un enfant" : "Modifier l'enfant");
        setWidth("700px");
        setHeight("600px");

        TabSheet tabSheet = new TabSheet();
        tabSheet.setWidthFull();

        // Tab 1: Informations
        tabSheet.add(new Tab("Informations"), createInfoTab());

        // Tab 2: Documents
        tabSheet.add(new Tab("Documents"), createDocumentsTab());

        add(tabSheet);

        // Buttons
        Button saveBtn = new Button("Enregistrer", e -> save());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelBtn = new Button("Annuler", e -> close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        getFooter().add(new HorizontalLayout(saveBtn, cancelBtn));
    }

    private VerticalLayout createInfoTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        sexe.setItems("M", "F");
        sexe.setItemLabelGenerator(s -> "M".equals(s) ? "Masculin" : "Féminin");

        // Set Arabic fields direction
        nomAr.getStyle().set("direction", "rtl");
        prenomAr.getStyle().set("direction", "rtl");

        // Set values if editing
        if (!isNew) {
            nom.setValue(this.enfant.getNom_pac() != null ? this.enfant.getNom_pac() : "");
            prenom.setValue(this.enfant.getPr_pac() != null ? this.enfant.getPr_pac() : "");
            nomAr.setValue(this.enfant.getNom_PAC_A() != null ? this.enfant.getNom_PAC_A() : "");
            prenomAr.setValue(this.enfant.getPr_PAC_A() != null ? this.enfant.getPr_PAC_A() : "");
            sexe.setValue(this.enfant.getSex_pac());
            niveauInstruction.setValue(this.enfant.getNiv_INSTRUCTION() != null ? this.enfant.getNiv_INSTRUCTION() : "");
            if (this.enfant.getDat_n_pac() != null) {
                dateNaissance.setValue(this.enfant.getDat_n_pac().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());
            }
        }

        nom.setRequired(true);
        prenom.setRequired(true);

        form.add(nom, prenom, nomAr, prenomAr, sexe, dateNaissance, niveauInstruction);

        layout.add(form);
        return layout;
    }

    private VerticalLayout createDocumentsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H4 title = new H4("Documents de l'enfant");
        title.getStyle().set("margin", "0");

        Span hint = new Span("Téléchargez ou mettez à jour les documents (Photo, CIN pour +18 ans, Attestation scolarité)");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");

        // Photo section
        VerticalLayout photoSection = createDocumentSection(
            "Photo de l'enfant",
            isNew ? null : enfant.getEnfant_photo(),
            isNew ? null : enfant.getEnfant_photo_filename(),
            isNew ? null : enfant.getEnfant_photo_contentType(),
            photoPreviewContainer,
            (data, filename, contentType) -> {
                newPhoto = data;
                newPhotoFilename = filename;
                newPhotoContentType = contentType;
            }
        );

        // CIN section (for children over 18)
        VerticalLayout cinSection = createDocumentSection(
            "Carte d'identité nationale (CIN) - pour +18 ans",
            isNew ? null : enfant.getCin_image(),
            isNew ? null : enfant.getCin_image_filename(),
            isNew ? null : enfant.getCin_image_contentType(),
            cinPreviewContainer,
            (data, filename, contentType) -> {
                newCinImage = data;
                newCinFilename = filename;
                newCinContentType = contentType;
            }
        );

        // Attestation de scolarité section
        VerticalLayout attestationSection = createDocumentSection(
            "Attestation de scolarité",
            isNew ? null : enfant.getAttestation_scolarite_photo(),
            isNew ? null : enfant.getAttestation_scolarite_photo_filename(),
            isNew ? null : enfant.getAttestation_scolarite_photo_contentType(),
            attestationPreviewContainer,
            (data, filename, contentType) -> {
                newAttestationScolarite = data;
                newAttestationScolariteFilename = filename;
                newAttestationScolariteContentType = contentType;
            }
        );

        layout.add(title, hint, photoSection, cinSection, attestationSection);
        return layout;
    }

    private VerticalLayout createDocumentSection(String label, byte[] existingData, String existingFilename,
            String existingContentType, Div previewContainer, DocumentUploadHandler handler) {

        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(false);
        section.getStyle()
            .set("border", "1px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "8px")
            .set("padding", "1rem")
            .set("margin-bottom", "0.5rem");

        Span sectionLabel = new Span(label);
        sectionLabel.getStyle().set("font-weight", "500").set("margin-bottom", "0.5rem");

        // Preview container
        previewContainer.getStyle().set("margin-bottom", "0.5rem");
        updatePreview(previewContainer, existingData, existingFilename, existingContentType);

        // Upload component
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("application/pdf", "image/jpeg", "image/png", ".pdf", ".jpg", ".jpeg", ".png");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024); // 5MB
        upload.setDropLabel(new Span("Glissez un fichier ou cliquez"));
        upload.setWidthFull();

        upload.addSucceededListener(event -> {
            try {
                InputStream inputStream = buffer.getInputStream();
                byte[] data = inputStream.readAllBytes();
                handler.onUpload(data, event.getFileName(), event.getMIMEType());
                updatePreview(previewContainer, data, event.getFileName(), event.getMIMEType());
                Notification.show("Document téléchargé: " + event.getFileName(), 2000, Notification.Position.TOP_END);
            } catch (IOException e) {
                Notification.show("Erreur de lecture du fichier", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        section.add(sectionLabel, previewContainer, upload);
        return section;
    }

    private void updatePreview(Div container, byte[] data, String filename, String contentType) {
        container.removeAll();

        if (data != null && data.length > 0 && filename != null) {
            HorizontalLayout row = new HorizontalLayout();
            row.setAlignItems(FlexComponent.Alignment.CENTER);
            row.setWidthFull();
            row.getStyle().set("background", "var(--lumo-contrast-5pct)")
                .set("padding", "0.5rem")
                .set("border-radius", "4px");

            // Show image preview if it's an image
            if (contentType != null && contentType.startsWith("image/")) {
                StreamResource resource = new StreamResource(filename, () -> new ByteArrayInputStream(data));
                Image preview = new Image(resource, filename);
                preview.setHeight("60px");
                preview.getStyle().set("border-radius", "4px").set("object-fit", "cover");
                row.add(preview);
            } else {
                Span icon = new Span(VaadinIcon.FILE.create());
                row.add(icon);
            }

            Span filenameSpan = new Span(filename);
            filenameSpan.getStyle().set("flex", "1").set("margin-left", "0.5rem");

            Span sizeSpan = new Span(formatFileSize(data.length));
            sizeSpan.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");

            row.add(filenameSpan, sizeSpan);
            container.add(row);
        } else {
            Span empty = new Span("Aucun document");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-style", "italic");
            container.add(empty);
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private void save() {
        if (nom.isEmpty() || prenom.isEmpty()) {
            Notification.show("Veuillez remplir les champs obligatoires", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        try {
            // Collect new values from form
            Map<String, Object> newValues = new LinkedHashMap<>();
            newValues.put("nom_pac", nom.getValue());
            newValues.put("pr_pac", prenom.getValue());
            newValues.put("nom_PAC_A", nomAr.getValue());
            newValues.put("pr_PAC_A", prenomAr.getValue());
            newValues.put("sex_pac", sexe.getValue());
            newValues.put("niv_INSTRUCTION", niveauInstruction.getValue());

            if (dateNaissance.getValue() != null) {
                Date date = Date.from(dateNaissance.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                newValues.put("dat_n_pac", dateFormat.format(date));
            }

            // Collect documents
            List<DocumentUpload> documents = new ArrayList<>();

            if (newPhoto != null) {
                DocumentUpload doc = new DocumentUpload();
                doc.setFieldName("enfant_photo");
                doc.setFilename(newPhotoFilename);
                doc.setContentType(newPhotoContentType);
                doc.setData(newPhoto);
                doc.setSize(newPhoto.length);
                documents.add(doc);
            }

            if (newCinImage != null) {
                DocumentUpload doc = new DocumentUpload();
                doc.setFieldName("cin_image");
                doc.setFilename(newCinFilename);
                doc.setContentType(newCinContentType);
                doc.setData(newCinImage);
                doc.setSize(newCinImage.length);
                documents.add(doc);
            }

            if (newAttestationScolarite != null) {
                DocumentUpload doc = new DocumentUpload();
                doc.setFieldName("attestation_scolarite_photo");
                doc.setFilename(newAttestationScolariteFilename);
                doc.setContentType(newAttestationScolariteContentType);
                doc.setData(newAttestationScolarite);
                doc.setSize(newAttestationScolarite.length);
                documents.add(doc);
            }

            // Create modification request instead of saving directly
            if (isNew) {
                modificationService.createEnfantCreationRequest(agent, newValues, documents);
                Notification.show("Demande d'ajout d'enfant soumise. En attente de validation par l'admin.",
                    4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                modificationService.createEnfantModificationRequest(agent, enfant, newValues, documents);
                Notification.show("Demande de modification soumise. En attente de validation par l'admin.",
                    4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            fireEvent(new SaveEvent(this, enfant));
            close();

        } catch (RuntimeException ex) {
            Notification.show("Erreur: " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public Registration addSaveListener(ComponentEventListener<SaveEvent> listener) {
        return addListener(SaveEvent.class, listener);
    }

    @FunctionalInterface
    private interface DocumentUploadHandler {
        void onUpload(byte[] data, String filename, String contentType);
    }

    public static class SaveEvent extends ComponentEvent<AdherentEnfantEditDialog> {
        private final AdhEnfant enfant;

        public SaveEvent(AdherentEnfantEditDialog source, AdhEnfant enfant) {
            super(source, false);
            this.enfant = enfant;
        }

        public AdhEnfant getEnfant() {
            return enfant;
        }
    }
}
