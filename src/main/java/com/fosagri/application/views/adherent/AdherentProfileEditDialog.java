package com.fosagri.application.views.adherent;

import com.fosagri.application.dto.DocumentUpload;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.services.ModificationDemandeService;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class AdherentProfileEditDialog extends Dialog {

    private final AdhAgent agent;
    private final AdhAgentService agentService;
    private final ModificationDemandeService modificationService;

    private final TextField telephone = new TextField("Téléphone");
    private final EmailField email = new EmailField("Email");
    private final TextField adresse = new TextField("Adresse");
    private final TextField ville = new TextField("Ville");
    private final TextField nomAr = new TextField("الاسم العائلي (Nom en arabe)");
    private final TextField prenomAr = new TextField("الاسم الشخصي (Prénom en arabe)");

    // Document fields - store uploaded data
    private byte[] newPhoto;
    private String newPhotoFilename;
    private String newPhotoContentType;

    private byte[] newCinImage;
    private String newCinFilename;
    private String newCinContentType;

    private byte[] newRib;
    private String newRibFilename;
    private String newRibContentType;

    // UI containers for document previews
    private final Div photoPreviewContainer = new Div();
    private final Div cinPreviewContainer = new Div();
    private final Div ribPreviewContainer = new Div();

    public AdherentProfileEditDialog(AdhAgent agent, AdhAgentService agentService,
                                      ModificationDemandeService modificationService) {
        this.agent = agent;
        this.agentService = agentService;
        this.modificationService = modificationService;

        setHeaderTitle("Modifier mon profil");
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

        // Read-only fields
        TextField nom = new TextField("Nom");
        nom.setValue(agent.getNOM_AG() != null ? agent.getNOM_AG() : "");
        nom.setReadOnly(true);

        TextField prenom = new TextField("Prénom");
        prenom.setValue(agent.getPR_AG() != null ? agent.getPR_AG() : "");
        prenom.setReadOnly(true);

        TextField cin = new TextField("CIN");
        cin.setValue(agent.getCIN_AG() != null ? agent.getCIN_AG() : "");
        cin.setReadOnly(true);

        // Editable fields
        telephone.setValue(agent.getNum_Tel() != null ? agent.getNum_Tel() : "");
        email.setValue(agent.getMail() != null ? agent.getMail() : "");
        adresse.setValue(agent.getAdresse() != null ? agent.getAdresse() : "");
        ville.setValue(agent.getVille() != null ? agent.getVille() : "");

        // Arabic name fields
        nomAr.setValue(agent.getNOM_AG_AR() != null ? agent.getNOM_AG_AR() : "");
        nomAr.getStyle().set("direction", "rtl");
        prenomAr.setValue(agent.getPR_AG_AR() != null ? agent.getPR_AG_AR() : "");
        prenomAr.getStyle().set("direction", "rtl");

        form.add(nom, prenom, nomAr, prenomAr, cin, telephone, email, ville, adresse);
        form.setColspan(adresse, 2);

        layout.add(form);
        return layout;
    }

    private VerticalLayout createDocumentsTab() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);

        H4 title = new H4("Mes documents justificatifs");
        title.getStyle().set("margin", "0");

        Span hint = new Span("Téléchargez ou mettez à jour vos documents (Photo, CIN, RIB)");
        hint.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.85rem");

        // Photo section
        VerticalLayout photoSection = createDocumentSection(
            "Photo de profil",
            agent.getAgent_photo(),
            agent.getAgent_photo_filename(),
            agent.getAgent_photo_contentType(),
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
            agent.getCin_image(),
            agent.getCin_image_filename(),
            agent.getCin_image_contentType(),
            cinPreviewContainer,
            (data, filename, contentType) -> {
                newCinImage = data;
                newCinFilename = filename;
                newCinContentType = contentType;
            }
        );

        // RIB section
        VerticalLayout ribSection = createDocumentSection(
            "Relevé d'identité bancaire (RIB)",
            agent.getRib(),
            agent.getRib_filename(),
            agent.getRib_contentType(),
            ribPreviewContainer,
            (data, filename, contentType) -> {
                newRib = data;
                newRibFilename = filename;
                newRibContentType = contentType;
            }
        );

        layout.add(title, hint, photoSection, cinSection, ribSection);
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
        try {
            // Collect new values from form
            Map<String, Object> newValues = new LinkedHashMap<>();
            newValues.put("num_Tel", telephone.getValue());
            newValues.put("mail", email.getValue());
            newValues.put("adresse", adresse.getValue());
            newValues.put("ville", ville.getValue());
            newValues.put("NOM_AG_AR", nomAr.getValue());
            newValues.put("PR_AG_AR", prenomAr.getValue());

            // Collect documents
            List<DocumentUpload> documents = new ArrayList<>();

            if (newPhoto != null) {
                DocumentUpload doc = new DocumentUpload();
                doc.setFieldName("agent_photo");
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

            if (newRib != null) {
                DocumentUpload doc = new DocumentUpload();
                doc.setFieldName("rib");
                doc.setFilename(newRibFilename);
                doc.setContentType(newRibContentType);
                doc.setData(newRib);
                doc.setSize(newRib.length);
                documents.add(doc);
            }

            // Create modification request instead of saving directly
            modificationService.createAgentModificationRequest(agent, newValues, documents);

            Notification.show("Demande de modification soumise. En attente de validation par l'admin.",
                4000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            fireEvent(new SaveEvent(this, agent));
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

    public static class SaveEvent extends ComponentEvent<AdherentProfileEditDialog> {
        private final AdhAgent agent;

        public SaveEvent(AdherentProfileEditDialog source, AdhAgent agent) {
            super(source, false);
            this.agent = agent;
        }

        public AdhAgent getAgent() {
            return agent;
        }
    }
}
