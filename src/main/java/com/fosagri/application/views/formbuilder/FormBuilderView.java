package com.fosagri.application.views.formbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.entities.PrestationField;
import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.forms.*;
import com.fosagri.application.services.PrestationFieldService;
import com.fosagri.application.services.PrestationRefService;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@PageTitle("Génération des formulaires")
@Route(value = "form-builder", layout = MainLayout.class)
@RolesAllowed("ADMIN")
@Menu(order = 9, icon = LineAwesomeIconUrl.EDIT_SOLID)
public class FormBuilderView extends VerticalLayout {

    private final PrestationRefService prestationRefService;
    private final PrestationFieldService prestationFieldService;

    private final FormSchema schema = new FormSchema();
    private final Grid<FormField> grid = new Grid<>(FormField.class, false);
    private final ListDataProvider<FormField> dataProvider = new ListDataProvider<>(new ArrayList<>());
    private final TextArea schemaJson = new TextArea("Schema JSON");
    private final Div previewContainer = new Div();

    private ComboBox<PrestationRef> prestationCombo;
    private PrestationRef selectedPrestation;

    public FormBuilderView(PrestationRefService prestationRefService, PrestationFieldService prestationFieldService) {
        this.prestationRefService = prestationRefService;
        this.prestationFieldService = prestationFieldService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        schema.setKey("custom");
        schema.setTitle("Nouveau formulaire");

        add(createHeader());
        add(createPrestationSelector());

        SplitLayout split = new SplitLayout();
        split.setSizeFull();
        split.setSplitterPosition(55);
        add(split);

        VerticalLayout left = new VerticalLayout();
        left.setPadding(false);
        left.setSpacing(true);
        left.setSizeFull();

        configureGrid();
        left.add(createToolbar(), grid);

        VerticalLayout right = new VerticalLayout();
        right.setPadding(false);
        right.setSpacing(true);
        right.setSizeFull();

        H3 previewTitle = new H3("Aperçu du formulaire");
        previewContainer.setWidthFull();
        previewContainer.getStyle().set("overflow-y", "auto").set("max-height", "500px");
        right.add(previewTitle, previewContainer);

        split.addToPrimary(left);
        split.addToSecondary(right);

        refreshAll();
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        H2 title = new H2("Génération des formulaires");
        title.getStyle().set("margin", "0");

        header.add(title);
        return header;
    }

    private HorizontalLayout createPrestationSelector() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.END);
        layout.setSpacing(true);

        prestationCombo = new ComboBox<>("Sélectionner une prestation");
        prestationCombo.setWidth("400px");
        prestationCombo.setItemLabelGenerator(p -> p.getLabel() + " (ID: " + p.getId() + ")");
        prestationCombo.setItems(prestationRefService.findAll());
        prestationCombo.addValueChangeListener(e -> {
            selectedPrestation = e.getValue();
            if (selectedPrestation != null) {
                loadFieldsFromPrestation(selectedPrestation);
            } else {
                clearFields();
            }
        });

        Button refreshBtn = new Button(VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> {
            prestationCombo.setItems(prestationRefService.findAll());
        });

        layout.add(prestationCombo, refreshBtn);
        return layout;
    }

    private void loadFieldsFromPrestation(PrestationRef prestation) {
        // Clear current fields
        dataProvider.getItems().clear();

        // Update schema
        schema.setKey(prestation.getLabel());
        schema.setTitle(prestation.getLabel());

        // Load fields from database
        List<PrestationField> prestationFields = prestationFieldService.findByPrestationRefId(prestation.getId());

        // Convert PrestationField to FormField
        for (PrestationField pf : prestationFields) {
            FormField ff = convertToFormField(pf);
            dataProvider.getItems().add(ff);
        }

        dataProvider.refreshAll();
        schema.setFields(new ArrayList<>(dataProvider.getItems()));
        refreshAll();

        Notification.show("Chargé " + prestationFields.size() + " champs pour '" + prestation.getLabel() + "'",
                3000, Notification.Position.TOP_END);
    }

    private FormField convertToFormField(PrestationField pf) {
        FormField ff = new FormField();
        ff.setName(pf.getColonne());
        ff.setLabel(pf.getLabel());
        ff.setType(mapFieldType(pf.getFieldtype()));
        ff.setRequired(pf.isRequired());
        ff.setOrder(pf.getOrdre());

        // Parse options from valeurs (format: "value1,value2,value3" or "value1:label1,value2:label2")
        if (pf.getValeurs() != null && !pf.getValeurs().isEmpty()) {
            List<FieldOption> options = new ArrayList<>();
            // Support both comma and semicolon as separators
            String separator = pf.getValeurs().contains(";") ? ";" : ",";
            for (String part : pf.getValeurs().split(separator)) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    if (trimmed.contains(":")) {
                        String[] kv = trimmed.split(":", 2);
                        options.add(new FieldOption(kv[0].trim(), kv[1].trim()));
                    } else {
                        options.add(new FieldOption(trimmed, trimmed));
                    }
                }
            }
            if (!options.isEmpty()) {
                ff.setOptions(options);
            }
        }

        // Parse condition
        if (pf.getCondition() != null && !pf.getCondition().isEmpty()) {
            String[] parts = pf.getCondition().split(",", 3);
            if (parts.length >= 2) {
                Condition c = new Condition();
                c.setField(parts[0].trim());
                c.setOperator(parts.length > 1 ? parts[1].trim() : "equals");
                c.setValue(parts.length > 2 ? parts[2].trim() : null);
                ff.setCondition(c);
            }
        }

        return ff;
    }

    // Map database fieldtype (French) to form-builder type
    private String mapFieldType(String fieldtype) {
        if (fieldtype == null) return "text";
        switch (fieldtype.toLowerCase()) {
            // French types from database
            case "chiffre":
                return "number";
            case "texte":
                return "text";
            case "textarea":
                return "textarea";
            case "label":
                return "label";
            case "enfant":
                return "enfant";
            case "multioption":
                return "multiselect"; // MultiOption => multiselect dropdown
            case "checkbox":
                return "checkbox"; // Checkbox stays as checkbox
            case "date":
                return "date";
            case "option":
                return "select";
            case "conjoint":
                return "conjoint";
            case "file":
                return "file";
            // English fallback types
            case "text":
            case "string":
                return "text";
            case "text_area":
                return "textarea";
            case "number":
            case "integer":
            case "decimal":
                return "number";
            case "select":
            case "dropdown":
            case "combo":
                return "select";
            case "multiselect":
                return "multiselect";
            case "boolean":
                return "checkbox";
            case "upload":
                return "file";
            default:
                return "text";
        }
    }

    // Map form-builder type back to database fieldtype (French)
    private String mapToDbFieldType(String formType) {
        if (formType == null) return "Texte";
        switch (formType.toLowerCase()) {
            case "number":
                return "Chiffre";
            case "text":
                return "Texte";
            case "textarea":
                return "Textarea";
            case "label":
                return "Label";
            case "enfant":
                return "Enfant";
            case "conjoint":
                return "Conjoint";
            case "checkbox":
                return "Checkbox";
            case "multiselect":
                return "MultiOption";
            case "date":
                return "Date";
            case "select":
                return "Option";
            case "file":
                return "File";
            default:
                return "Texte";
        }
    }

    private PrestationField convertToPrestationField(FormField ff, PrestationRef prestation) {
        PrestationField pf = new PrestationField();
        pf.setPrestationRef(prestation);
        pf.setColonne(ff.getName());
        pf.setLabel(ff.getLabel());
        pf.setFieldtype(mapToDbFieldType(ff.getType()));
        pf.setRequired(Boolean.TRUE.equals(ff.getRequired()));
        pf.setOrdre(ff.getOrder() != null ? ff.getOrder() : 0);
        pf.setActive(true);

        // Convert options to valeurs (comma-separated format)
        if (ff.getOptions() != null && !ff.getOptions().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (FieldOption opt : ff.getOptions()) {
                if (sb.length() > 0) sb.append(",");
                // Use simple value if value equals label, otherwise use value:label format
                if (opt.getValue().equals(opt.getLabel())) {
                    sb.append(opt.getValue());
                } else {
                    sb.append(opt.getValue()).append(":").append(opt.getLabel());
                }
            }
            pf.setValeurs(sb.toString());
        }

        // Convert condition
        if (ff.getCondition() != null) {
            Condition c = ff.getCondition();
            pf.setCondition(String.join(",",
                    Objects.toString(c.getField(), ""),
                    Objects.toString(c.getOperator(), ""),
                    Objects.toString(c.getValue(), "")));
        }

        return pf;
    }

    private void clearFields() {
        dataProvider.getItems().clear();
        dataProvider.refreshAll();
        schema.setKey("custom");
        schema.setTitle("Nouveau formulaire");
        schema.setFields(new ArrayList<>());
        refreshAll();
    }

    private HorizontalLayout createToolbar() {
        Button addField = new Button("Ajouter un champ", VaadinIcon.PLUS.create(), e -> openFieldDialog(null));
        addField.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button saveBtn = new Button("Sauvegarder", VaadinIcon.CHECK.create(), e -> saveFieldsToPrestation());
        saveBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);

        HorizontalLayout hl = new HorizontalLayout(addField, saveBtn);
        hl.setAlignItems(FlexComponent.Alignment.BASELINE);
        return hl;
    }

    private void saveFieldsToPrestation() {
        if (selectedPrestation == null) {
            Notification.show("Veuillez sélectionner une prestation", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        try {
            List<FormField> formFields = new ArrayList<>(dataProvider.getItems());
            List<PrestationField> prestationFields = new ArrayList<>();

            int order = 1;
            for (FormField ff : formFields) {
                PrestationField pf = convertToPrestationField(ff, selectedPrestation);
                pf.setOrdre(order++);
                prestationFields.add(pf);
            }

            // Save to database (prestation_field table)
            prestationFieldService.replaceFieldsForPrestation(selectedPrestation, prestationFields);

            // Refresh the schema for preview
            schema.setFields(formFields);

            Notification.show("Sauvegardé " + prestationFields.size() + " champs pour '" + selectedPrestation.getLabel() + "'",
                    3000, Notification.Position.TOP_END)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (Exception ex) {
            Notification.show("Erreur: " + ex.getMessage(), 5000, Notification.Position.TOP_END)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private Div createSchemaEditor() {
        schemaJson.setWidthFull();
        schemaJson.setMinHeight("150px");
        Button apply = new Button("Appliquer JSON", e -> applySchemaFromJson());
        apply.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        Div wrapper = new Div(schemaJson, apply);
        wrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "var(--lumo-space-s)");
        return wrapper;
    }

    private void configureGrid() {
        grid.setDataProvider(dataProvider);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        grid.setHeight("300px");

        grid.addColumn(FormField::getName).setHeader("Nom").setAutoWidth(true);
        grid.addColumn(FormField::getLabel).setHeader("Label").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(ff -> ff.getOrder() == null ? "" : String.valueOf(ff.getOrder()))
                .setHeader("Ordre").setWidth("80px").setFlexGrow(0);
        grid.addColumn(FormField::getType).setHeader("Type").setAutoWidth(true);
        grid.addComponentColumn(ff -> {
            Span badge = new Span(Boolean.TRUE.equals(ff.getRequired()) ? "Oui" : "Non");
            badge.getStyle()
                    .set("padding", "2px 8px")
                    .set("border-radius", "4px")
                    .set("font-size", "0.75rem");
            if (Boolean.TRUE.equals(ff.getRequired())) {
                badge.getStyle().set("background", "#fee2e2").set("color", "#991b1b");
            } else {
                badge.getStyle().set("background", "#f3f4f6").set("color", "#374151");
            }
            return badge;
        }).setHeader("Requis").setWidth("80px");

        grid.addComponentColumn(ff -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e -> openFieldDialog(ff));

            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> deleteField(ff));

            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setWidth("120px").setFlexGrow(0);
    }

    private void deleteField(FormField ff) {
        if (selectedPrestation == null) {
            Notification.show("Veuillez d'abord sélectionner une prestation", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        dataProvider.getItems().remove(ff);
        dataProvider.refreshAll();
        schema.setFields(new ArrayList<>(dataProvider.getItems()));

        // Save immediately to database
        saveFieldsToPrestation();

        refreshPreview();
    }

    private void openFieldDialog(FormField existing) {
        if (selectedPrestation == null) {
            Notification.show("Veuillez d'abord sélectionner une prestation", 3000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        Dialog dlg = new Dialog();
        dlg.setHeaderTitle(existing == null ? "Nouveau champ" : "Modifier le champ");
        dlg.setWidth("500px");

        Binder<FieldFormModel> binder = new Binder<>(FieldFormModel.class);
        FieldFormModel model = existing == null ? new FieldFormModel() : FieldFormModel.from(existing);

        FormLayout fl = new FormLayout();
        fl.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 2));

        TextField name = new TextField("Nom (colonne)");
        name.setRequired(true);

        TextField label = new TextField("Label");
        label.setRequired(true);

        com.vaadin.flow.component.select.Select<String> type = new com.vaadin.flow.component.select.Select<>();
        type.setLabel("Type");
        type.setItems("text", "textarea", "number", "date", "select", "checkbox", "multiselect", "label", "enfant", "conjoint", "file");

        TextField placeholder = new TextField("Placeholder");

        com.vaadin.flow.component.select.Select<String> required = new com.vaadin.flow.component.select.Select<>();
        required.setLabel("Requis");
        required.setItems("true", "false");

        TextField options = new TextField("Options");
        options.setHelperText("Format: option1,option2,option3 ou val1:Label1,val2:Label2");

        TextField cond = new TextField("Condition");
        cond.setHelperText("Format: champ,operateur,valeur");

        com.vaadin.flow.component.textfield.IntegerField order = new com.vaadin.flow.component.textfield.IntegerField("Ordre");

        fl.add(name, label, type, placeholder, required, order, options, cond);
        fl.setColspan(options, 2);
        fl.setColspan(cond, 2);

        binder.bind(name, FieldFormModel::getName, FieldFormModel::setName);
        binder.bind(label, FieldFormModel::getLabel, FieldFormModel::setLabel);
        binder.bind(type, FieldFormModel::getType, FieldFormModel::setType);
        binder.bind(placeholder, FieldFormModel::getPlaceholder, FieldFormModel::setPlaceholder);
        binder.bind(required, FieldFormModel::getRequired, FieldFormModel::setRequired);
        binder.bind(options, FieldFormModel::getOptions, FieldFormModel::setOptions);
        binder.bind(cond, FieldFormModel::getCond, FieldFormModel::setCond);
        binder.bind(order, FieldFormModel::getOrder, FieldFormModel::setOrder);

        binder.readBean(model);

        Button save = new Button("Enregistrer", e -> {
            try {
                binder.writeBean(model);
                FormField ff = model.toFormField();

                // Update local data
                if (existing == null) {
                    // Set order for new field
                    int maxOrder = dataProvider.getItems().stream()
                        .mapToInt(f -> f.getOrder() != null ? f.getOrder() : 0)
                        .max().orElse(0);
                    ff.setOrder(maxOrder + 1);
                    dataProvider.getItems().add(ff);
                } else {
                    dataProvider.getItems().remove(existing);
                    dataProvider.getItems().add(ff);
                }
                dataProvider.refreshAll();
                schema.setFields(new ArrayList<>(dataProvider.getItems()));

                // Save immediately to database
                saveFieldsToPrestation();

                refreshAll();
                dlg.close();
            } catch (ValidationException ex) {
                Notification.show("Erreur: " + ex.getMessage());
            }
        });
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancel = new Button("Annuler", e -> dlg.close());
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        dlg.add(fl);
        dlg.getFooter().add(actions);
        dlg.open();
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
    }

    // Helper DTO for dialog editing
    public static class FieldFormModel {
        private String name;
        private String label;
        private String type;
        private String placeholder;
        private String required;
        private String options;
        private String cond;
        private Integer order;

        public static FieldFormModel from(FormField ff) {
            FieldFormModel m = new FieldFormModel();
            m.name = ff.getName();
            m.label = ff.getLabel();
            m.type = ff.getType();
            m.placeholder = ff.getPlaceholder();
            m.required = String.valueOf(Boolean.TRUE.equals(ff.getRequired()));
            if (ff.getOptions() != null) {
                String s = ff.getOptions().stream()
                    .map(o -> o.getValue().equals(o.getLabel()) ? o.getValue() : o.getValue() + ":" + o.getLabel())
                    .reduce((a, b) -> a + "," + b).orElse("");
                m.options = s;
            }
            if (ff.getCondition() != null) {
                Condition c = ff.getCondition();
                m.cond = String.join(",", Objects.toString(c.getField(), ""), Objects.toString(c.getOperator(), ""), Objects.toString(c.getValue(), ""));
            }
            m.order = ff.getOrder();
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
                // Support both comma and semicolon as separators
                String separator = options.contains(";") ? ";" : ",";
                for (String part : options.split(separator)) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        if (trimmed.contains(":")) {
                            String[] kv = trimmed.split(":", 2);
                            opts.add(new FieldOption(kv[0].trim(), kv[1].trim()));
                        } else {
                            opts.add(new FieldOption(trimmed, trimmed));
                        }
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
            return f;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        public String getRequired() {
            return required;
        }

        public void setRequired(String required) {
            this.required = required;
        }

        public String getOptions() {
            return options;
        }

        public void setOptions(String options) {
            this.options = options;
        }

        public String getCond() {
            return cond;
        }

        public void setCond(String cond) {
            this.cond = cond;
        }

        public Integer getOrder() {
            return order;
        }

        public void setOrder(Integer order) {
            this.order = order;
        }
    }
}
