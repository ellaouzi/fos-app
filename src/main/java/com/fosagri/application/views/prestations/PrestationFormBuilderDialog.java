package com.fosagri.application.views.prestations;

import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.forms.*;
import com.fosagri.application.services.PrestationRefService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;

import java.util.ArrayList;
import java.util.List;

public class PrestationFormBuilderDialog extends Dialog {
    
    private final PrestationRef prestationRef;
    private final PrestationRefService service;
    private final List<SaveListener> saveListeners = new ArrayList<>();
    
    private final FormSchema schema = new FormSchema();
    private final Grid<FormField> grid = new Grid<>(FormField.class, false);
    private final ListDataProvider<FormField> dataProvider = new ListDataProvider<>(new ArrayList<>());
    private final TextArea schemaJson = new TextArea("Schema JSON");
    private final Div previewContainer = new Div();
    
    public PrestationFormBuilderDialog(PrestationRef prestationRef, PrestationRefService service) {
        this.prestationRef = prestationRef;
        this.service = service;
        
        setHeaderTitle("Formulaire - " + prestationRef.getLabel());
        setWidth("1400px");
        setHeight("800px");
        setModal(true);
        setResizable(true);
        
        initializeSchema();
        createContent();
        refreshAll();
    }
    
    private void initializeSchema() {
        schema.setKey("prestation_" + prestationRef.getId());
        schema.setTitle("Demande de " + prestationRef.getLabel());
        
        // Charger le schéma existant si disponible
        if (prestationRef.getFormSchemaJson() != null && !prestationRef.getFormSchemaJson().trim().isEmpty()) {
            try {
                FormSchema existingSchema = FormSchema.fromJson(prestationRef.getFormSchemaJson());
                schema.setKey(existingSchema.getKey());
                schema.setTitle(existingSchema.getTitle());
                schema.setFields(existingSchema.getFields());
                dataProvider.getItems().addAll(existingSchema.getFields());
            } catch (Exception e) {
                Notification.show("Erreur lors du chargement du schéma existant: " + e.getMessage());
            }
        }
    }
    
    private void createContent() {
        SplitLayout mainSplit = new SplitLayout();
        mainSplit.setSizeFull();
        
        // Panneau gauche - Form Builder
        VerticalLayout leftPanel = createFormBuilderPanel();
        
        // Panneau droit - Preview
        VerticalLayout rightPanel = createPreviewPanel();
        
        mainSplit.addToPrimary(leftPanel);
        mainSplit.addToSecondary(rightPanel);
        mainSplit.setSplitterPosition(60);
        
        // Boutons de dialogue
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setWidthFull();
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        Button saveButton = new Button("Sauvegarder le formulaire", e -> saveFormSchema());
        saveButton.getStyle().set("background-color", "var(--lumo-primary-color)");
        saveButton.getStyle().set("color", "white");
        
        Button cancelButton = new Button("Fermer", e -> close());
        
        buttonLayout.add(saveButton, cancelButton);
        
        VerticalLayout mainLayout = new VerticalLayout(mainSplit, buttonLayout);
        mainLayout.setSizeFull();
        mainLayout.setPadding(false);
        mainLayout.setSpacing(true);
        
        add(mainLayout);
    }
    
    private VerticalLayout createFormBuilderPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.setPadding(false);
        panel.setSpacing(true);
        panel.setWidthFull();
        
        H2 title = new H2("Configuration du Formulaire");
        panel.add(title);
        
        configureGrid();
        panel.add(createToolbar(), grid, createSchemaEditor());
        
        return panel;
    }
    
    private VerticalLayout createPreviewPanel() {
        VerticalLayout panel = new VerticalLayout();
        panel.setPadding(false);
        panel.setSpacing(true);
        panel.setWidthFull();
        
        H2 title = new H2("Aperçu du Formulaire");
        previewContainer.setWidthFull();
        panel.add(title, previewContainer);
        
        return panel;
    }
    
    private HorizontalLayout createToolbar() {
        Button addField = new Button("Ajouter un champ", e -> openFieldDialog(null));
        addField.getStyle().set("background-color", "var(--lumo-primary-color)");
        addField.getStyle().set("color", "white");
        
        Button exportJson = new Button("Exporter JSON", e -> exportSchema());
        
        HorizontalLayout toolbar = new HorizontalLayout(addField, exportJson);
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);
        return toolbar;
    }
    
    private Div createSchemaEditor() {
        schemaJson.setWidthFull();
        schemaJson.setMinHeight("150px");
        Button apply = new Button("Appliquer JSON", e -> applySchemaFromJson());
        Div wrapper = new Div(schemaJson, apply);
        wrapper.getStyle().set("display", "flex")
               .set("flex-direction", "column")
               .set("gap", "var(--lumo-space-s)");
        return wrapper;
    }
    
    private void configureGrid() {
        grid.setDataProvider(dataProvider);
        grid.addColumn(FormField::getName).setHeader("Nom").setAutoWidth(true);
        grid.addColumn(FormField::getLabel).setHeader("Label").setAutoWidth(true);
        grid.addColumn(ff -> ff.getOrder() == null ? "" : String.valueOf(ff.getOrder()))
            .setHeader("Ordre").setAutoWidth(true);
        grid.addColumn(FormField::getType).setHeader("Type").setAutoWidth(true);
        grid.addColumn(ff -> ff.getRequired() != null && ff.getRequired() ? "Oui" : "Non")
            .setHeader("Requis");
        
        grid.addComponentColumn(ff -> new Button("Modifier", e -> openFieldDialog(ff)))
            .setWidth("120px");
        grid.addComponentColumn(ff -> {
            Button deleteBtn = new Button("Supprimer", e -> deleteField(ff));
            deleteBtn.getStyle().set("color", "red");
            return deleteBtn;
        }).setWidth("120px");
    }
    
    private void deleteField(FormField formField) {
        dataProvider.getItems().remove(formField);
        dataProvider.refreshAll();
        schema.setFields(new ArrayList<>(dataProvider.getItems()));
        refreshPreview();
    }
    
    private void openFieldDialog(FormField existing) {
        FormFieldDialog dialog = new FormFieldDialog(existing);
        dialog.addSaveListener(formField -> {
            if (existing == null) {
                dataProvider.getItems().add(formField);
            } else {
                dataProvider.getItems().remove(existing);
                dataProvider.getItems().add(formField);
            }
            dataProvider.refreshAll();
            schema.setFields(new ArrayList<>(dataProvider.getItems()));
            refreshAll();
        });
        dialog.open();
    }
    
    private void exportSchema() {
        try {
            schema.setFields(new ArrayList<>(dataProvider.getItems()));
            String json = schema.toJson();
            schemaJson.setValue(json);
            Notification.show("Schema généré en JSON");
        } catch (Exception ex) {
            Notification.show("Erreur: " + ex.getMessage());
        }
    }
    
    private void applySchemaFromJson() {
        try {
            FormSchema s = FormSchema.fromJson(schemaJson.getValue());
            schema.setKey(s.getKey());
            schema.setTitle(s.getTitle());
            schema.setFields(s.getFields());
            dataProvider.getItems().clear();
            dataProvider.getItems().addAll(s.getFields());
            dataProvider.refreshAll();
            refreshAll();
            Notification.show("Schema appliqué");
        } catch (Exception ex) {
            Notification.show("JSON invalide: " + ex.getMessage());
        }
    }
    
    private void refreshPreview() {
        previewContainer.removeAll();
        previewContainer.add(FormRenderer.createFormWithAnswerBox(schema));
    }
    
    private void refreshAll() {
        refreshPreview();
        exportSchema();
    }
    
    private void saveFormSchema() {
        try {
            schema.setFields(new ArrayList<>(dataProvider.getItems()));
            String jsonSchema = schema.toJson();
            
            prestationRef.setFormSchemaJson(jsonSchema);
            service.save(prestationRef);
            
            saveListeners.forEach(listener -> listener.onSave(prestationRef));
            Notification.show("Formulaire sauvegardé avec succès");
            close();
            
        } catch (Exception e) {
            Notification.show("Erreur lors de la sauvegarde: " + e.getMessage());
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