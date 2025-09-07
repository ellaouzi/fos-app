package com.fosagri.application.views.reclamations;

import com.fosagri.application.entities.Reclamation;
import com.fosagri.application.entities.Reclamation.StatutReclamation;
import com.fosagri.application.entities.Reclamation.TypeReclamation;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.services.ReclamationService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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
import java.util.ArrayList;
import java.util.List;

@PageTitle("Réclamations")
@Route("reclamations")
@Menu(order = 10, icon = LineAwesomeIconUrl.EXCLAMATION_TRIANGLE_SOLID)
public class ReclamationsView extends VerticalLayout {
    
    private final ReclamationService reclamationService;
    private final AdhAgentService agentService;
    
    private final Grid<Reclamation> grid = new Grid<>(Reclamation.class, false);
    private final ListDataProvider<Reclamation> dataProvider;
    private final TextField filterText = new TextField();
    private final ComboBox<StatutReclamation> statutFilter = new ComboBox<>("Filtrer par statut");
    private final ComboBox<TypeReclamation> typeFilter = new ComboBox<>("Filtrer par type");
    
    public ReclamationsView(ReclamationService reclamationService, AdhAgentService agentService) {
        this.reclamationService = reclamationService;
        this.agentService = agentService;
        this.dataProvider = new ListDataProvider<>(new ArrayList<>());
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        H2 title = new H2("Gestion des Réclamations");
        add(title);
        
        configureGrid();
        configureFilters();
        
        add(createToolbar(), createFilterLayout(), grid);
        
        refreshData();
    }
    
    private void configureGrid() {
        grid.setDataProvider(dataProvider);
        grid.setSizeFull();
        
        grid.addColumn(Reclamation::getId).setHeader("ID").setAutoWidth(true);
        
        grid.addColumn(reclamation -> {
            try {
                if (reclamation.getAgent() != null) {
                    return reclamation.getAgent().getNOM_AG() + " " + reclamation.getAgent().getPR_AG();
                }
            } catch (Exception e) {
                // Handle lazy loading issues gracefully
                return "Agent (ID: " + (reclamation.getAgent() != null ? "Présent" : "N/A") + ")";
            }
            return "N/A";
        }).setHeader("Agent").setAutoWidth(true);
        
        grid.addColumn(Reclamation::getObjet).setHeader("Objet").setAutoWidth(true);
        
        grid.addColumn(reclamation -> reclamation.getType().getLabel())
                .setHeader("Type").setAutoWidth(true);
        
        grid.addComponentColumn(this::createStatusBadge).setHeader("Statut").setAutoWidth(true);
        
        grid.addColumn(reclamation -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return reclamation.getDateCreation() != null ? sdf.format(reclamation.getDateCreation()) : "";
        }).setHeader("Date création").setAutoWidth(true);
        
        grid.addComponentColumn(reclamation -> {
            HorizontalLayout actions = new HorizontalLayout();
            
            Button viewBtn = new Button("Voir", VaadinIcon.EYE.create());
            viewBtn.addClickListener(e -> viewReclamation(reclamation));
            
            Button processBtn = new Button("Traiter", VaadinIcon.EDIT.create());
            processBtn.addClickListener(e -> processReclamation(reclamation));
            processBtn.setEnabled(reclamation.getStatut() != StatutReclamation.FERMEE && 
                                 reclamation.getStatut() != StatutReclamation.REJETEE);
            
            Button deleteBtn = new Button("Supprimer", VaadinIcon.TRASH.create());
            deleteBtn.addClickListener(e -> confirmDelete(reclamation));
            deleteBtn.getStyle().set("color", "red");
            
            actions.add(viewBtn, processBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setWidth("300px").setFlexGrow(0);
    }
    
    private Span createStatusBadge(Reclamation reclamation) {
        Span badge = new Span(reclamation.getStatut().getLabel());
        badge.getElement().getThemeList().add("badge");
        
        switch (reclamation.getStatut()) {
            case NOUVELLE:
                badge.getElement().getThemeList().add("primary");
                break;
            case EN_COURS:
                badge.getElement().getThemeList().add("contrast");
                break;
            case RESOLUE:
                badge.getElement().getThemeList().add("success");
                break;
            case FERMEE:
                badge.getElement().getThemeList().add("normal");
                break;
            case REJETEE:
                badge.getElement().getThemeList().add("error");
                break;
        }
        
        return badge;
    }
    
    private void configureFilters() {
        filterText.setPlaceholder("Rechercher...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateFilter());
        
        statutFilter.setItems(StatutReclamation.values());
        statutFilter.setItemLabelGenerator(StatutReclamation::getLabel);
        statutFilter.addValueChangeListener(e -> updateFilter());
        
        typeFilter.setItems(TypeReclamation.values());
        typeFilter.setItemLabelGenerator(TypeReclamation::getLabel);
        typeFilter.addValueChangeListener(e -> updateFilter());
    }
    
    private HorizontalLayout createToolbar() {
        Button newReclamationBtn = new Button("Nouvelle réclamation", VaadinIcon.PLUS.create());
        newReclamationBtn.addClickListener(e -> openNewReclamationDialog());
        
        Button refreshBtn = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshBtn.addClickListener(e -> refreshData());
        
        HorizontalLayout toolbar = new HorizontalLayout(newReclamationBtn, refreshBtn);
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);
        return toolbar;
    }
    
    private HorizontalLayout createFilterLayout() {
        filterText.setWidth("300px");
        statutFilter.setWidth("200px");
        typeFilter.setWidth("200px");
        
        HorizontalLayout filterLayout = new HorizontalLayout(filterText, statutFilter, typeFilter);
        filterLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        return filterLayout;
    }
    
    private void updateFilter() {
        String filterValue = filterText.getValue();
        StatutReclamation statutValue = statutFilter.getValue();
        TypeReclamation typeValue = typeFilter.getValue();
        
        dataProvider.setFilter(reclamation -> {
            boolean matchesText = true;
            boolean matchesStatut = true;
            boolean matchesType = true;
            
            if (filterValue != null && !filterValue.trim().isEmpty()) {
                String searchTerm = filterValue.toLowerCase();
                matchesText = reclamation.getObjet().toLowerCase().contains(searchTerm) ||
                             (reclamation.getDetail() != null && 
                              reclamation.getDetail().toLowerCase().contains(searchTerm));
                
                // Try to search agent properties, but handle lazy loading gracefully
                if (!matchesText && reclamation.getAgent() != null) {
                    try {
                        matchesText = reclamation.getAgent().getNOM_AG().toLowerCase().contains(searchTerm) ||
                                     reclamation.getAgent().getPR_AG().toLowerCase().contains(searchTerm);
                    } catch (Exception e) {
                        // Skip agent search if lazy loading fails
                        matchesText = false;
                    }
                }
            }
            
            if (statutValue != null) {
                matchesStatut = statutValue.equals(reclamation.getStatut());
            }
            
            if (typeValue != null) {
                matchesType = typeValue.equals(reclamation.getType());
            }
            
            return matchesText && matchesStatut && matchesType;
        });
    }
    
    private void refreshData() {
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(reclamationService.findAll());
        dataProvider.refreshAll();
    }
    
    private void openNewReclamationDialog() {
        NouvelleReclamationDialog dialog = new NouvelleReclamationDialog(agentService, reclamationService);
        dialog.addSaveListener(reclamation -> {
            refreshData();
            Notification.show("Réclamation créée avec succès");
        });
        dialog.open();
    }
    
    private void viewReclamation(Reclamation reclamation) {
        ReclamationDetailsDialog dialog = new ReclamationDetailsDialog(reclamation, reclamationService);
        dialog.open();
    }
    
    private void processReclamation(Reclamation reclamation) {
        TraiterReclamationDialog dialog = new TraiterReclamationDialog(reclamation, reclamationService);
        dialog.addSaveListener(updatedReclamation -> {
            refreshData();
            Notification.show("Réclamation mise à jour avec succès");
        });
        dialog.open();
    }
    
    private void confirmDelete(Reclamation reclamation) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Confirmer la suppression");
        confirmDialog.setText(String.format("Êtes-vous sûr de vouloir supprimer la réclamation #%d ?", 
            reclamation.getId()));
        
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Supprimer");
        confirmDialog.setConfirmButtonTheme("error primary");
        
        confirmDialog.addConfirmListener(e -> deleteReclamation(reclamation));
        confirmDialog.open();
    }
    
    private void deleteReclamation(Reclamation reclamation) {
        try {
            reclamationService.deleteById(reclamation.getId());
            refreshData();
            Notification.show("Réclamation supprimée avec succès");
        } catch (Exception e) {
            Notification.show("Erreur lors de la suppression: " + e.getMessage());
        }
    }
}