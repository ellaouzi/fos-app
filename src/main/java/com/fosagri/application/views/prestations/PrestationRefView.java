package com.fosagri.application.views.prestations;

import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.services.PrestationRefService;
import com.fosagri.application.services.DemandePrestationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@PageTitle("Prestations")
@Route("prestations")
@Menu(order = 8, icon = LineAwesomeIconUrl.CLIPBOARD_LIST_SOLID)
public class PrestationRefView extends VerticalLayout {
    
    private final PrestationRefService service;
    private final DemandePrestationService demandePrestationService;
    private final Grid<PrestationRef> grid = new Grid<>(PrestationRef.class, false);
    private final ListDataProvider<PrestationRef> dataProvider;
    private final TextField filterText = new TextField();
    
    public PrestationRefView(PrestationRefService service, DemandePrestationService demandePrestationService) {
        this.service = service;
        this.demandePrestationService = demandePrestationService;
        this.dataProvider = new ListDataProvider<>(new ArrayList<>());
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        H2 title = new H2("Gestion des Prestations");
        add(title);
        
        configureGrid();
        configureFilter();
        
        add(createToolbar(), filterText, grid);
        
        refreshData();
    }
    
    private void configureGrid() {
        grid.setDataProvider(dataProvider);
        grid.setSizeFull();
        
        grid.addColumn(PrestationRef::getId).setHeader("ID").setAutoWidth(true);
        grid.addColumn(PrestationRef::getLabel).setHeader("Label").setAutoWidth(true);
        grid.addColumn(PrestationRef::getType).setHeader("Type").setAutoWidth(true);
        grid.addColumn(PrestationRef::getDescription).setHeader("Description").setAutoWidth(true);
        grid.addComponentColumn(prestationRef -> createStatusBadge(prestationRef.isOpen() ? "Ouverte" : "Fermée"))
            .setHeader("Statut").setAutoWidth(true);
        grid.addComponentColumn(prestationRef -> createAvailabilityBadge(getDateBasedStatus(prestationRef)))
            .setHeader("Disponibilité").setAutoWidth(true);
        grid.addColumn(prestationRef -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return prestationRef.getDateDu() != null ? sdf.format(prestationRef.getDateDu()) : "";
        }).setHeader("Date Début").setAutoWidth(true);
        grid.addColumn(prestationRef -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return prestationRef.getDateAu() != null ? sdf.format(prestationRef.getDateAu()) : "";
        }).setHeader("Date Fin").setAutoWidth(true);
        grid.addColumn(PrestationRef::getNombreLimit).setHeader("Limite").setAutoWidth(true);
        grid.addComponentColumn(prestationRef -> createDemandesCounterBadge(prestationRef))
            .setHeader("Demandes").setAutoWidth(true);
        
        grid.addComponentColumn(prestationRef -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            
            Button editBtn = new Button(VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
            editBtn.addClickListener(e -> openPrestationDialog(prestationRef));
            editBtn.getElement().setProperty("title", "Modifier");
            
            Button deleteBtn = new Button(VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> confirmAndDeletePrestation(prestationRef));
            deleteBtn.getElement().setProperty("title", "Supprimer");
            
            Button formBtn = new Button(VaadinIcon.FORM.create());
            formBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
            formBtn.addClickListener(e -> openFormBuilder(prestationRef));
            formBtn.getElement().setProperty("title", "Formulaire");
            
            actions.add(editBtn, deleteBtn, formBtn);
            return actions;
        }).setHeader("Actions").setWidth("150px").setFlexGrow(0);
    }
    
    private void configureFilter() {
        filterText.setPlaceholder("Rechercher par label...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateFilter());
        filterText.setWidthFull();
    }
    
    private HorizontalLayout createToolbar() {
        Button addBtn = new Button("Nouvelle Prestation", VaadinIcon.PLUS.create());
        addBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        addBtn.addClickListener(e -> {
            Notification.show("📝 Ouverture du formulaire de nouvelle prestation...", 
                             2000, Notification.Position.BOTTOM_CENTER);
            openPrestationDialog(null);
        });
        
        Button refreshBtn = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> {
            refreshData();
            Notification.show("🔄 Données actualisées", 2000, Notification.Position.BOTTOM_CENTER);
        });
        
        HorizontalLayout toolbar = new HorizontalLayout(addBtn, refreshBtn);
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);
        toolbar.setSpacing(true);
        return toolbar;
    }
    
    private void updateFilter() {
        String filterValue = filterText.getValue();
        if (filterValue == null || filterValue.trim().isEmpty()) {
            dataProvider.clearFilters();
        } else {
            dataProvider.setFilter(prestationRef -> 
                prestationRef.getLabel().toLowerCase().contains(filterValue.toLowerCase()) ||
                (prestationRef.getDescription() != null && 
                 prestationRef.getDescription().toLowerCase().contains(filterValue.toLowerCase()))
            );
        }
    }
    
    private void refreshData() {
        try {
            // Clear existing data
            dataProvider.getItems().clear();
            
            // Fetch fresh data from the service
            List<PrestationRef> prestations = service.findAll();
            
            // Add the fresh data to the provider
            dataProvider.getItems().addAll(prestations);
            
            // Refresh the data provider to update the grid
            dataProvider.refreshAll();
            
            // Update the grid to ensure it reflects the changes
            grid.getDataProvider().refreshAll();
            
        } catch (Exception e) {
            Notification.show("❌ Erreur lors du rafraîchissement des données: " + e.getMessage(), 
                             5000, Notification.Position.TOP_CENTER);
        }
    }
    
    private void openPrestationDialog(PrestationRef prestationRef) {
        PrestationRefFormDialog dialog = new PrestationRefFormDialog(prestationRef, service);
        dialog.addSaveListener(savedPrestation -> {
            // Clear any existing filters to ensure the new prestation is visible
            filterText.clear();
            
            // Refresh the grid data
            refreshData();
            
            // Select the newly created/updated prestation in the grid
            if (prestationRef == null) {
                // For new prestations, find and select it in the grid
                grid.getDataProvider().refreshAll();
                grid.select(savedPrestation);
                
                // Scroll to the new prestation (find its index in the list)
                List<PrestationRef> items = new ArrayList<>(dataProvider.getItems());
                int index = items.indexOf(savedPrestation);
                if (index >= 0) {
                    grid.scrollToIndex(index);
                }
            }
            
            String action = prestationRef == null ? "créée" : "mise à jour";
            Notification.show("✅ Prestation '" + savedPrestation.getLabel() + "' " + action + " avec succès", 
                             3000, Notification.Position.TOP_CENTER);
        });
        dialog.open();
    }
    
    private void openFormBuilder(PrestationRef prestationRef) {
        if (prestationRef == null) {
            Notification.show("Veuillez d'abord sauvegarder la prestation");
            return;
        }
        
        PrestationFormBuilderDialog dialog = new PrestationFormBuilderDialog(prestationRef, service);
        dialog.addSaveListener(updatedPrestation -> {
            refreshData();
            Notification.show("✅ Formulaire de la prestation '" + updatedPrestation.getLabel() + "' mis à jour avec succès", 
                             3000, Notification.Position.TOP_CENTER);
        });
        dialog.open();
    }
    
    private void confirmAndDeletePrestation(PrestationRef prestationRef) {
        com.vaadin.flow.component.confirmdialog.ConfirmDialog confirmDialog = 
            new com.vaadin.flow.component.confirmdialog.ConfirmDialog();
        confirmDialog.setHeader("Confirmer la suppression");
        confirmDialog.setText("Êtes-vous sûr de vouloir supprimer la prestation '" + 
                              prestationRef.getLabel() + "' ? Cette action est irréversible.");
        
        confirmDialog.setCancelable(true);
        confirmDialog.setCancelText("Annuler");
        confirmDialog.setConfirmText("Supprimer");
        confirmDialog.setConfirmButtonTheme("error primary");
        
        confirmDialog.addConfirmListener(e -> deletePrestation(prestationRef));
        confirmDialog.open();
    }
    
    private void deletePrestation(PrestationRef prestationRef) {
        try {
            service.deleteById(prestationRef.getId());
            refreshData();
            Notification.show("✅ Prestation '" + prestationRef.getLabel() + "' supprimée avec succès", 
                             3000, Notification.Position.TOP_CENTER);
        } catch (Exception e) {
            Notification.show("❌ Erreur lors de la suppression: " + e.getMessage(), 
                             5000, Notification.Position.TOP_CENTER);
        }
    }
    
    private String getDateBasedStatus(PrestationRef prestationRef) {
        Date dateDu = prestationRef.getDateDu();
        Date dateAu = prestationRef.getDateAu();
        
        // If no dates are set, check if it's open
        if (dateDu == null && dateAu == null) {
            return prestationRef.isOpen() ? "Disponible" : "Non disponible";
        }
        
        // If only one date is missing, we can't determine proper status
        if (dateDu == null || dateAu == null) {
            return prestationRef.isOpen() ? "Disponible" : "Non disponible";
        }
        
        // Convert Date to LocalDateTime (handling java.sql.Date)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime dateDebutLDT = new java.sql.Timestamp(dateDu.getTime()).toLocalDateTime();
        LocalDateTime dateFinLDT = new java.sql.Timestamp(dateAu.getTime()).toLocalDateTime();
        
        return getStatus(now, dateDebutLDT, dateFinLDT);
    }
    
    public String getStatus(LocalDateTime now, LocalDateTime dateDu, LocalDateTime dateAu) {
        // Convert to date only for comparison (ignore time)
        LocalDateTime nowDate = now.toLocalDate().atStartOfDay();
        LocalDateTime startDate = dateDu.toLocalDate().atStartOfDay();
        LocalDateTime endDate = dateAu.toLocalDate().atStartOfDay();
        
        if (nowDate.isBefore(startDate)) {
            return "À venir";
        } else if (startDate.isEqual(endDate)) {
            // Single day event
            if (nowDate.isEqual(startDate)) {
                return "Aujourd'hui";
            } else if (nowDate.isAfter(startDate)) {
                return "Terminée";
            } else {
                return "À venir";
            }
        } else if (nowDate.isEqual(startDate)) {
            return "Commence aujourd'hui";
        } else if (nowDate.isEqual(endDate)) {
            return "Se termine aujourd'hui";
        } else if (nowDate.isAfter(startDate) && nowDate.isBefore(endDate)) {
            return "En cours";
        } else {
            return "Terminée";
        }
    }
    
    private Span createStatusBadge(String status) {
        Span badge = new Span(status);
        badge.getStyle().set("padding", "4px 8px");
        badge.getStyle().set("border-radius", "12px");
        badge.getStyle().set("font-size", "12px");
        badge.getStyle().set("font-weight", "600");
        badge.getStyle().set("white-space", "nowrap");
        
        switch (status.toLowerCase()) {
            case "ouverte":
                badge.getStyle().set("background-color", "#d4edda");
                badge.getStyle().set("color", "#155724");
                break;
            case "fermée":
                badge.getStyle().set("background-color", "#f8d7da");
                badge.getStyle().set("color", "#721c24");
                break;
            default:
                badge.getStyle().set("background-color", "#e2e3e5");
                badge.getStyle().set("color", "#383d41");
        }
        
        return badge;
    }
    
    private Span createAvailabilityBadge(String availability) {
        Span badge = new Span(availability);
        badge.getStyle().set("padding", "4px 8px");
        badge.getStyle().set("border-radius", "12px");
        badge.getStyle().set("font-size", "12px");
        badge.getStyle().set("font-weight", "600");
        badge.getStyle().set("white-space", "nowrap");
        
        switch (availability.toLowerCase()) {
            case "en cours":
                badge.getStyle().set("background-color", "#d1ecf1");
                badge.getStyle().set("color", "#0c5460");
                break;
            case "à venir":
                badge.getStyle().set("background-color", "#d4edda");
                badge.getStyle().set("color", "#155724");
                break;
            case "aujourd'hui":
                badge.getStyle().set("background-color", "#d1ecf1");
                badge.getStyle().set("color", "#0c5460");
                break;
            case "commence aujourd'hui":
                badge.getStyle().set("background-color", "#d4edda");
                badge.getStyle().set("color", "#155724");
                break;
            case "se termine aujourd'hui":
                badge.getStyle().set("background-color", "#fff3cd");
                badge.getStyle().set("color", "#856404");
                break;
            case "terminée":
                badge.getStyle().set("background-color", "#f8d7da");
                badge.getStyle().set("color", "#721c24");
                break;
            case "disponible":
                badge.getStyle().set("background-color", "#d4edda");
                badge.getStyle().set("color", "#155724");
                break;
            case "non disponible":
                badge.getStyle().set("background-color", "#e2e3e5");
                badge.getStyle().set("color", "#383d41");
                break;
            default:
                badge.getStyle().set("background-color", "#e2e3e5");
                badge.getStyle().set("color", "#383d41");
        }
        
        return badge;
    }
    
    private Span createDemandesCounterBadge(PrestationRef prestationRef) {
        long totalDemandes = 0;
        long soumises = 0, enCours = 0, acceptees = 0, refusees = 0, terminees = 0;
        
        try {
            // Use count queries instead of loading entities to avoid BLOB issues
            totalDemandes = demandePrestationService.countTotalDemandes(prestationRef);
            soumises = demandePrestationService.countSoumises(prestationRef);
            enCours = demandePrestationService.countEnCours(prestationRef);
            acceptees = demandePrestationService.countAcceptees(prestationRef);
            refusees = demandePrestationService.countRefusees(prestationRef);
            terminees = demandePrestationService.countTerminees(prestationRef);
        } catch (Exception e) {
            // Log error and show error indicator
            System.err.println("❌ Erreur lors du chargement des demandes pour prestation " + prestationRef.getId() + ": " + e.getMessage());
            return createErrorBadge();
        }
        
        Span badge = new Span(String.valueOf(totalDemandes));
        badge.getStyle().set("padding", "4px 8px");
        badge.getStyle().set("border-radius", "50%");
        badge.getStyle().set("font-size", "12px");
        badge.getStyle().set("font-weight", "700");
        badge.getStyle().set("min-width", "24px");
        badge.getStyle().set("text-align", "center");
        badge.getStyle().set("display", "inline-block");
        
        // Color based on total number of demandes
        if (totalDemandes == 0) {
            badge.getStyle().set("background-color", "#e2e3e5");
            badge.getStyle().set("color", "#6c757d");
        } else if (soumises > 0 || enCours > 0) {
            // Has pending or in-progress demandes - orange
            badge.getStyle().set("background-color", "#fff3cd");
            badge.getStyle().set("color", "#856404");
        } else if (acceptees > 0) {
            // Has accepted demandes - green
            badge.getStyle().set("background-color", "#d4edda");
            badge.getStyle().set("color", "#155724");
        } else {
            // Only refused/completed demandes - blue
            badge.getStyle().set("background-color", "#d1ecf1");
            badge.getStyle().set("color", "#0c5460");
        }
        
        // Add tooltip with detailed breakdown
        String tooltip = String.format("Total: %d\nSoumises: %d\nEn cours: %d\nAcceptées: %d\nRefusées: %d\nTerminées: %d", 
            totalDemandes, soumises, enCours, acceptees, refusees, terminees);
        badge.getElement().setProperty("title", tooltip);
        
        return badge;
    }
    
    private Span createErrorBadge() {
        Span badge = new Span("!");
        badge.getStyle().set("padding", "4px 8px");
        badge.getStyle().set("border-radius", "50%");
        badge.getStyle().set("font-size", "12px");
        badge.getStyle().set("font-weight", "700");
        badge.getStyle().set("min-width", "24px");
        badge.getStyle().set("text-align", "center");
        badge.getStyle().set("display", "inline-block");
        badge.getStyle().set("background-color", "#f8d7da");
        badge.getStyle().set("color", "#721c24");
        badge.getElement().setProperty("title", "Erreur lors du chargement des demandes");
        return badge;
    }
}