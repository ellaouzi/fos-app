package com.fosagri.application.views.prestations;

import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.services.PrestationRefService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
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
import java.util.ArrayList;
import java.util.List;

@PageTitle("Prestations")
@Route("prestations")
@Menu(order = 8, icon = LineAwesomeIconUrl.CLIPBOARD_LIST_SOLID)
public class PrestationRefView extends VerticalLayout {
    
    private final PrestationRefService service;
    private final Grid<PrestationRef> grid = new Grid<>(PrestationRef.class, false);
    private final ListDataProvider<PrestationRef> dataProvider;
    private final TextField filterText = new TextField();
    
    public PrestationRefView(PrestationRefService service) {
        this.service = service;
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
        grid.addColumn(prestationRef -> prestationRef.isOpen() ? "Ouverte" : "Fermée")
            .setHeader("Statut").setAutoWidth(true);
        grid.addColumn(prestationRef -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return prestationRef.getDateDu() != null ? sdf.format(prestationRef.getDateDu()) : "";
        }).setHeader("Date Début").setAutoWidth(true);
        grid.addColumn(prestationRef -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return prestationRef.getDateAu() != null ? sdf.format(prestationRef.getDateAu()) : "";
        }).setHeader("Date Fin").setAutoWidth(true);
        grid.addColumn(PrestationRef::getNombreLimit).setHeader("Limite").setAutoWidth(true);
        
        grid.addComponentColumn(prestationRef -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);
            
            Button editBtn = new Button("Modifier", VaadinIcon.EDIT.create());
            editBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
            editBtn.addClickListener(e -> openPrestationDialog(prestationRef));
            editBtn.getStyle().set("font-size", "12px");
            
            Button deleteBtn = new Button("Supprimer", VaadinIcon.TRASH.create());
            deleteBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> confirmAndDeletePrestation(prestationRef));
            deleteBtn.getStyle().set("font-size", "12px");
            
            Button formBtn = new Button("Formulaire", VaadinIcon.FORM.create());
            formBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_TERTIARY);
            formBtn.addClickListener(e -> openFormBuilder(prestationRef));
            formBtn.getStyle().set("font-size", "12px");
            
            actions.add(editBtn, deleteBtn, formBtn);
            return actions;
        }).setHeader("Actions").setWidth("350px").setFlexGrow(0);
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
}