package com.fosagri.application.views.demandes;

import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.service.AdhEnfantService;
import com.fosagri.application.service.AdhConjointService;
import com.fosagri.application.services.DemandePrestationService;
import com.fosagri.application.services.PrestationRefService;
import com.fosagri.application.services.PdfReportService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
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

@PageTitle("Demandes de Prestations")
@Route("demandes-prestations")
@Menu(order = 9, icon = LineAwesomeIconUrl.FILE_ALT_SOLID)
public class DemandesPrestationView extends VerticalLayout {
    
    private final DemandePrestationService demandeService;
    private final PrestationRefService prestationService;
    private final AdhAgentService agentService;
    private final AdhEnfantService enfantService;
    private final AdhConjointService conjointService;
    private final PdfReportService pdfReportService;
    
    private final Grid<DemandePrestation> grid = new Grid<>(DemandePrestation.class, false);
    private final ListDataProvider<DemandePrestation> dataProvider;
    private final TextField filterText = new TextField();
    private final ComboBox<String> statutFilter = new ComboBox<>("Filtrer par statut");
    
    public DemandesPrestationView(DemandePrestationService demandeService, 
                                  PrestationRefService prestationService,
                                  AdhAgentService agentService,
                                  AdhEnfantService enfantService,
                                  AdhConjointService conjointService,
                                  PdfReportService pdfReportService) {
        this.demandeService = demandeService;
        this.prestationService = prestationService;
        this.agentService = agentService;
        this.enfantService = enfantService;
        this.conjointService = conjointService;
        this.pdfReportService = pdfReportService;
        this.dataProvider = new ListDataProvider<>(new ArrayList<>());
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        H2 title = new H2("Demandes de Prestations");
        add(title);
        
        configureGrid();
        configureFilters();
        
        add(createToolbar(), createFilterLayout(), grid);
        
        refreshData();
    }
    
    private void configureGrid() {
        grid.setDataProvider(dataProvider);
        grid.setSizeFull();
        
        grid.addColumn(DemandePrestation::getId).setHeader("ID").setAutoWidth(true);
        
        grid.addColumn(demande -> {
            AdhAgent agent = demande.getAgent();
            return agent != null ? agent.getNOM_AG() + " " + agent.getPR_AG() : "";
        }).setHeader("Agent").setAutoWidth(true);
        
        grid.addColumn(demande -> {
            PrestationRef prestation = demande.getPrestation();
            return prestation != null ? prestation.getLabel() : "";
        }).setHeader("Prestation").setAutoWidth(true);
        
        grid.addColumn(demande -> {
            String statut = demande.getStatut();
            return getStatutLabel(statut);
        }).setHeader("Statut").setAutoWidth(true);
        
        grid.addColumn(demande -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return demande.getDateDemande() != null ? sdf.format(demande.getDateDemande()) : "";
        }).setHeader("Date demande").setAutoWidth(true);
        
        grid.addColumn(demande -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return demande.getDateTraitement() != null ? sdf.format(demande.getDateTraitement()) : "";
        }).setHeader("Date traitement").setAutoWidth(true);
        
        grid.addComponentColumn(demande -> {
            HorizontalLayout actions = new HorizontalLayout();
            
            Button viewBtn = new Button("Voir", VaadinIcon.EYE.create());
            viewBtn.addClickListener(e -> viewDemande(demande));
            
            Button editBtn = new Button("Traiter", VaadinIcon.EDIT.create());
            editBtn.addClickListener(e -> traiterDemande(demande));
            editBtn.setEnabled("SOUMISE".equals(demande.getStatut()) || "EN_COURS".equals(demande.getStatut()));
            
            Button pdfBtn = new Button("PDF", VaadinIcon.FILE_TEXT_O.create());
            pdfBtn.addClickListener(e -> generatePdfReport(demande));
            pdfBtn.getStyle().set("color", "green");
            
            Button deleteBtn = new Button("Supprimer", VaadinIcon.TRASH.create());
            deleteBtn.addClickListener(e -> confirmDelete(demande));
            deleteBtn.getStyle().set("color", "red");
            
            actions.add(viewBtn, editBtn, pdfBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setWidth("350px").setFlexGrow(0);
grid.setColumnReorderingAllowed(true);
    }
    
    private void configureFilters() {
        filterText.setPlaceholder("Rechercher...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> updateFilter());
        
        statutFilter.setItems("", "SOUMISE", "EN_COURS", "ACCEPTEE", "REFUSEE", "TERMINEE");
        statutFilter.setItemLabelGenerator(this::getStatutLabel);
        statutFilter.addValueChangeListener(e -> updateFilter());
    }
    
    private String getStatutLabel(String statut) {
        if (statut == null || statut.isEmpty()) return "Tous";
        switch (statut) {
            case "SOUMISE": return "Soumise";
            case "EN_COURS": return "En cours";
            case "ACCEPTEE": return "Acceptée";
            case "REFUSEE": return "Refusée";
            case "TERMINEE": return "Terminée";
            default: return statut;
        }
    }
    
    private HorizontalLayout createToolbar() {
        Button newDemandeBtn = new Button("Nouvelle demande", VaadinIcon.PLUS.create());
        newDemandeBtn.addClickListener(e -> ouvrirNouvelleDemandeDialog());
        
        Button refreshBtn = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshBtn.addClickListener(e -> refreshData());
        
        HorizontalLayout toolbar = new HorizontalLayout(newDemandeBtn, refreshBtn);
        toolbar.setAlignItems(FlexComponent.Alignment.BASELINE);
        return toolbar;
    }
    
    private HorizontalLayout createFilterLayout() {
        filterText.setWidth("300px");
        statutFilter.setWidth("200px");
        
        HorizontalLayout filterLayout = new HorizontalLayout(filterText, statutFilter);
        filterLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
        return filterLayout;
    }
    
    private void updateFilter() {
        String filterValue = filterText.getValue();
        String statutValue = statutFilter.getValue();
        
        dataProvider.setFilter(demande -> {
            boolean matchesText = true;
            boolean matchesStatut = true;
            
            if (filterValue != null && !filterValue.trim().isEmpty()) {
                String searchTerm = filterValue.toLowerCase();
                matchesText = (demande.getAgent() != null && 
                              (demande.getAgent().getNOM_AG().toLowerCase().contains(searchTerm) ||
                               demande.getAgent().getPR_AG().toLowerCase().contains(searchTerm))) ||
                             (demande.getPrestation() != null &&
                              demande.getPrestation().getLabel().toLowerCase().contains(searchTerm));
            }
            
            if (statutValue != null && !statutValue.trim().isEmpty()) {
                matchesStatut = statutValue.equals(demande.getStatut());
            }
            
            return matchesText && matchesStatut;
        });
    }
    
    private void refreshData() {
        dataProvider.getItems().clear();
        dataProvider.getItems().addAll(demandeService.findAll());
        dataProvider.refreshAll();
    }
    
    private void ouvrirNouvelleDemandeDialog() {
        NouvelleDemandeDialog dialog = new NouvelleDemandeDialog(
            prestationService, agentService, enfantService, conjointService, demandeService);
        dialog.addSaveListener(demande -> {
            refreshData();
            Notification.show("Demande soumise avec succès");
        });
        dialog.open();
    }
    
    private void viewDemande(DemandePrestation demande) {
        DemandeDetailsDialog dialog = new DemandeDetailsDialog(demande);
        dialog.open();
    }
    
    private void traiterDemande(DemandePrestation demande) {
        TraiterDemandeDialog dialog = new TraiterDemandeDialog(demande, demandeService);
        dialog.addSaveListener(updatedDemande -> {
            refreshData();
            Notification.show("Demande mise à jour avec succès");
        });
        dialog.open();
    }
    
    private void confirmDelete(DemandePrestation demande) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Confirmer la suppression");
        confirmDialog.setText(String.format("Êtes-vous sûr de vouloir supprimer la demande #%d de %s ?", 
            demande.getId(), 
            demande.getAgent() != null ? demande.getAgent().getNOM_AG() + " " + demande.getAgent().getPR_AG() : ""));
        
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Supprimer");
        confirmDialog.setConfirmButtonTheme("error primary");
        
        confirmDialog.addConfirmListener(e -> deleteDemande(demande));
        confirmDialog.open();
    }
    
    private void deleteDemande(DemandePrestation demande) {
        try {
            demandeService.deleteById(demande.getId());
            refreshData();
            Notification.show("Demande supprimée avec succès");
        } catch (Exception e) {
            Notification.show("Erreur lors de la suppression: " + e.getMessage());
        }
    }
    
    private void generatePdfReport(DemandePrestation demande) {
        try {
            // Use the REST API endpoint for PDF generation
            String pdfUrl = String.format("/api/pdf/demande/%d", demande.getId());
            
            getUI().ifPresent(ui -> ui.getPage().open(pdfUrl, "_blank"));
            
            Notification.show("Rapport PDF généré avec succès");
        } catch (Exception e) {
            Notification.show("Erreur lors de la génération du PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
