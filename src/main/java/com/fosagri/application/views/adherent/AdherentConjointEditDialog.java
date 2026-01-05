package com.fosagri.application.views.adherent;

import com.fosagri.application.dto.DocumentUpload;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.service.AdhConjointService;
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

public class AdherentConjointEditDialog extends Dialog {

    private final AdhConjoint conjoint;
    private final AdhAgent agent;
    private final AdhConjointService conjointService;
    private final ModificationDemandeService modificationService;
    private final boolean isNew;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private final TextField nom = new TextField("Nom");
    private final TextField prenom = new TextField("Prénom");
    private final TextField nomAr = new TextField("الاسم العائلي (Nom en arabe)");
    private final TextField prenomAr = new TextField("الاسم الشخصي (Prénom en arabe)");
    private final TextField cin = new TextField("CIN");
    private final DatePicker dateNaissance = new DatePicker("Date de naissance");
    private final TextField telephone = new TextField("Téléphone");
    private final ComboBox<String> sexe = new ComboBox<>("Sexe");

    // Document fields - store uploaded data
    private byte[] newPhoto;
    private String newPhotoFilename;
    private String newPhotoContentType;

    private byte[] newCinImage;
    private String newCinFilename;
    private String newCinContentType;

    private byte[] newActeMariage;
    private String newActeMariageFilename;
    private String newActeMariageContentType;

    // UI containers for document previews
    private final Div photoPreviewContainer = new Div();
    private final Div cinPreviewContainer = new Div();
    private final Div acteMariagePreviewContainer = new Div();

    public AdherentConjointEditDialog(AdhConjoint conjoint, AdhAgent agent, AdhConjointService conjointService,
                                       ModificationDemandeService modificationService) {
        this.agent = agent;
        this.conjointService = conjointService;
        this.modificationService = modificationService;
        this.isNew = (conjoint == null);
        this.conjoint = conjoint != null ? conjoint : new AdhConjoint();

        setHeaderTitle(isNew ? "Ajouter un conjoint" : "Modifier le conjoint");
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
            nom.setValue(this.conjoint.getNOM_CONJ() != null ? this.conjoint.getNOM_CONJ() : "");
            prenom.setValue(this.conjoint.getPR_CONJ() != null ? this.conjoint.getPR_CONJ() : "");
            nomAr.setValue(this.conjoint.getNom_CONJ_A() != null ? this.conjoint.getNom_CONJ_A() : "");
            prenomAr.setValue(this.conjoint.getPr_CONJ_A() != null ? this.conjoint.getPr_CONJ_A() : "");
            cin.setValue(this.conjoint.getCIN_CONJ() != null ? this.conjoint.getCIN_CONJ() : "");
            telephone.setValue(this.conjoint.getTele() != null ? this.conjoint.getTele() : "");
            sexe.setValue(this.conjoint.getSex_CONJ());
            if (this.conjoint.getDat_N_CONJ() != null) {
                dateNaissance.setValue(this.conjoint.getDat_N_CONJ().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate());
            }
        }

        nom.setRequired(true);
        prenom.setRequired(true);

        form.add(nom, prenom, nomAr, prenomAr, cin, sexe, dateNaissance, telephone);

        layout.add(form);
        return layout;
    }

    private VerticalLayout createDocumentsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H4 title = new H4("Documents du conjoint");
        title.getStyle().set("margin", "0");

        Span hint = new Span("Téléchargez ou mettez à jour les documents (Photo, CIN, Acte de mariage)");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");

        // Photo section
        VerticalLayout photoSection = createDocumentSection(
            "Photo du conjoint",
            isNew ? null : conjoint.getConjoint_photo(),
            isNew ? null : conjoint.getConjoint_photo_filename(),
            isNew ? null : conjoint.getConjoint_photo_contentType(),
            photoPreviewContainer,
            (data, filename, contentType) -> {
                newPhoto = data;
                newPhotoFilename = filename;
                newPhotoContentType = contentType;
            }
        );

        // CIN section
        VerticalLayout cinSection = createDocumentSection(
            "Carte d'identité nationale (CIN)",
            isNew ? null : conjoint.getCin_image(),
            isNew ? null : conjoint.getCin_image_filename(),
            isNew ? null : conjoint.getCin_image_contentType(),
            cinPreviewContainer,
            (data, filename, contentType) -> {
                newCinImage = data;
                newCinFilename = filename;
                newCinContentType = contentType;
            }
        );

        // Acte de mariage section
        VerticalLayout acteMariageSection = createDocumentSection(
            "Acte de mariage",
            isNew ? null : conjoint.getActe_mariage_photo(),
            isNew ? null : conjoint.getActe_mariage_photo_filename(),
            isNew ? null : conjoint.getActe_mariage_photo_contentType(),
            acteMariagePreviewContainer,
            (data, filename, contentType) -> {
                newActeMariage = data;
                newActeMariageFilename = filename;
                newActeMariageContentType = contentType;
            }
        );

        layout.add(title, hint, photoSection, cinSection, acteMariageSection);
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
            newValues.put("NOM_CONJ", nom.getValue());
            newValues.put("PR_CONJ", prenom.getValue());
            newValues.put("nom_CONJ_A", nomAr.getValue());
            newValues.put("pr_CONJ_A", prenomAr.getValue());
            newValues.put("CIN_CONJ", cin.getValue());
            newValues.put("tele", telephone.getValue());
            newValues.put("sex_CONJ", sexe.getValue());

            if (dateNaissance.getValue() != null) {
                Date date = Date.from(dateNaissance.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant());
                newValues.put("dat_N_CONJ", dateFormat.format(date));
            }

            // Collect documents
            List<DocumentUpload> documents = new ArrayList<>();

            if (newPhoto != null) {
                DocumentUpload doc = new DocumentUpload();
                doc.setFieldName("conjoint_photo");
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

            if (newActeMariage != null) {
                DocumentUpload doc = new DocumentUpload();
                doc.setFieldName("acte_mariage_photo");
                doc.setFilename(newActeMariageFilename);
                doc.setContentType(newActeMariageContentType);
                doc.setData(newActeMariage);
                doc.setSize(newActeMariage.length);
                documents.add(doc);
            }

            // Create modification request instead of saving directly
            if (isNew) {
                modificationService.createConjointCreationRequest(agent, newValues, documents);
                Notification.show("Demande d'ajout de conjoint soumise. En attente de validation par l'admin.",
                    4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                modificationService.createConjointModificationRequest(agent, conjoint, newValues, documents);
                Notification.show("Demande de modification soumise. En attente de validation par l'admin.",
                    4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            fireEvent(new SaveEvent(this, conjoint));
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

    public static class SaveEvent extends ComponentEvent<AdherentConjointEditDialog> {
        private final AdhConjoint conjoint;

        public SaveEvent(AdherentConjointEditDialog source, AdhConjoint conjoint) {
            super(source, false);
            this.conjoint = conjoint;
        }

        public AdhConjoint getConjoint() {
            return conjoint;
        }
    }
}
