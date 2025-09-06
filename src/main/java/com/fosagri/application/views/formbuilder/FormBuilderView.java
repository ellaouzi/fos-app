package com.fosagri.application.views.formbuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.forms.*;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@PageTitle("Form Builder")
@Route("form-builder")
@Menu(order = 7, icon = LineAwesomeIconUrl.EDIT_SOLID)
public class FormBuilderView extends VerticalLayout {

    private final FormSchema schema = new FormSchema();
    private final Grid<FormField> grid = new Grid<>(FormField.class, false);
    private final ListDataProvider<FormField> dataProvider = new ListDataProvider<>(new ArrayList<>());
    private final TextArea schemaJson = new TextArea("Schema JSON");
    private final Div previewContainer = new Div();

    public FormBuilderView() {
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        schema.setKey("custom");
        schema.setTitle("Nouveau formulaire");

        H2 title = new H2("Form Builder");
        add(title);

        SplitLayout split = new SplitLayout();
        split.setSizeFull();
        add(split);

        VerticalLayout left = new VerticalLayout();
        left.setPadding(false);
        left.setSpacing(true);
        left.setWidthFull();

        configureGrid();
        left.add(createToolbar(), grid, createSchemaEditor());

        VerticalLayout right = new VerticalLayout();
        right.setPadding(false);
        right.setSpacing(true);
        right.setWidthFull();

        H2 previewTitle = new H2("Live Preview");
        previewContainer.setWidthFull();
        right.add(previewTitle, previewContainer);

        split.addToPrimary(left);
        split.addToSecondary(right);

        refreshAll();
    }

    private HorizontalLayout createToolbar() {
        Button addField = new Button("Ajouter un champ", e -> openFieldDialog(null));
        Button exportJson = new Button("Exporter JSON", e -> exportSchema());
        HorizontalLayout hl = new HorizontalLayout(addField, exportJson);
        hl.setAlignItems(FlexComponent.Alignment.BASELINE);
        return hl;
    }

    private Div createSchemaEditor() {
        schemaJson.setWidthFull();
        schemaJson.setMinHeight("200px");
        Button apply = new Button("Appliquer JSON", e -> applySchemaFromJson());
        Div wrapper = new Div(schemaJson, apply);
        wrapper.getStyle().set("display", "flex").set("flex-direction", "column").set("gap", "var(--lumo-space-s)");
        return wrapper;
    }

    private void configureGrid() {
        grid.setDataProvider(dataProvider);
        grid.addColumn(FormField::getName).setHeader("name").setAutoWidth(true);
        grid.addColumn(FormField::getLabel).setHeader("label").setAutoWidth(true);
        grid.addColumn(ff -> ff.getOrder() == null ? "" : String.valueOf(ff.getOrder()))
            .setHeader("order").setAutoWidth(true);
        grid.addColumn(FormField::getType).setHeader("type").setAutoWidth(true);
        grid.addColumn(ff -> ff.getRequired() != null && ff.getRequired() ? "true" : "false").setHeader("required");
        grid.addComponentColumn(ff -> new Button("Edit", e -> openFieldDialog(ff))).setWidth("120px");
        grid.addComponentColumn(ff -> new Button("Delete", e -> deleteField(ff))).setWidth("120px");
    }

    private void deleteField(FormField ff) {
        dataProvider.getItems().remove(ff);
        dataProvider.refreshAll();
        schema.setFields(new ArrayList<>(dataProvider.getItems()));
        refreshPreview();
    }

    private void openFieldDialog(FormField existing) {
        Dialog dlg = new Dialog();
        dlg.setHeaderTitle(existing == null ? "Nouveau champ" : "Modifier le champ");

        Binder<FieldFormModel> binder = new Binder<>(FieldFormModel.class);
        FieldFormModel model = existing == null ? new FieldFormModel() : FieldFormModel.from(existing);

        FormLayout fl = new FormLayout();
        TextField name = new TextField("name");
        TextField label = new TextField("label");
        com.vaadin.flow.component.select.Select<String> type = new com.vaadin.flow.component.select.Select<>();
        type.setLabel("type");
        type.setItems("text", "textarea", "number", "date", "select", "checkbox", "enfant", "conjoint");
        TextField placeholder = new TextField("placeholder");
        com.vaadin.flow.component.select.Select<String> required = new com.vaadin.flow.component.select.Select<>();
        required.setLabel("required");
        required.setItems("true", "false");
        TextField options = new TextField("options (value:label;...)");
        TextField cond = new TextField("condition (field,op,value)");
        com.vaadin.flow.component.textfield.IntegerField order = new com.vaadin.flow.component.textfield.IntegerField("order");

        fl.add(name, label, type, placeholder, required, options, cond, order);

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
                if (existing == null) {
                    dataProvider.getItems().add(ff);
                } else {
                    dataProvider.getItems().remove(existing);
                    dataProvider.getItems().add(ff);
                }
                dataProvider.refreshAll();
                schema.setFields(new ArrayList<>(dataProvider.getItems()));
                refreshAll();
                dlg.close();
            } catch (ValidationException ex) {
                Notification.show("Erreur: " + ex.getMessage());
            }
        });

        Button cancel = new Button("Annuler", e -> dlg.close());
        HorizontalLayout actions = new HorizontalLayout(save, cancel);
        dlg.add(fl, actions);
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
                String s = ff.getOptions().stream().map(o -> o.getValue()+":"+o.getLabel()).reduce((a,b)->a+";"+b).orElse("");
                m.options = s;
            }
            if (ff.getCondition() != null) {
                Condition c = ff.getCondition();
                m.cond = String.join(",", Objects.toString(c.getField(),""), Objects.toString(c.getOperator(),""), Objects.toString(c.getValue(),""));
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
                for (String pair : options.split(";")) {
                    String[] kv = pair.split(":",2);
                    if (kv.length == 2) opts.add(new FieldOption(kv[0].trim(), kv[1].trim()));
                }
                f.setOptions(opts);
            }
            if (cond != null && !cond.isBlank()) {
                String[] parts = cond.split(",",3);
                Condition c = new Condition();
                c.setField(parts.length>0?parts[0].trim():null);
                c.setOperator(parts.length>1?parts[1].trim():null);
                c.setValue(parts.length>2?parts[2].trim():null);
                f.setCondition(c);
            }
            f.setOrder(order);
            return f;
        }

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
    }
}
