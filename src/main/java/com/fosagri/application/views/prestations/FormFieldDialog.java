package com.fosagri.application.views.prestations;

import com.fosagri.application.forms.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FormFieldDialog extends Dialog {
    
    private final List<SaveListener> saveListeners = new ArrayList<>();
    private final Binder<FieldFormModel> binder = new Binder<>(FieldFormModel.class);
    
    private TextField name;
    private TextField label;
    private Select<String> type;
    private TextField placeholder;
    private Select<String> required;
    private TextField options;
    private TextField condition;
    private IntegerField order;
    private IntegerField maxFiles;
    private TextField acceptedFileTypes;
    
    public FormFieldDialog(FormField existing) {
        setHeaderTitle(existing == null ? "Nouveau champ" : "Modifier le champ");
        setWidth("600px");
        setModal(true);
        
        FieldFormModel model = existing == null ? new FieldFormModel() : FieldFormModel.from(existing);
        
        createForm();
        createButtons();
        
        binder.readBean(model);
    }
    
    private void createForm() {
        FormLayout formLayout = new FormLayout();
        
        name = new TextField("Nom du champ");
        name.setRequired(true);
        
        label = new TextField("Label");
        label.setRequired(true);
        
        type = new Select<>();
        type.setLabel("Type de champ");
        type.setItems("text", "textarea", "number", "date", "select", "checkbox", "file", "enfant", "conjoint");
        type.setRequiredIndicatorVisible(true);
        
        placeholder = new TextField("Placeholder");
        
        required = new Select<>();
        required.setLabel("Requis");
        required.setItems("true", "false");
        required.setValue("false");
        
        options = new TextField("Options (value:label;...)");
        options.setHelperText("Pour les champs de type 'select'. Format: valeur1:label1;valeur2:label2");
        
        condition = new TextField("Condition (field,op,value)");
        condition.setHelperText("Condition d'affichage. Format: nom_champ,eq,valeur");
        
        order = new IntegerField("Ordre d'affichage");
        order.setValue(1);
        
        maxFiles = new IntegerField("Nombre max de fichiers");
        maxFiles.setHelperText("Pour les champs de type 'file'. Nombre maximum de fichiers à télécharger");
        maxFiles.setValue(3);
        maxFiles.setMin(1);
        maxFiles.setMax(10);
        
        acceptedFileTypes = new TextField("Types de fichiers acceptés");
        acceptedFileTypes.setHelperText("Pour les champs de type 'file'. Ex: .pdf,.doc,.docx,.jpg,.png");
        acceptedFileTypes.setValue(".pdf,.doc,.docx,.jpg,.jpeg,.png");
        
        // Show/hide file-specific fields based on type selection
        type.addValueChangeListener(e -> {
            boolean isFileType = "file".equals(e.getValue());
            boolean isSelectType = "select".equals(e.getValue());
            maxFiles.setVisible(isFileType);
            acceptedFileTypes.setVisible(isFileType);
            options.setVisible(isSelectType);
        });
        
        // Initialize visibility after binding
        binder.addStatusChangeListener(e -> {
            String currentType = type.getValue();
            boolean isFileType = "file".equals(currentType);
            boolean isSelectType = "select".equals(currentType);
            maxFiles.setVisible(isFileType);
            acceptedFileTypes.setVisible(isFileType);
            options.setVisible(isSelectType);
        });
        
        // Binding
        binder.bind(name, FieldFormModel::getName, FieldFormModel::setName);
        binder.bind(label, FieldFormModel::getLabel, FieldFormModel::setLabel);
        binder.bind(type, FieldFormModel::getType, FieldFormModel::setType);
        binder.bind(placeholder, FieldFormModel::getPlaceholder, FieldFormModel::setPlaceholder);
        binder.bind(required, FieldFormModel::getRequired, FieldFormModel::setRequired);
        binder.bind(options, FieldFormModel::getOptions, FieldFormModel::setOptions);
        binder.bind(condition, FieldFormModel::getCond, FieldFormModel::setCond);
        binder.bind(order, FieldFormModel::getOrder, FieldFormModel::setOrder);
        binder.bind(maxFiles, FieldFormModel::getMaxFiles, FieldFormModel::setMaxFiles);
        binder.bind(acceptedFileTypes, FieldFormModel::getAcceptedFileTypes, FieldFormModel::setAcceptedFileTypes);
        
        formLayout.add(name, label, type, placeholder, required, options, condition, order, maxFiles, acceptedFileTypes);
        add(formLayout);
    }
    
    private void createButtons() {
        Button saveButton = new Button("Enregistrer", e -> save());
        saveButton.getStyle().set("background-color", "var(--lumo-primary-color)");
        saveButton.getStyle().set("color", "white");
        
        Button cancelButton = new Button("Annuler", e -> close());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        add(buttonLayout);
    }
    
    private void save() {
        try {
            FieldFormModel model = new FieldFormModel();
            binder.writeBean(model);
            
            FormField formField = model.toFormField();
            saveListeners.forEach(listener -> listener.onSave(formField));
            close();
            
        } catch (ValidationException e) {
            Notification.show("Veuillez corriger les erreurs dans le formulaire");
        } catch (Exception e) {
            Notification.show("Erreur: " + e.getMessage());
        }
    }
    
    public void addSaveListener(SaveListener listener) {
        saveListeners.add(listener);
    }
    
    @FunctionalInterface
    public interface SaveListener {
        void onSave(FormField formField);
    }
    
    // Helper DTO pour le binding
    public static class FieldFormModel {
        private String name;
        private String label;
        private String type;
        private String placeholder;
        private String required;
        private String options;
        private String cond;
        private Integer order;
        private Integer maxFiles;
        private String acceptedFileTypes;
        
        public static FieldFormModel from(FormField ff) {
            FieldFormModel m = new FieldFormModel();
            m.name = ff.getName();
            m.label = ff.getLabel();
            m.type = ff.getType();
            m.placeholder = ff.getPlaceholder();
            m.required = String.valueOf(Boolean.TRUE.equals(ff.getRequired()));
            if (ff.getOptions() != null) {
                String s = ff.getOptions().stream()
                    .map(o -> o.getValue() + ":" + o.getLabel())
                    .reduce((a, b) -> a + ";" + b)
                    .orElse("");
                m.options = s;
            }
            if (ff.getCondition() != null) {
                Condition c = ff.getCondition();
                m.cond = String.join(",", 
                    Objects.toString(c.getField(), ""), 
                    Objects.toString(c.getOperator(), ""), 
                    Objects.toString(c.getValue(), ""));
            }
            m.order = ff.getOrder();
            m.maxFiles = ff.getMaxFiles();
            m.acceptedFileTypes = ff.getAcceptedFileTypes();
            return m;
        }
        
        public FormField toFormField() {
            FormField f = new FormField();
            f.setName(name);
            f.setLabel(label);
            f.setType(type);
            f.setPlaceholder(placeholder);
            f.setRequired("true".equalsIgnoreCase(required));
            
            if (options != null && !options.isBlank()) {
                List<FieldOption> opts = new ArrayList<>();
                for (String pair : options.split(";")) {
                    String[] kv = pair.split(":", 2);
                    if (kv.length == 2) {
                        opts.add(new FieldOption(kv[0].trim(), kv[1].trim()));
                    }
                }
                f.setOptions(opts);
            }
            
            if (cond != null && !cond.isBlank()) {
                String[] parts = cond.split(",", 3);
                Condition c = new Condition();
                c.setField(parts.length > 0 ? parts[0].trim() : null);
                c.setOperator(parts.length > 1 ? parts[1].trim() : null);
                c.setValue(parts.length > 2 ? parts[2].trim() : null);
                f.setCondition(c);
            }
            
            f.setOrder(order);
            f.setMaxFiles(maxFiles);
            f.setAcceptedFileTypes(acceptedFileTypes);
            return f;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getLabel() { return label; }
        public void setLabel(String label) { this.label = label; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getPlaceholder() { return placeholder; }
        public void setPlaceholder(String placeholder) { this.placeholder = placeholder; }
        
        public String getRequired() { return required; }
        public void setRequired(String required) { this.required = required; }
        
        public String getOptions() { return options; }
        public void setOptions(String options) { this.options = options; }
        
        public String getCond() { return cond; }
        public void setCond(String cond) { this.cond = cond; }
        
        public Integer getOrder() { return order; }
        public void setOrder(Integer order) { this.order = order; }
        
        public Integer getMaxFiles() { return maxFiles; }
        public void setMaxFiles(Integer maxFiles) { this.maxFiles = maxFiles; }
        
        public String getAcceptedFileTypes() { return acceptedFileTypes; }
        public void setAcceptedFileTypes(String acceptedFileTypes) { this.acceptedFileTypes = acceptedFileTypes; }
    }
}