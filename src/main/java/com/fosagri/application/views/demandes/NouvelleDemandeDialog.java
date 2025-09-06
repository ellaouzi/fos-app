package com.fosagri.application.views.demandes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.forms.FormRenderer;
import com.fosagri.application.forms.FormSchema;
import com.fosagri.application.forms.FormField;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.service.AdhEnfantService;
import com.fosagri.application.service.AdhConjointService;
import com.fosagri.application.services.DemandePrestationService;
import com.fosagri.application.services.PrestationRefService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NouvelleDemandeDialog extends Dialog {
    
    private final PrestationRefService prestationService;
    private final AdhAgentService agentService;
    private final AdhEnfantService enfantService;
    private final AdhConjointService conjointService;
    private final DemandePrestationService demandeService;
    private final List<SaveListener> saveListeners = new ArrayList<>();
    
    private ComboBox<AdhAgent> agentCombo;
    private ComboBox<PrestationRef> prestationCombo;
    private Div formContainer;
    private Component currentForm;
    private FormSchema currentFormSchema;
    private Map<String, Object> currentFormData;
    private Button submitButton;
    
    public NouvelleDemandeDialog(PrestationRefService prestationService,
                                AdhAgentService agentService,
                                AdhEnfantService enfantService,
                                AdhConjointService conjointService,
                                DemandePrestationService demandeService) {
        this.prestationService = prestationService;
        this.agentService = agentService;
        this.enfantService = enfantService;
        this.conjointService = conjointService;
        this.demandeService = demandeService;
        
        setHeaderTitle("Nouvelle Demande de Prestation");
        setWidth("900px");
        setHeight("700px");
        setModal(true);
        setResizable(true);
        
        createContent();
    }
    
    private void createContent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(true);
        
        // Section sélection
        H3 selectionTitle = new H3("Sélection");
        
        agentCombo = new ComboBox<>("Agent");
        agentCombo.setItems(agentService.findAll());
        agentCombo.setItemLabelGenerator(agent -> agent.getNOM_AG() + " " + agent.getPR_AG() + " (" + agent.getCIN_AG() + ")");
        agentCombo.setWidthFull();
        agentCombo.setRequired(true);
        
        prestationCombo = new ComboBox<>("Prestation");
        prestationCombo.setItems(prestationService.findOpenPrestations());
        prestationCombo.setItemLabelGenerator(PrestationRef::getLabel);
        prestationCombo.setWidthFull();
        prestationCombo.setRequired(true);
        prestationCombo.addValueChangeListener(e -> loadPrestationForm(e.getValue()));
        
        // Section formulaire dynamique
        H3 formTitle = new H3("Formulaire de demande");
        formContainer = new Div();
        formContainer.setWidthFull();
        
        // Vérification d'éligibilité
        agentCombo.addValueChangeListener(e -> checkEligibility());
        prestationCombo.addValueChangeListener(e -> checkEligibility());
        
        layout.add(selectionTitle, agentCombo, prestationCombo, formTitle, formContainer);
        
        // Boutons
        createButtons(layout);
        
        add(layout);
    }
    
    private void createButtons(VerticalLayout layout) {
        submitButton = new Button("Soumettre la demande", e -> submitDemande());
        submitButton.getStyle().set("background-color", "var(--lumo-primary-color)");
        submitButton.getStyle().set("color", "white");
        submitButton.setEnabled(false); // Désactivé par défaut
        
        Button cancelButton = new Button("Annuler", e -> close());
        
        HorizontalLayout buttonLayout = new HorizontalLayout(submitButton, cancelButton);
        buttonLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        
        layout.add(buttonLayout);
    }
    
    private void loadPrestationForm(PrestationRef prestation) {
        formContainer.removeAll();
        currentForm = null;
        currentFormSchema = null;
        currentFormData = null;
        
        if (prestation == null) {
            submitButton.setEnabled(false);
            return;
        }
        
        // Afficher les informations de la prestation
        VerticalLayout prestationInfo = new VerticalLayout();
        prestationInfo.setPadding(false);
        prestationInfo.setSpacing(true);
        prestationInfo.getStyle().set("border", "1px solid var(--lumo-contrast-20pct)")
                      .set("padding", "1rem")
                      .set("border-radius", "var(--lumo-border-radius)")
                      .set("background-color", "var(--lumo-contrast-5pct)");
        
        H4 infoTitle = new H4("📋 " + prestation.getLabel());
        infoTitle.getStyle().set("margin-top", "0");
        prestationInfo.add(infoTitle);
        
        if (prestation.getDescription() != null && !prestation.getDescription().trim().isEmpty()) {
            Div description = new Div();
            description.setText(prestation.getDescription());
            description.getStyle().set("margin-bottom", "1rem")
                      .set("color", "var(--lumo-secondary-text-color)");
            prestationInfo.add(description);
        }
        
        // Charger le formulaire dynamique si disponible
        if (prestation.getFormSchemaJson() != null && !prestation.getFormSchemaJson().trim().isEmpty()) {
            try {
                H4 formTitle = new H4("📝 Formulaire de demande");
                prestationInfo.add(formTitle);
                
                currentFormSchema = FormSchema.fromJson(prestation.getFormSchemaJson());
                AdhAgent selectedAgent = agentCombo.getValue();
                // Create form without callback - we'll collect answers manually during submission
                currentForm = FormRenderer.createForm(currentFormSchema, null, selectedAgent, enfantService, conjointService);
                prestationInfo.add(currentForm);
                
                submitButton.setEnabled(true);
                Notification.show("✅ Formulaire chargé avec succès ! Remplissez les champs et cliquez sur 'Soumettre'", 3000, Notification.Position.MIDDLE);
                
            } catch (Exception e) {
                Notification.show("❌ Erreur lors du chargement du formulaire: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
                Div errorDiv = new Div();
                errorDiv.setText("⚠️ Formulaire non disponible - veuillez contacter l'administrateur");
                errorDiv.getStyle().set("color", "var(--lumo-error-text-color)")
                          .set("background-color", "var(--lumo-error-color-10pct)")
                          .set("padding", "1rem")
                          .set("border-radius", "var(--lumo-border-radius)")
                          .set("border", "1px solid var(--lumo-error-color-50pct)");
                prestationInfo.add(errorDiv);
                submitButton.setEnabled(false);
            }
        } else {
            H4 simpleTitle = new H4("📝 Demande simple");
            prestationInfo.add(simpleTitle);
            
            Div noFormDiv = new Div();
            noFormDiv.setText("✓ Aucun formulaire spécifique requis pour cette prestation. Vous pouvez soumettre votre demande directement.");
            noFormDiv.getStyle().set("font-style", "italic")
                      .set("color", "var(--lumo-success-text-color)")
                      .set("background-color", "var(--lumo-success-color-10pct)")
                      .set("padding", "1rem")
                      .set("border-radius", "var(--lumo-border-radius)")
                      .set("border", "1px solid var(--lumo-success-color-50pct)");
            prestationInfo.add(noFormDiv);
            submitButton.setEnabled(true);
        }
        
        formContainer.add(prestationInfo);
    }
    
    private void checkEligibility() {
        AdhAgent agent = agentCombo.getValue();
        PrestationRef prestation = prestationCombo.getValue();
        
        if (agent != null && prestation != null) {
            boolean canApply = demandeService.canAgentApplyToPrestation(agent, prestation);
            if (!canApply) {
                String errorMessage = getEligibilityErrorMessage(agent, prestation);
                Notification.show("❌ " + errorMessage, 5000, Notification.Position.MIDDLE);
                formContainer.removeAll();
                currentForm = null;
                currentFormSchema = null;
                currentFormData = null;
                submitButton.setEnabled(false);
                
                // Afficher un message d'erreur dans le conteneur du formulaire
                Div errorContainer = new Div();
                errorContainer.setText("❌ " + errorMessage);
                errorContainer.getStyle().set("color", "var(--lumo-error-text-color)")
                          .set("background-color", "var(--lumo-error-color-10pct)")
                          .set("padding", "2rem")
                          .set("text-align", "center")
                          .set("border-radius", "var(--lumo-border-radius)")
                          .set("border", "1px solid var(--lumo-error-color-50pct)");
                formContainer.add(errorContainer);
            } else {
                // Recharger le formulaire si l'éligibilité est OK
                loadPrestationForm(prestation);
            }
        }
    }
    
    private String getEligibilityErrorMessage(AdhAgent agent, PrestationRef prestation) {
        // Vérifier si la prestation est ouverte
        if (!prestation.isOpen()) {
            return "La prestation '" + prestation.getLabel() + "' est fermée";
        }
        
        // Vérifier les dates
        java.util.Date now = new java.util.Date();
        if (prestation.getDateDu() != null && now.before(prestation.getDateDu())) {
            return "La prestation '" + prestation.getLabel() + "' n'est pas encore ouverte";
        }
        if (prestation.getDateAu() != null && now.after(prestation.getDateAu())) {
            return "La prestation '" + prestation.getLabel() + "' est expirée";
        }
        
        // Vérifier la limite de demandes
        if (prestation.getNombreLimit() > 0) {
            // Note: On ne peut pas facilement accéder au repository ici, 
            // donc on utilise un message générique
            return "La limite de demandes pour la prestation '" + prestation.getLabel() + "' pourrait être atteinte";
        }
        
        // Message générique si aucune condition spécifique n'est identifiée
        return "L'agent " + agent.getNOM_AG() + " " + agent.getPR_AG() + " n'est pas éligible pour cette prestation";
    }
    
    private void submitDemande() {
        AdhAgent agent = agentCombo.getValue();
        PrestationRef prestation = prestationCombo.getValue();
        
        if (agent == null) {
            Notification.show("Veuillez sélectionner un agent");
            return;
        }
        
        if (prestation == null) {
            Notification.show("Veuillez sélectionner une prestation");
            return;
        }
        
        if (!demandeService.canAgentApplyToPrestation(agent, prestation)) {
            Notification.show("Cet agent ne peut pas postuler à cette prestation");
            return;
        }
        
        try {
            String reponseJson = null;
            
            // Collect form data if we have a form and schema
            if (currentForm != null && currentFormSchema != null) {
                Map<String, Object> formAnswers = collectFormAnswers();
                if (formAnswers != null && !formAnswers.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    reponseJson = mapper.writeValueAsString(formAnswers);
                    System.out.println("📊 Soumission avec données collectées du formulaire: " + reponseJson);
                } else {
                    System.out.println("⚠️ Formulaire présent mais aucune donnée collectée");
                }
            } else {
                System.out.println("⚠️ Soumission sans formulaire (pas de données à collecter)");
            }
            
            DemandePrestation demande = demandeService.submitDemandePrestation(agent, prestation, reponseJson);
            
            saveListeners.forEach(listener -> listener.onSave(demande));
            close();
            
        } catch (Exception e) {
            Notification.show("Erreur lors de la soumission: " + e.getMessage());
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
            // Use reflection to access the FormRenderer's collectAnswers method
            // We need to find the field components within the form structure
            java.lang.reflect.Method collectMethod = FormRenderer.class.getDeclaredMethod("collectAnswers", 
                FormSchema.class, Map.class);
            collectMethod.setAccessible(true);
            
            // Extract field components from the form
            Map<String, Component> fieldComponents = extractFieldComponents(currentForm);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> answers = (Map<String, Object>) collectMethod.invoke(null, currentFormSchema, fieldComponents);
            
            return answers;
        } catch (Exception e) {
            System.err.println("Erreur lors de la collecte des réponses du formulaire: " + e.getMessage());
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    
    private Map<String, Component> extractFieldComponents(Component form) {
        Map<String, Component> fieldComponents = new HashMap<>();
        
        if (form instanceof VerticalLayout) {
            VerticalLayout container = (VerticalLayout) form;
            
            // Find FormLayout within the container
            container.getChildren().forEach(child -> {
                if (child instanceof com.vaadin.flow.component.formlayout.FormLayout) {
                    com.vaadin.flow.component.formlayout.FormLayout formLayout = 
                        (com.vaadin.flow.component.formlayout.FormLayout) child;
                    
                    // Map field components to their names based on schema order
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