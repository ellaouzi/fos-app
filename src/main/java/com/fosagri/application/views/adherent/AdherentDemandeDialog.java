package com.fosagri.application.views.adherent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.components.SecureFileUploadComponent;
import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.forms.FormRenderer;
import com.fosagri.application.forms.FormSchema;
import com.fosagri.application.forms.FormField;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.service.AdhEnfantService;
import com.fosagri.application.service.AdhConjointService;
import com.fosagri.application.services.DemandePrestationService;
import com.fosagri.application.services.PrestationFieldService;
import com.fosagri.application.entities.PrestationField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdherentDemandeDialog extends Dialog {

    private final AdhAgent agent;
    private final PrestationRef prestation;
    private final DemandePrestationService demandeService;
    private final AdhEnfantService enfantService;
    private final AdhConjointService conjointService;
    private final PrestationFieldService prestationFieldService;
    private final List<SaveListener> saveListeners = new ArrayList<>();

    private Div formContainer;
    private Component currentForm;
    private FormSchema currentFormSchema;
    private Map<String, Object> formCurrentValues;
    private Button submitButton;
    private SecureFileUploadComponent fileUploadComponent;

    public AdherentDemandeDialog(AdhAgent agent,
                                  PrestationRef prestation,
                                  DemandePrestationService demandeService,
                                  AdhEnfantService enfantService,
                                  AdhConjointService conjointService,
                                  PrestationFieldService prestationFieldService) {
        this.agent = agent;
        this.prestation = prestation;
        this.demandeService = demandeService;
        this.enfantService = enfantService;
        this.conjointService = conjointService;
        this.prestationFieldService = prestationFieldService;

        setHeaderTitle("Nouvelle Demande");
        setWidth("700px");
        setMaxWidth("95vw");
        setHeight("auto");
        setMaxHeight("90vh");
        setModal(true);
        setResizable(true);
        setDraggable(true);

        createContent();
    }

    private void createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(false);
        layout.setSizeFull();
        layout.getStyle().set("gap", "0.75rem");

        // Header section: Agent + Prestation info side by side
        HorizontalLayout headerSection = new HorizontalLayout();
        headerSection.setWidthFull();
        headerSection.setSpacing(true);
        headerSection.getStyle().set("gap", "1rem");

        Div agentInfo = createAgentInfoSection();
        Div prestationInfo = createPrestationInfoSection();

        headerSection.add(agentInfo, prestationInfo);
        headerSection.setFlexGrow(1, agentInfo);
        headerSection.setFlexGrow(1, prestationInfo);
        layout.add(headerSection);

        // Form container
        formContainer = new Div();
        formContainer.setWidthFull();
        formContainer.getStyle()
            .set("overflow-y", "auto")
            .set("max-height", "50vh");
        layout.add(formContainer);

        // File attachment zone (only if prestation.isattached is true)
        if (prestation.isIsattached()) {
            Div attachmentSection = new Div();
            attachmentSection.setWidthFull();
            attachmentSection.getStyle()
                .set("margin-top", "1rem")
                .set("padding", "1rem")
                .set("background-color", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius)")
                .set("border", "1px dashed var(--lumo-contrast-20pct)");

            fileUploadComponent = new SecureFileUploadComponent(5, 10 * 1024 * 1024);
            attachmentSection.add(fileUploadComponent);
            layout.add(attachmentSection);
        }

        // Buttons - must be created before loadPrestationForm()
        createButtons(layout);

        // Load form after buttons are created
        loadPrestationForm();

        add(layout);
    }

    private Div createAgentInfoSection() {
        Div container = new Div();
        container.getStyle()
            .set("background-color", "var(--lumo-primary-color-10pct)")
            .set("padding", "0.75rem")
            .set("border-radius", "var(--lumo-border-radius)");

        Span icon = new Span("👤 ");
        icon.getStyle().set("font-size", "1rem");

        Span agentName = new Span(agent.getNOM_AG() + " " + agent.getPR_AG());
        agentName.getStyle().set("font-weight", "600");

        Span agentCin = new Span(" • CIN: " + agent.getCIN_AG());
        agentCin.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "0.9em");

        container.add(icon, agentName, agentCin);
        return container;
    }

    private Div createPrestationInfoSection() {
        Div container = new Div();
        container.getStyle()
            .set("background-color", "var(--lumo-contrast-5pct)")
            .set("padding", "0.75rem")
            .set("border-radius", "var(--lumo-border-radius)");

        Span icon = new Span("📋 ");
        icon.getStyle().set("font-size", "1rem");

        Span title = new Span(prestation.getLabel());
        title.getStyle().set("font-weight", "600");

        container.add(icon, title);

        if (prestation.getType() != null) {
            Span typeBadge = new Span(prestation.getType());
            typeBadge.getStyle()
                .set("background-color", "var(--lumo-primary-color-10pct)")
                .set("color", "var(--lumo-primary-text-color)")
                .set("padding", "2px 8px")
                .set("border-radius", "12px")
                .set("font-size", "11px")
                .set("margin-left", "0.5rem");
            container.add(typeBadge);
        }

        return container;
    }

    private void loadPrestationForm() {
        formContainer.removeAll();
        currentForm = null;
        currentFormSchema = null;
        formCurrentValues = null;

        try {
            // Load fields from prestation_field table
            System.out.println("🔍 Loading fields for prestation ID: " + prestation.getId() + " (" + prestation.getLabel() + ")");
            List<PrestationField> prestationFields = prestationFieldService.findByPrestationRefId(prestation.getId());
            System.out.println("📋 Found " + (prestationFields != null ? prestationFields.size() : 0) + " fields");

            if (prestationFields != null && !prestationFields.isEmpty()) {
                VerticalLayout formSection = new VerticalLayout();
                formSection.setPadding(false);
                formSection.setSpacing(false);
                formSection.getStyle().set("gap", "0.5rem");

                // Convert PrestationField to FormField and create schema
                List<FormField> formFields = new ArrayList<>();
                for (PrestationField pf : prestationFields) {
                    FormField ff = convertToFormField(pf);
                    System.out.println("🔄 Converted field: " + pf.getLabel() + " (" + pf.getFieldtype() + ") -> type=" + ff.getType() + ", name=" + ff.getName());
                    if (pf.getValeurs() != null && !pf.getValeurs().trim().isEmpty()) {
                        System.out.println("   Valeurs from DB: [" + pf.getValeurs() + "]");
                    }
                    if (ff.getOptions() != null && !ff.getOptions().isEmpty()) {
                        System.out.println("   Options in FormField: " + ff.getOptions().size());
                    } else {
                        System.out.println("   No options in FormField");
                    }
                    formFields.add(ff);
                }

                System.out.println("📝 Creating FormSchema with " + formFields.size() + " fields");
                currentFormSchema = new FormSchema();
                currentFormSchema.setTitle(prestation.getLabel());
                currentFormSchema.setFields(formFields);

                System.out.println("🎨 Calling FormRenderer.createFormWithValues...");
                System.out.println("   Agent: " + (agent != null ? agent.getNOM_AG() : "null"));
                System.out.println("   enfantService: " + (enfantService != null ? "present" : "null"));
                System.out.println("   conjointService: " + (conjointService != null ? "present" : "null"));

                FormRenderer.FormWithValues formWithValues = FormRenderer.createFormWithValues(
                    currentFormSchema, null, agent, enfantService, conjointService);
                currentForm = formWithValues.form;
                formCurrentValues = formWithValues.currentValues;

                System.out.println("✅ Form created successfully, adding to container");

                formSection.add(currentForm);
                formContainer.add(formSection);

                submitButton.setEnabled(true);

            } else {
                // No fields defined - allow submission without form
                Div noFormDiv = new Div();
                noFormDiv.setText("Aucun formulaire requis pour cette prestation. Cliquez sur 'Soumettre' pour envoyer votre demande.");
                noFormDiv.getStyle()
                    .set("color", "var(--lumo-success-text-color)")
                    .set("background-color", "var(--lumo-success-color-10pct)")
                    .set("padding", "1rem")
                    .set("border-radius", "var(--lumo-border-radius)")
                    .set("text-align", "center");
                formContainer.add(noFormDiv);
                submitButton.setEnabled(true);
            }

        } catch (Exception e) {
            Div errorDiv = new Div();
            errorDiv.setText("Erreur lors du chargement du formulaire: " + e.getMessage());
            errorDiv.getStyle()
                .set("color", "var(--lumo-error-text-color)")
                .set("background-color", "var(--lumo-error-color-10pct)")
                .set("padding", "1rem")
                .set("border-radius", "var(--lumo-border-radius)");
            formContainer.add(errorDiv);
            submitButton.setEnabled(false);
            e.printStackTrace();
        }
    }

    private FormField convertToFormField(PrestationField pf) {
        FormField ff = new FormField();
        ff.setName(pf.getColonne() != null ? pf.getColonne() : "field_" + pf.getId());
        ff.setLabel(pf.getLabel());
        ff.setType(mapFieldType(pf.getFieldtype()));
        ff.setRequired(pf.isRequired());
        ff.setOrder(pf.getOrdre());

        // Parse options if present (for select/checkbox types)
        if (pf.getValeurs() != null && !pf.getValeurs().trim().isEmpty()) {
            String[] optionValues = pf.getValeurs().split(",");
            List<com.fosagri.application.forms.FieldOption> options = new ArrayList<>();
            for (String val : optionValues) {
                String trimmed = val.trim();
                if (!trimmed.isEmpty()) {
                    com.fosagri.application.forms.FieldOption opt = new com.fosagri.application.forms.FieldOption();
                    opt.setLabel(trimmed);
                    opt.setValue(trimmed);
                    options.add(opt);
                }
            }
            ff.setOptions(options);
        }

        return ff;
    }

    private String mapFieldType(String fieldtype) {
        if (fieldtype == null) return "text";
        switch (fieldtype.toLowerCase()) {
            case "chiffre": return "number";
            case "texte": return "text";
            case "textarea": return "textarea";
            case "label": return "label";
            case "enfant": return "enfant";
            case "multioption": return "multiselect";
            case "checkbox": return "checkbox";
            case "date": return "date";
            case "option": return "select";
            case "conjoint": return "conjoint";
            case "file": return "file";
            // English fallbacks
            case "number": return "number";
            case "text": return "text";
            case "select": return "select";
            case "multiselect": return "multiselect";
            default: return "text";
        }
    }

    private void createButtons(VerticalLayout layout) {
        submitButton = new Button("Soumettre la demande", e -> submitDemande());
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        submitButton.setEnabled(false);

        Button cancelButton = new Button("Annuler", e -> close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttonLayout = new HorizontalLayout(submitButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttonLayout.setWidthFull();
        buttonLayout.getStyle().set("margin-top", "1rem");

        layout.add(buttonLayout);
    }

    private void submitDemande() {
        if (!demandeService.canAgentApplyToPrestation(agent, prestation)) {
            Notification notification = Notification.show(
                "Vous n'êtes pas éligible pour cette prestation",
                3000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        try {
            String reponseJson = null;
            String documentsJson = null;

            if (currentForm != null && currentFormSchema != null) {
                Map<String, Object> formAnswers = collectFormAnswers();
                if (formAnswers != null && !formAnswers.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    reponseJson = mapper.writeValueAsString(formAnswers);
                }
            }

            // Collect attached files if the upload component exists
            if (fileUploadComponent != null && fileUploadComponent.hasFiles()) {
                documentsJson = fileUploadComponent.getFilesAsJson();
            }

            DemandePrestation demande = demandeService.submitDemandePrestation(agent, prestation, reponseJson, documentsJson);

            Notification notification = Notification.show(
                "Demande soumise avec succès!",
                3000, Notification.Position.BOTTOM_END);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            saveListeners.forEach(listener -> listener.onSave(demande));
            close();

        } catch (Exception e) {
            Notification notification = Notification.show(
                "Erreur lors de la soumission: " + e.getMessage(),
                5000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public void addSaveListener(SaveListener listener) {
        saveListeners.add(listener);
    }

    private Map<String, Object> collectFormAnswers() {
        if (currentForm == null || currentFormSchema == null) {
            return null;
        }

        try {
            Map<String, Component> fieldComponents = extractFieldComponents(currentForm);

            if (formCurrentValues != null) {
                return FormRenderer.collectAnswers(currentFormSchema, fieldComponents, formCurrentValues);
            } else {
                return FormRenderer.collectAnswers(currentFormSchema, fieldComponents);
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la collecte des réponses: " + e.getMessage());
            return new HashMap<>();
        }
    }

    private Map<String, Component> extractFieldComponents(Component form) {
        Map<String, Component> fieldComponents = new HashMap<>();

        if (form instanceof VerticalLayout) {
            VerticalLayout container = (VerticalLayout) form;

            container.getChildren().forEach(child -> {
                if (child instanceof com.vaadin.flow.component.formlayout.FormLayout) {
                    com.vaadin.flow.component.formlayout.FormLayout formLayout =
                        (com.vaadin.flow.component.formlayout.FormLayout) child;

                    List<FormField> fields = new ArrayList<>(currentFormSchema.getFields());
                    fields.sort(Comparator
                        .comparing((FormField f) -> f.getOrder() == null ? Integer.MAX_VALUE : f.getOrder())
                        .thenComparing(FormField::getName, Comparator.nullsLast(String::compareTo)));

                    Component[] fieldComps = formLayout.getChildren().toArray(Component[]::new);

                    for (int i = 0; i < Math.min(fields.size(), fieldComps.length); i++) {
                        fieldComponents.put(fields.get(i).getName(), fieldComps[i]);
                    }
                }
            });
        }

        return fieldComponents;
    }

    @FunctionalInterface
    public interface SaveListener {
        void onSave(DemandePrestation demande);
    }
}
