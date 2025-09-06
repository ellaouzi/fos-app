package com.fosagri.application.forms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.model.AdhEnfant;
import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.service.AdhEnfantService;
import com.fosagri.application.service.AdhConjointService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.util.Base64;

/**
 * FormRenderer - Renders dynamic forms based on FormSchema
 * 
 * Supported field types:
 * - text: Text input field
 * - textarea: Multi-line text area
 * - number: Number input field
 * - date: Date picker
 * - select: ComboBox with predefined options
 * - checkbox: Boolean checkbox
 * - file: File upload with configurable max files and accepted types
 * - enfant: ComboBox populated with children of the selected agent
 * - conjoint: ComboBox populated with spouses of the selected agent
 * 
 * Note: enfant and conjoint types require agent context to be passed via
 * createForm(schema, onSubmit, selectedAgent, enfantService, conjointService)
 * 
 * JSON Data Structure for enfant/conjoint fields:
 * {
 *   "enfant_beneficiaire": {
 *     "id": 123,
 *     "nom": "Dupont", 
 *     "prenom": "Marie"
 *   },
 *   "conjoint_concerne": {
 *     "id": 456,
 *     "nom": "Martin",
 *     "prenom": "Jean"
 *   }
 * }
 */
public class FormRenderer {

    public static Component render(FormSchema schema) {
        return createForm(schema, null);
    }

    public static Component createForm(FormSchema schema, java.util.function.Consumer<Map<String, Object>> onSubmit) {
        return createForm(schema, onSubmit, null, null, null);
    }

    // Helper class to return both form and currentValues
    public static class FormWithValues {
        public final Component form;
        public final Map<String, Object> currentValues;
        
        public FormWithValues(Component form, Map<String, Object> currentValues) {
            this.form = form;
            this.currentValues = currentValues;
        }
    }
    
    public static FormWithValues createFormWithValues(FormSchema schema, java.util.function.Consumer<Map<String, Object>> onSubmit, 
                                                     AdhAgent selectedAgent, AdhEnfantService enfantService, AdhConjointService conjointService) {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);
        container.setSpacing(true);

        if (schema.getTitle() != null) {
            container.add(new H3(schema.getTitle()));
        }

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();

        Map<String, Component> fieldComponents = new HashMap<>();
        Map<String, Object> currentValues = new HashMap<>();

        List<FormField> fields = new ArrayList<>(schema.getFields());
        fields.sort(Comparator
                .comparing((FormField f) -> f.getOrder() == null ? Integer.MAX_VALUE : f.getOrder())
                .thenComparing(FormField::getName, Comparator.nullsLast(String::compareTo)));
        for (FormField field : fields) {
            Component comp = createFieldComponent(field, currentValues, () -> applyConditions(schema, fieldComponents, currentValues), selectedAgent, enfantService, conjointService);
            fieldComponents.put(field.getName(), comp);
            formLayout.add(comp);
        }

        container.add(formLayout);

        // Apply initial visibility based on conditions
        applyConditions(schema, fieldComponents, currentValues);

        if (onSubmit != null) {
            Button submit = new Button("Soumettre", e -> {
                Map<String, Object> answers = collectAnswers(schema, fieldComponents, currentValues);
                onSubmit.accept(answers);
            });
            container.add(submit);
        }

        return new FormWithValues(container, currentValues);
    }

    public static Component createForm(FormSchema schema, java.util.function.Consumer<Map<String, Object>> onSubmit, 
                                     AdhAgent selectedAgent, AdhEnfantService enfantService, AdhConjointService conjointService) {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);
        container.setSpacing(true);

        if (schema.getTitle() != null) {
            container.add(new H3(schema.getTitle()));
        }

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();

        Map<String, Component> fieldComponents = new HashMap<>();
        Map<String, Object> currentValues = new HashMap<>();

        List<FormField> fields = new ArrayList<>(schema.getFields());
        fields.sort(Comparator
                .comparing((FormField f) -> f.getOrder() == null ? Integer.MAX_VALUE : f.getOrder())
                .thenComparing(FormField::getName, Comparator.nullsLast(String::compareTo)));
        for (FormField field : fields) {
            Component comp = createFieldComponent(field, currentValues, () -> applyConditions(schema, fieldComponents, currentValues), selectedAgent, enfantService, conjointService);
            fieldComponents.put(field.getName(), comp);
            formLayout.add(comp);
        }

        container.add(formLayout);

        // Apply initial visibility based on conditions
        applyConditions(schema, fieldComponents, currentValues);

        if (onSubmit != null) {
            Button submit = new Button("Soumettre", e -> {
                Map<String, Object> answers = collectAnswers(schema, fieldComponents, currentValues);
                onSubmit.accept(answers);
            });
            container.add(submit);
        }

        return container;
    }

    private static Component createFieldComponent(FormField field, Map<String, Object> currentValues, Runnable onChange, 
                                                AdhAgent selectedAgent, AdhEnfantService enfantService, AdhConjointService conjointService) {
        String type = Optional.ofNullable(field.getType()).orElse("text").toLowerCase(Locale.ROOT);
        switch (type) {
            case "number": {
                NumberField nf = new NumberField(field.getLabel());
                nf.setPlaceholder(field.getPlaceholder());
                nf.setRequiredIndicatorVisible(Boolean.TRUE.equals(field.getRequired()));
                nf.addValueChangeListener(e -> { currentValues.put(field.getName(), e.getValue()); onChange.run(); });
                return nf;
            }
            case "date": {
                DatePicker dp = new DatePicker(field.getLabel());
                dp.setPlaceholder(field.getPlaceholder());
                dp.setRequiredIndicatorVisible(Boolean.TRUE.equals(field.getRequired()));
                dp.addValueChangeListener(e -> { currentValues.put(field.getName(), e.getValue()); onChange.run(); });
                return dp;
            }
            case "select": {
                ComboBox<FieldOption> cb = new ComboBox<>(field.getLabel());
                List<FieldOption> options = Optional.ofNullable(field.getOptions()).orElse(Collections.emptyList());
                cb.setItems(options);
                cb.setItemLabelGenerator(FieldOption::getLabel);
                cb.addValueChangeListener(e -> {
                    FieldOption fo = e.getValue();
                    currentValues.put(field.getName(), fo != null ? fo.getValue() : null);
                    onChange.run();
                });
                return cb;
            }
            case "checkbox": {
                Checkbox cbx = new Checkbox(field.getLabel());
                cbx.setValue(false);
                cbx.addValueChangeListener(e -> { currentValues.put(field.getName(), e.getValue()); onChange.run(); });
                return cbx;
            }
            case "enfant": {
                ComboBox<AdhEnfant> enfantCombo = new ComboBox<>(field.getLabel());
                enfantCombo.setPlaceholder("Sélectionner un enfant");
                enfantCombo.setRequiredIndicatorVisible(Boolean.TRUE.equals(field.getRequired()));
                enfantCombo.setItemLabelGenerator(enfant -> 
                    enfant.getNom_pac() + " " + enfant.getPr_pac() + 
                    (enfant.getDat_n_pac() != null ? " (né le " + enfant.getDat_n_pac() + ")" : ""));
                
                if (selectedAgent != null && enfantService != null) {
                    List<AdhEnfant> enfants = enfantService.findBasicInfoByAgent(selectedAgent);
                    enfantCombo.setItems(enfants);
                    if (enfants.isEmpty()) {
                        enfantCombo.setPlaceholder("Aucun enfant trouvé pour cet agent");
                    }
                }
                
                enfantCombo.addValueChangeListener(e -> {
                    AdhEnfant enfant = e.getValue();
                    if (enfant != null) {
                        // Store complete enfant object with id, nom, prenom
                        Map<String, Object> enfantData = new LinkedHashMap<>();
                        enfantData.put("id", enfant.getAdhEnfantId());
                        enfantData.put("nom", enfant.getNom_pac());
                        enfantData.put("prenom", enfant.getPr_pac());
                        currentValues.put(field.getName(), enfantData);
                    } else {
                        currentValues.put(field.getName(), null);
                    }
                    onChange.run();
                });
                return enfantCombo;
            }
            case "conjoint": {
                ComboBox<AdhConjoint> conjointCombo = new ComboBox<>(field.getLabel());
                conjointCombo.setPlaceholder("Sélectionner un conjoint");
                conjointCombo.setRequiredIndicatorVisible(Boolean.TRUE.equals(field.getRequired()));
                conjointCombo.setItemLabelGenerator(conjoint -> 
                    conjoint.getNOM_CONJ() + " " + conjoint.getPR_CONJ() + 
                    (conjoint.getDat_N_CONJ() != null ? " (né le " + conjoint.getDat_N_CONJ() + ")" : ""));
                
                if (selectedAgent != null && conjointService != null) {
                    List<AdhConjoint> conjoints = conjointService.findBasicInfoByAgent(selectedAgent);
                    conjointCombo.setItems(conjoints);
                    if (conjoints.isEmpty()) {
                        conjointCombo.setPlaceholder("Aucun conjoint trouvé pour cet agent");
                    }
                }
                
                conjointCombo.addValueChangeListener(e -> {
                    AdhConjoint conjoint = e.getValue();
                    if (conjoint != null) {
                        // Store complete conjoint object with id, nom, prenom
                        Map<String, Object> conjointData = new LinkedHashMap<>();
                        conjointData.put("id", conjoint.getAdhConjointId());
                        conjointData.put("nom", conjoint.getNOM_CONJ());
                        conjointData.put("prenom", conjoint.getPR_CONJ());
                        currentValues.put(field.getName(), conjointData);
                    } else {
                        currentValues.put(field.getName(), null);
                    }
                    onChange.run();
                });
                return conjointCombo;
            }
            case "textarea": {
                TextArea ta = new TextArea(field.getLabel());
                ta.setPlaceholder(field.getPlaceholder());
                ta.setRequiredIndicatorVisible(Boolean.TRUE.equals(field.getRequired()));
                ta.setWidthFull();
                ta.setMinHeight("120px");
                ta.addValueChangeListener(e -> { currentValues.put(field.getName(), e.getValue()); onChange.run(); });
                return ta;
            }
            case "file": {
                return createFileUploadComponent(field, currentValues, onChange);
            }
            case "text":
            default: {
                TextField tf = new TextField(field.getLabel());
                tf.setPlaceholder(field.getPlaceholder());
                tf.setRequiredIndicatorVisible(Boolean.TRUE.equals(field.getRequired()));
                tf.addValueChangeListener(e -> { currentValues.put(field.getName(), e.getValue()); onChange.run(); });
                return tf;
            }
        }
    }

    private static void applyConditions(FormSchema schema, Map<String, Component> fieldComponents, Map<String, Object> values) {
        Map<String, FormField> fieldDefs = schema.getFields().stream().collect(Collectors.toMap(FormField::getName, f -> f, (a,b)->a, LinkedHashMap::new));
        for (FormField field : schema.getFields()) {
            Component comp = fieldComponents.get(field.getName());
            boolean visible = evaluateVisibility(field, fieldDefs, values);
            comp.setVisible(visible);
        }
    }

    private static boolean evaluateVisibility(FormField field, Map<String, FormField> defs, Map<String, Object> values) {
        Condition c = field.getCondition();
        if (c == null || c.getField() == null || c.getOperator() == null) return true; // visible by default
        Object otherVal = values.get(c.getField());
        String op = c.getOperator();
        String target = c.getValue();
        if (otherVal == null) return false; // hide when dependency missing
        switch (op) {
            case "eq":
                return Objects.equals(stringify(otherVal), target);
            case "ne":
                return !Objects.equals(stringify(otherVal), target);
            default:
                return true;
        }
    }

    private static String stringify(Object o) {
        if (o instanceof java.time.LocalDate) return o.toString();
        return String.valueOf(o);
    }

    // Legacy method for backward compatibility
    public static Map<String, Object> collectAnswers(FormSchema schema, Map<String, Component> comps) {
        return collectAnswers(schema, comps, new HashMap<>());
    }
    
    public static Map<String, Object> collectAnswers(FormSchema schema, Map<String, Component> comps, Map<String, Object> currentValues) {
        Map<String, Object> ans = new LinkedHashMap<>();
        for (FormField f : schema.getFields()) {
            Component c = comps.get(f.getName());
            if (!c.isVisible()) continue; // only collect visible fields
            switch (Optional.ofNullable(f.getType()).orElse("text")) {
                case "number":
                    ans.put(f.getName(), ((NumberField) c).getValue());
                    break;
                case "date":
                    java.time.LocalDate d = ((DatePicker) c).getValue();
                    ans.put(f.getName(), d != null ? d.toString() : null);
                    break;
                case "select":
                    FieldOption sel = ((ComboBox<FieldOption>) c).getValue();
                    ans.put(f.getName(), sel != null ? sel.getValue() : null);
                    break;
                case "checkbox":
                    ans.put(f.getName(), ((Checkbox) c).getValue());
                    break;
                case "enfant":
                    AdhEnfant selectedEnfant = ((ComboBox<AdhEnfant>) c).getValue();
                    if (selectedEnfant != null) {
                        Map<String, Object> enfantData = new LinkedHashMap<>();
                        enfantData.put("id", selectedEnfant.getAdhEnfantId());
                        enfantData.put("nom", selectedEnfant.getNom_pac());
                        enfantData.put("prenom", selectedEnfant.getPr_pac());
                        ans.put(f.getName(), enfantData);
                    } else {
                        ans.put(f.getName(), null);
                    }
                    break;
                case "conjoint":
                    AdhConjoint selectedConjoint = ((ComboBox<AdhConjoint>) c).getValue();
                    if (selectedConjoint != null) {
                        Map<String, Object> conjointData = new LinkedHashMap<>();
                        conjointData.put("id", selectedConjoint.getAdhConjointId());
                        conjointData.put("nom", selectedConjoint.getNOM_CONJ());
                        conjointData.put("prenom", selectedConjoint.getPR_CONJ());
                        ans.put(f.getName(), conjointData);
                    } else {
                        ans.put(f.getName(), null);
                    }
                    break;
                case "textarea":
                    ans.put(f.getName(), ((TextArea) c).getValue());
                    break;
                case "file":
                    // File data is already stored in currentValues during upload
                    // We need to extract it from the component's current values
                    if (currentValues.containsKey(f.getName())) {
                        ans.put(f.getName(), currentValues.get(f.getName()));
                    }
                    break;
                case "text":
                default:
                    ans.put(f.getName(), ((TextField) c).getValue());
            }
        }
        return ans;
    }

    public static Component createFormWithAnswerBox(FormSchema schema) {
        VerticalLayout wrapper = new VerticalLayout();
        wrapper.setPadding(false);
        TextArea output = new TextArea("Réponses (JSON)");
        output.setWidthFull();
        output.setMinHeight("150px");
        Component form = createForm(schema, answers -> {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new ObjectMapper();
                output.setValue(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(answers));
            } catch (Exception ex) {
                output.setValue("Erreur: " + ex.getMessage());
            }
        });
        wrapper.add(form, output);
        return wrapper;
    }
    
    private static Component createFileUploadComponent(FormField field, Map<String, Object> currentValues, Runnable onChange) {
        VerticalLayout container = new VerticalLayout();
        container.setPadding(false);
        container.setSpacing(true);
        
        // Label with required indicator
        Span label = new Span(field.getLabel());
        if (Boolean.TRUE.equals(field.getRequired())) {
            label.getStyle().set("font-weight", "bold");
            Span required = new Span(" *");
            required.getStyle().set("color", "var(--lumo-error-text-color)");
            label.add(required);
        }
        container.add(label);
        
        // File storage for this field
        List<Map<String, Object>> uploadedFiles = new ArrayList<>();
        currentValues.put(field.getName(), uploadedFiles);
        
        // Configuration
        int maxFiles = field.getMaxFiles() != null ? field.getMaxFiles() : 3;
        String acceptedTypes = field.getAcceptedFileTypes() != null ? field.getAcceptedFileTypes() : ".pdf,.doc,.docx,.jpg,.jpeg,.png";
        
        // Files list display
        Div filesDisplay = new Div();
        filesDisplay.getStyle().set("margin-bottom", "10px");
        updateFilesDisplay(filesDisplay, uploadedFiles);
        container.add(filesDisplay);
        
        // Upload component
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        
        // Configure accepted file types
        String[] types = acceptedTypes.split(",");
        for (int i = 0; i < types.length; i++) {
            types[i] = types[i].trim();
        }
        upload.setAcceptedFileTypes(types);
        upload.setMaxFiles(1); // Process one file at a time
        upload.setMaxFileSize(10 * 1024 * 1024); // 10MB max
        
        // Add basic styling and text
        upload.setUploadButton(new Button("Choisir fichier"));
        upload.setDropAllowed(true);
        
        System.out.println("🔧 Upload component configured:");
        System.out.println("  - Field name: " + field.getName());
        System.out.println("  - Max files: " + maxFiles);
        System.out.println("  - Accepted types: " + String.join(", ", types));
        System.out.println("  - Upload files list initialized: " + uploadedFiles.size());
        
        // Add all event listeners for comprehensive debugging
        upload.addStartedListener(event -> {
            System.out.println("🚀 Upload started: " + event.getFileName() + " (" + event.getContentLength() + " bytes)");
        });
        
        upload.addProgressListener(event -> {
            System.out.println("📊 Upload progress: " + event.getFileName() + " - " + event.getReadBytes() + "/" + event.getContentLength());
        });
        
        upload.addFinishedListener(event -> {
            System.out.println("🏁 Upload finished: " + event.getFileName());
        });
        
        // Info text
        Span info = new Span(String.format("Fichiers acceptés: %s | Taille max: 10MB | Limite: %d fichier(s)", 
                                           acceptedTypes, maxFiles));
        info.getStyle().set("font-size", "12px")
                      .set("color", "var(--lumo-secondary-text-color)");
        container.add(info);
        
        upload.addSucceededListener(event -> {
            System.out.println("📤 Upload succeeded: " + event.getFileName());
            
            if (uploadedFiles.size() >= maxFiles) {
                System.out.println("⚠️ Max files reached, ignoring upload");
                return;
            }
            
            try (InputStream inputStream = buffer.getInputStream()) {
                byte[] fileBytes = inputStream.readAllBytes();
                String base64Content = Base64.getEncoder().encodeToString(fileBytes);
                
                Map<String, Object> fileInfo = new LinkedHashMap<>();
                fileInfo.put("filename", event.getFileName());
                fileInfo.put("contentType", event.getMIMEType());
                fileInfo.put("size", fileBytes.length);
                fileInfo.put("base64Content", base64Content);
                
                uploadedFiles.add(fileInfo);
                System.out.println("✅ File added to list: " + event.getFileName() + " (" + fileBytes.length + " bytes)");
                
                updateFilesDisplay(filesDisplay, uploadedFiles);
                
                // Update current values immediately
                currentValues.put(field.getName(), uploadedFiles);
                System.out.println("📊 Updated currentValues for field: " + field.getName() + " with " + uploadedFiles.size() + " files");
                
                // Disable upload if max reached
                if (uploadedFiles.size() >= maxFiles) {
                    upload.setVisible(false);
                    Span maxReachedMsg = new Span("Nombre maximum de fichiers atteint (" + maxFiles + ")");
                    maxReachedMsg.getStyle().set("color", "var(--lumo-secondary-text-color)")
                                           .set("font-size", "12px");
                    container.add(maxReachedMsg);
                }
                
                onChange.run();
                
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du téléchargement du fichier: " + e.getMessage());
                e.printStackTrace();
            }
        });
        
        upload.addFailedListener(event -> {
            System.err.println("❌ Upload failed: " + event.getFileName() + " - " + event.getReason().getMessage());
        });
        
        upload.addFileRejectedListener(event -> {
            System.err.println("❌ File rejected: " + event.getErrorMessage());
        });
        
        container.add(upload);
        
        return container;
    }
    
    private static void updateFilesDisplay(Div container, List<Map<String, Object>> files) {
        container.removeAll();
        
        for (int i = 0; i < files.size(); i++) {
            Map<String, Object> file = files.get(i);
            final int index = i;
            
            Div fileItem = new Div();
            fileItem.getStyle().set("display", "flex")
                              .set("align-items", "center")
                              .set("justify-content", "space-between")
                              .set("padding", "8px")
                              .set("margin-bottom", "4px")
                              .set("background-color", "var(--lumo-contrast-5pct)")
                              .set("border-radius", "var(--lumo-border-radius)");
            
            Div fileInfo = new Div();
            fileInfo.add(new Span(file.get("filename").toString()));
            
            long size = ((Number) file.get("size")).longValue();
            String sizeText = formatFileSize(size);
            Span sizeSpan = new Span(" (" + sizeText + ")");
            sizeSpan.getStyle().set("color", "var(--lumo-secondary-text-color)")
                               .set("font-size", "12px");
            fileInfo.add(sizeSpan);
            
            Button removeBtn = new Button(VaadinIcon.TRASH.create());
            removeBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_SMALL,
                                     com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY,
                                     com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);
            removeBtn.addClickListener(e -> {
                files.remove(index);
                System.out.println("🗑️ File removed, remaining: " + files.size());
                updateFilesDisplay(container, files);
            });
            
            fileItem.add(fileInfo, removeBtn);
            container.add(fileItem);
        }
    }
    
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
