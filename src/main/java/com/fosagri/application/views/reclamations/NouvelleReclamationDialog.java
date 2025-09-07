package com.fosagri.application.views.reclamations;

import com.fosagri.application.entities.Reclamation;
import com.fosagri.application.entities.Reclamation.TypeReclamation;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.services.ReclamationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class NouvelleReclamationDialog extends Dialog {
    
    private final AdhAgentService agentService;
    private final ReclamationService reclamationService;
    private final List<SaveListener> saveListeners = new ArrayList<>();
    
    private ComboBox<AdhAgent> agentCombo;
    private TextField objetField;
    private TextArea detailArea;
    private ComboBox<TypeReclamation> typeCombo;
    private Upload fileUpload;
    private MemoryBuffer buffer = new MemoryBuffer();
    private Button submitButton;
    
    public NouvelleReclamationDialog(AdhAgentService agentService, ReclamationService reclamationService) {
        this.agentService = agentService;
        this.reclamationService = reclamationService;
        
        setHeaderTitle("Nouvelle Réclamation");
        setWidth("700px");
        setHeight("600px");
        setModal(true);
        setResizable(true);
        
        createContent();
    }
    
    private void createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        
        H3 title = new H3("Créer une nouvelle réclamation");
        
        // Agent selection
        agentCombo = new ComboBox<>("Agent");
        agentCombo.setItems(agentService.findAll());
        agentCombo.setItemLabelGenerator(agent -> 
            agent.getNOM_AG() + " " + agent.getPR_AG() + " (" + agent.getCIN_AG() + ")");
        agentCombo.setWidthFull();
        agentCombo.setRequired(true);
        
        // Object field
        objetField = new TextField("Objet de la réclamation");
        objetField.setWidthFull();
        objetField.setRequired(true);
        objetField.setMaxLength(200);
        
        // Type selection
        typeCombo = new ComboBox<>("Type de réclamation");
        typeCombo.setItems(TypeReclamation.values());
        typeCombo.setItemLabelGenerator(TypeReclamation::getLabel);
        typeCombo.setWidthFull();
        typeCombo.setRequired(true);
        
        // Detail text area
        detailArea = new TextArea("Détail de la réclamation");
        detailArea.setWidthFull();
        detailArea.setHeight("150px");
        detailArea.setPlaceholder("Décrivez en détail votre réclamation...");
        
        // File upload
        fileUpload = new Upload(buffer);
        fileUpload.setAcceptedFileTypes("application/pdf", "image/jpeg", "image/png", "image/gif", 
                                       "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        fileUpload.setMaxFileSize(5 * 1024 * 1024); // 5MB max
        fileUpload.setDropLabel(new com.vaadin.flow.component.html.Span("Glissez le fichier ici ou cliquez pour sélectionner"));
        fileUpload.setWidthFull();
        
        // Buttons
        submitButton = new Button("Soumettre la réclamation");
        submitButton.addClickListener(e -> saveReclamation());
        submitButton.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        
        Button cancelButton = new Button("Annuler");
        cancelButton.addClickListener(e -> close());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(submitButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        layout.add(title, agentCombo, objetField, typeCombo, detailArea, fileUpload, buttonLayout);
        add(layout);
    }
    
    private void saveReclamation() {
        // Validation
        if (agentCombo.getValue() == null) {
            Notification.show("Veuillez sélectionner un agent");
            return;
        }
        
        if (objetField.getValue() == null || objetField.getValue().trim().isEmpty()) {
            Notification.show("Veuillez saisir l'objet de la réclamation");
            return;
        }
        
        if (typeCombo.getValue() == null) {
            Notification.show("Veuillez sélectionner un type de réclamation");
            return;
        }
        
        try {
            MultipartFile file = null;
            
            // Handle file upload if present
            if (buffer.getInputStream() != null) {
                file = createMultipartFile();
            }
            
            // Create reclamation
            Reclamation reclamation = reclamationService.createReclamation(
                agentCombo.getValue(),
                objetField.getValue().trim(),
                detailArea.getValue() != null ? detailArea.getValue().trim() : "",
                typeCombo.getValue(),
                file
            );
            
            // Notify listeners
            saveListeners.forEach(listener -> listener.onSave(reclamation));
            
            close();
            
        } catch (Exception e) {
            Notification.show("Erreur lors de la création: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private MultipartFile createMultipartFile() throws IOException {
        if (buffer.getInputStream() == null) {
            return null;
        }
        
        InputStream inputStream = buffer.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > -1) {
            baos.write(buffer, 0, len);
        }
        baos.flush();
        
        // Create a simple MultipartFile implementation
        return new SimpleMultipartFile(
            "file",
            this.buffer.getFileName(),
            "application/octet-stream",
            baos.toByteArray()
        );
    }
    
    // Simple MultipartFile implementation
    private static class SimpleMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;
        
        public SimpleMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = content;
        }
        
        @Override
        public String getName() { return name; }
        
        @Override
        public String getOriginalFilename() { return originalFilename; }
        
        @Override
        public String getContentType() { return contentType; }
        
        @Override
        public boolean isEmpty() { return content == null || content.length == 0; }
        
        @Override
        public long getSize() { return content != null ? content.length : 0; }
        
        @Override
        public byte[] getBytes() { return content; }
        
        @Override
        public InputStream getInputStream() {
            return new java.io.ByteArrayInputStream(content);
        }
        
        @Override
        public void transferTo(java.io.File dest) throws IOException, IllegalStateException {
            throw new UnsupportedOperationException();
        }
    }
    
    // Listener interface
    public interface SaveListener {
        void onSave(Reclamation reclamation);
    }
    
    public void addSaveListener(SaveListener listener) {
        saveListeners.add(listener);
    }
}