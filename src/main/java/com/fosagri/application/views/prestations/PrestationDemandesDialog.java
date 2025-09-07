package com.fosagri.application.views.prestations;

import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.services.DemandePrestationService;
import com.fosagri.application.dto.DemandeViewDto;
import com.fosagri.application.dto.EnhancedDemandeViewDto;
import com.fosagri.application.views.demandes.DemandeDetailsDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class PrestationDemandesDialog extends Dialog {
    
    private final PrestationRef prestationRef;
    private final DemandePrestationService demandePrestationService;
    private final Grid<EnhancedDemandeViewDto> grid = new Grid<>(EnhancedDemandeViewDto.class, false);
    private final ListDataProvider<EnhancedDemandeViewDto> dataProvider;
    private final TextField filterText = new TextField();
    
    public PrestationDemandesDialog(PrestationRef prestationRef, DemandePrestationService demandePrestationService) {
        this.prestationRef = prestationRef;
        this.demandePrestationService = demandePrestationService;
        
        List<EnhancedDemandeViewDto> demandes = demandePrestationService.findByPrestationWithJsonFields(prestationRef);
        this.dataProvider = new ListDataProvider<>(demandes);
        
        setWidth("90%");
        setHeight("80%");
        setModal(true);
        setDraggable(true);
        setResizable(true);
        
        createContent();
    }
    
    private void createContent() {
        VerticalLayout content = new VerticalLayout();
        content.setSizeFull();
        content.setPadding(true);
        content.setSpacing(true);
        
        // Header
        H3 title = new H3("Demandes pour: " + prestationRef.getLabel());
        
        // Stats
        HorizontalLayout stats = createStats();
        
        // Toolbar with filter and export
        HorizontalLayout toolbar = createToolbar();
        
        // Grid
        configureGrid();
        
        content.add(title, stats, toolbar, filterText, grid);
        add(content);
        
        // Footer buttons
        createFooter();
    }
    
    private HorizontalLayout createStats() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.setSpacing(true);
        
        List<EnhancedDemandeViewDto> demandes = dataProvider.getItems().stream().collect(Collectors.toList());
        
        long total = demandes.size();
        long soumises = demandes.stream().filter(d -> "SOUMISE".equals(d.getStatut())).count();
        long enCours = demandes.stream().filter(d -> "EN_COURS".equals(d.getStatut())).count();
        long acceptees = demandes.stream().filter(d -> "ACCEPTEE".equals(d.getStatut())).count();
        long refusees = demandes.stream().filter(d -> "REFUSEE".equals(d.getStatut())).count();
        long terminees = demandes.stream().filter(d -> "TERMINEE".equals(d.getStatut())).count();
        
        layout.add(
            createStatBadge("Total", total, "var(--lumo-primary-color)"),
            createStatBadge("Soumises", soumises, "var(--lumo-warning-color)"),
            createStatBadge("En cours", enCours, "var(--lumo-primary-color)"),
            createStatBadge("Acceptées", acceptees, "var(--lumo-success-color)"),
            createStatBadge("Refusées", refusees, "var(--lumo-error-color)"),
            createStatBadge("Terminées", terminees, "var(--lumo-contrast-60pct)")
        );
        
        return layout;
    }
    
    private Span createStatBadge(String label, long count, String color) {
        Span badge = new Span(label + ": " + count);
        badge.getStyle()
            .set("padding", "4px 12px")
            .set("border-radius", "16px")
            .set("background-color", color)
            .set("color", "white")
            .set("font-size", "12px")
            .set("font-weight", "600")
            .set("white-space", "nowrap");
        return badge;
    }
    
    private HorizontalLayout createToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        toolbar.setWidthFull();
        
        // Left side - empty for now
        HorizontalLayout leftSide = new HorizontalLayout();
        
        // Right side - Export buttons
        HorizontalLayout rightSide = new HorizontalLayout();
        rightSide.setSpacing(true);
        
        Button exportCsvBtn = new Button("Export CSV", VaadinIcon.DOWNLOAD.create());
        exportCsvBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        exportCsvBtn.addClickListener(e -> downloadCsv());
        
        Button exportExcelBtn = new Button("Export Excel", VaadinIcon.FILE_TABLE.create());
        exportExcelBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        exportExcelBtn.addClickListener(e -> downloadCsv()); // Same as CSV for now
        
        rightSide.add(exportCsvBtn, exportExcelBtn);
        
        toolbar.add(leftSide, rightSide);
        return toolbar;
    }
    
    private void configureGrid() {
        grid.setDataProvider(dataProvider);
        grid.setSizeFull();
        
        grid.addColumn(EnhancedDemandeViewDto::getId).setHeader("ID").setAutoWidth(true);
        
        grid.addColumn(demande -> {
            if (demande.getAgent() != null) {
                return demande.getAgent().getNOM_AG() + " " + demande.getAgent().getPR_AG();
            }
            return "";
        }).setHeader("Agent").setAutoWidth(true);
        
        grid.addComponentColumn(demande -> {
            Span badge = new Span(getStatutLabel(demande.getStatut()));
            badge.getStyle()
                .set("padding", "4px 8px")
                .set("border-radius", "12px")
                .set("font-size", "12px")
                .set("font-weight", "600")
                .set("white-space", "nowrap");
            
            switch (demande.getStatut()) {
                case "SOUMISE":
                    badge.getStyle().set("background-color", "#fff3cd").set("color", "#856404");
                    break;
                case "EN_COURS":
                    badge.getStyle().set("background-color", "#d1ecf1").set("color", "#0c5460");
                    break;
                case "ACCEPTEE":
                    badge.getStyle().set("background-color", "#d4edda").set("color", "#155724");
                    break;
                case "REFUSEE":
                    badge.getStyle().set("background-color", "#f8d7da").set("color", "#721c24");
                    break;
                case "TERMINEE":
                    badge.getStyle().set("background-color", "#e2e3e5").set("color", "#383d41");
                    break;
                default:
                    badge.getStyle().set("background-color", "#e2e3e5").set("color", "#383d41");
            }
            
            return badge;
        }).setHeader("Statut").setAutoWidth(true);
        
        grid.addColumn(demande -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return demande.getDateDemande() != null ? sdf.format(demande.getDateDemande()) : "";
        }).setHeader("Date demande").setAutoWidth(true);
        
        grid.addColumn(demande -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return demande.getDateTraitement() != null ? sdf.format(demande.getDateTraitement()) : "";
        }).setHeader("Date traitement").setAutoWidth(true);
        
        // Add dynamic JSON field columns
        addJsonFieldColumns();
        
        grid.addColumn(EnhancedDemandeViewDto::getCommentaire).setHeader("Commentaire").setAutoWidth(true);
        
        // Action column for details
        grid.addComponentColumn(demande -> {
            Button detailsBtn = new Button("Détails", VaadinIcon.EYE.create());
            detailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            detailsBtn.addClickListener(e -> openDemandeDetails(demande));
            return detailsBtn;
        }).setHeader("Actions").setAutoWidth(true);
        
        // Configure filter
        filterText.setPlaceholder("Rechercher...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.setWidthFull();
        filterText.addValueChangeListener(e -> updateFilter());
    }
    
    private String getStatutLabel(String statut) {
        if (statut == null) return "Inconnu";
        switch (statut) {
            case "SOUMISE": return "Soumise";
            case "EN_COURS": return "En cours";
            case "ACCEPTEE": return "Acceptée";
            case "REFUSEE": return "Refusée";
            case "TERMINEE": return "Terminée";
            default: return statut;
        }
    }
    
    private void updateFilter() {
        String filterValue = filterText.getValue();
        if (filterValue == null || filterValue.trim().isEmpty()) {
            dataProvider.clearFilters();
        } else {
            dataProvider.setFilter(demande -> {
                String searchTerm = filterValue.toLowerCase();
                return (demande.getAgent() != null && 
                       (demande.getAgent().getNOM_AG().toLowerCase().contains(searchTerm) ||
                        demande.getAgent().getPR_AG().toLowerCase().contains(searchTerm))) ||
                       getStatutLabel(demande.getStatut()).toLowerCase().contains(searchTerm) ||
                       (demande.getCommentaire() != null && 
                        demande.getCommentaire().toLowerCase().contains(searchTerm));
            });
        }
    }
    
    private void downloadCsv() {
        String csvContent = generateCsvContent();
        String filename = "demandes-" + prestationRef.getLabel().replaceAll("\\s+", "_") + ".csv";
        
        // Create a data URL for download
        String dataUrl = "data:text/csv;charset=utf-8," + 
            java.net.URLEncoder.encode(csvContent, StandardCharsets.UTF_8);
        
        // Create a temporary anchor element for download
        getElement().executeJs(
            "const link = document.createElement('a');" +
            "link.href = $0;" +
            "link.download = $1;" +
            "link.style.display = 'none';" +
            "document.body.appendChild(link);" +
            "link.click();" +
            "document.body.removeChild(link);",
            dataUrl, filename
        );
    }
    
    private String generateCsvContent() {
        StringBuilder csv = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        java.util.Set<String> jsonKeys = demandePrestationService.extractCommonJsonKeys(prestationRef);
        
        // Headers
        csv.append("ID,Agent,Statut,Date demande,Date traitement,Commentaire");
        
        // Add JSON field headers
        for (String key : jsonKeys) {
            csv.append(",").append(formatFieldName(key));
        }
        
        csv.append(",Réponses JSON,Documents JSON\n");
        
        // Data
        List<EnhancedDemandeViewDto> demandes = dataProvider.getItems().stream().collect(Collectors.toList());
        for (EnhancedDemandeViewDto demande : demandes) {
            csv.append(demande.getId()).append(",");
            
            if (demande.getAgent() != null) {
                csv.append("\"").append(demande.getAgent().getNOM_AG()).append(" ")
                   .append(demande.getAgent().getPR_AG()).append("\"").append(",");
            } else {
                csv.append(",");
            }
            
            csv.append(getStatutLabel(demande.getStatut())).append(",");
            
            if (demande.getDateDemande() != null) {
                csv.append(sdf.format(demande.getDateDemande()));
            }
            csv.append(",");
            
            if (demande.getDateTraitement() != null) {
                csv.append(sdf.format(demande.getDateTraitement()));
            }
            csv.append(",");
            
            if (demande.getCommentaire() != null) {
                csv.append("\"").append(demande.getCommentaire().replace("\"", "\"\"")).append("\"");
            }
            csv.append(",");
            
            // Add JSON field values
            for (String key : jsonKeys) {
                Object value = demande.getJsonField(key);
                if (value != null) {
                    String valueStr = value.toString();
                    if (valueStr.contains(",") || valueStr.contains("\"") || valueStr.contains("\n")) {
                        csv.append("\"").append(valueStr.replace("\"", "\"\"")).append("\"");
                    } else {
                        csv.append(valueStr);
                    }
                }
                csv.append(",");
            }
            
            // Get full demande for JSON data
            DemandePrestation fullDemande = demandePrestationService.findById(demande.getId());
            if (fullDemande != null) {
                // Réponses JSON
                if (fullDemande.getReponseJson() != null) {
                    csv.append("\"").append(fullDemande.getReponseJson().replace("\"", "\"\"")).append("\"");
                }
                csv.append(",");
                
                // Documents JSON
                if (fullDemande.getDocumentsJson() != null) {
                    csv.append("\"").append(fullDemande.getDocumentsJson().replace("\"", "\"\"")).append("\"");
                }
            } else {
                csv.append(","); // Empty réponses JSON
            }
            csv.append("\n");
        }
        
        return csv.toString();
    }
    
    private void addJsonFieldColumns() {
        // Get common JSON keys for this prestation
        java.util.Set<String> jsonKeys = demandePrestationService.extractCommonJsonKeys(prestationRef);
        
        // Add a column for each JSON field
        for (String key : jsonKeys) {
            grid.addColumn(demande -> demande.getJsonFieldAsString(key))
                .setHeader(formatFieldName(key))
                .setAutoWidth(true)
                .setSortable(true);
        }
    }
    
    private String formatFieldName(String fieldName) {
        // Convert camelCase/snake_case to readable format
        String formatted = fieldName
            .replaceAll("([a-z])([A-Z])", "$1 $2")  // camelCase to spaces
            .replaceAll("_", " ")                    // snake_case to spaces
            .toLowerCase();
        
        // Convert to Title Case
        String[] words = formatted.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }
    
    private void openDemandeDetails(EnhancedDemandeViewDto demandeDto) {
        // Fetch full demande with JSON data
        DemandePrestation fullDemande = demandePrestationService.findById(demandeDto.getId());
        if (fullDemande != null) {
            DemandeDetailsDialog detailsDialog = new DemandeDetailsDialog(fullDemande);
            detailsDialog.open();
        }
    }
    
    private void createFooter() {
        Button closeBtn = new Button("Fermer");
        closeBtn.addClickListener(e -> close());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        
        HorizontalLayout footer = new HorizontalLayout(closeBtn);
        footer.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        footer.setPadding(true);
        
        getFooter().add(footer);
    }
}