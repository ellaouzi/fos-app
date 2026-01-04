package com.fosagri.application.views.dashboard;

import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.services.DemandePrestationService;
import com.fosagri.application.services.PrestationRefService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PageTitle("Analytics - Prestations & Demandes")
@Route("analytics-dashboard")
@Menu(order = 6, icon = LineAwesomeIconUrl.CHART_PIE_SOLID)
@RolesAllowed("ADMIN")
public class AnalyticsDashboardView extends VerticalLayout {
    
    private final DemandePrestationService demandeService;
    private final PrestationRefService prestationService;
    
    private VerticalLayout contentLayout;
    private Tabs mainTabs;
    private Tab overviewTab;
    private Tab prestationsTab;
    private Tab demandesTab;
    
    public AnalyticsDashboardView(DemandePrestationService demandeService, PrestationRefService prestationService) {
        this.demandeService = demandeService;
        this.prestationService = prestationService;
        
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        
        createHeader();
        createTabs();
        createContent();
        
        showOverview();
    }
    
    private void createHeader() {
        H2 title = new H2("Analytics - Informations Décisionnelles");
        title.getStyle().set("margin-bottom", "20px");
        
        Button refreshBtn = new Button("Actualiser", VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(com.vaadin.flow.component.button.ButtonVariant.LUMO_PRIMARY);
        refreshBtn.addClickListener(e -> refreshData());
        
        HorizontalLayout header = new HorizontalLayout(title, refreshBtn);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setWidthFull();
        
        add(header);
    }
    
    private void createTabs() {
        overviewTab = new Tab("Vue d'ensemble");
        prestationsTab = new Tab("Analyse Prestations");
        demandesTab = new Tab("Analyse Demandes");
        
        mainTabs = new Tabs(overviewTab, prestationsTab, demandesTab);
        mainTabs.setWidthFull();
        
        mainTabs.addSelectedChangeListener(event -> {
            Tab selectedTab = mainTabs.getSelectedTab();
            if (selectedTab == overviewTab) {
                showOverview();
            } else if (selectedTab == prestationsTab) {
                showPrestations();
            } else if (selectedTab == demandesTab) {
                showDemandes();
            }
        });
        
        add(mainTabs);
    }
    
    private void createContent() {
        contentLayout = new VerticalLayout();
        contentLayout.setSizeFull();
        contentLayout.setPadding(false);
        contentLayout.setSpacing(true);
        add(contentLayout);
    }
    
    private void showOverview() {
        contentLayout.removeAll();
        
        HorizontalLayout metricsLayout = createMetricsCards();
        contentLayout.add(metricsLayout);
        
        HorizontalLayout chartsLayout = new HorizontalLayout();
        chartsLayout.setSizeFull();
        chartsLayout.setSpacing(true);
        
        Div demandesChart = createDemandesStatusDisplay();
        demandesChart.setWidth("50%");
        
        Div prestationsChart = createPrestationsAvailabilityDisplay();
        prestationsChart.setWidth("50%");
        
        chartsLayout.add(demandesChart, prestationsChart);
        contentLayout.add(chartsLayout);
        
        contentLayout.add(createAlertsSection());
    }
    
    private void showPrestations() {
        contentLayout.removeAll();
        
        H3 title = new H3("Analyse des Prestations");
        contentLayout.add(title);
        
        HorizontalLayout prestationMetrics = createPrestationMetrics();
        contentLayout.add(prestationMetrics);
        
        Grid<PrestationRef> grid = createPrestationAnalysisGrid();
        contentLayout.add(grid);
    }
    
    private void showDemandes() {
        contentLayout.removeAll();
        
        H3 title = new H3("Analyse des Demandes");
        contentLayout.add(title);
        
        HorizontalLayout demandeMetrics = createDemandeMetrics();
        contentLayout.add(demandeMetrics);
        
        Div statusDisplay = createDetailedDemandesDisplay();
        contentLayout.add(statusDisplay);
    }
    
    private HorizontalLayout createMetricsCards() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        
        long totalPrestations = prestationService.count();
        long totalDemandes = demandeService.count();
        long demandesEnCours = demandeService.countByStatut("EN_COURS");
        long demandesSoumises = demandeService.countByStatut("SOUMISE");
        
        layout.add(
            createMetricCard("Total Prestations", String.valueOf(totalPrestations), "var(--lumo-primary-color)", VaadinIcon.CLIPBOARD),
            createMetricCard("Total Demandes", String.valueOf(totalDemandes), "var(--lumo-success-color)", VaadinIcon.FILE_TEXT),
            createMetricCard("En Cours", String.valueOf(demandesEnCours), "var(--lumo-warning-color)", VaadinIcon.CLOCK),
            createMetricCard("À Traiter", String.valueOf(demandesSoumises), "var(--lumo-error-color)", VaadinIcon.EXCLAMATION)
        );
        
        return layout;
    }
    
    private Div createMetricCard(String title, String value, String color, VaadinIcon icon) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("padding", "20px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
            .set("text-align", "center")
            .set("flex", "1");
        
        Div iconDiv = new Div();
        iconDiv.add(icon.create());
        iconDiv.getStyle()
            .set("color", color)
            .set("font-size", "2rem")
            .set("margin-bottom", "10px");
        
        H3 valueLabel = new H3(value);
        valueLabel.getStyle()
            .set("margin", "0")
            .set("color", color)
            .set("font-size", "2.5rem");
        
        Span titleLabel = new Span(title);
        titleLabel.getStyle()
            .set("color", "var(--lumo-secondary-text-color)")
            .set("font-weight", "500");
        
        card.add(iconDiv, valueLabel, titleLabel);
        return card;
    }
    
    private Div createDemandesStatusDisplay() {
        Div container = new Div();
        container.getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("padding", "20px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        
        H3 title = new H3("Répartition des Demandes par Statut");
        title.getStyle().set("text-align", "center").set("margin-bottom", "20px");
        container.add(title);
        
        VerticalLayout statusBars = new VerticalLayout();
        statusBars.setSpacing(true);
        statusBars.setPadding(false);
        
        long soumises = demandeService.countByStatut("SOUMISE");
        long enCours = demandeService.countByStatut("EN_COURS");
        long acceptees = demandeService.countByStatut("ACCEPTEE");
        long refusees = demandeService.countByStatut("REFUSEE");
        long terminees = demandeService.countByStatut("TERMINEE");
        long total = soumises + enCours + acceptees + refusees + terminees;
        
        if (total > 0) {
            statusBars.add(createStatusBar("Soumises", soumises, total, "var(--lumo-warning-color)"));
            statusBars.add(createStatusBar("En cours", enCours, total, "var(--lumo-primary-color)"));
            statusBars.add(createStatusBar("Acceptées", acceptees, total, "var(--lumo-success-color)"));
            statusBars.add(createStatusBar("Refusées", refusees, total, "var(--lumo-error-color)"));
            statusBars.add(createStatusBar("Terminées", terminees, total, "var(--lumo-contrast-60pct)"));
        } else {
            Span noData = new Span("Aucune donnée disponible");
            noData.getStyle().set("text-align", "center").set("color", "var(--lumo-secondary-text-color)");
            statusBars.add(noData);
        }
        
        container.add(statusBars);
        return container;
    }
    
    private Div createStatusBar(String label, long value, long total, String color) {
        Div barContainer = new Div();
        barContainer.getStyle().set("margin-bottom", "10px");
        
        HorizontalLayout labelLayout = new HorizontalLayout();
        labelLayout.setWidthFull();
        labelLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        Span labelSpan = new Span(label);
        Span valueSpan = new Span(value + " (" + String.format("%.1f%%", (value * 100.0) / total) + ")");
        labelLayout.add(labelSpan, valueSpan);
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setWidthFull();
        progressBar.setValue((double) value / total);
        progressBar.getStyle().set("--vaadin-progress-bar-color", color);
        
        barContainer.add(labelLayout, progressBar);
        return barContainer;
    }
    
    private Div createPrestationsAvailabilityDisplay() {
        Div container = new Div();
        container.getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("padding", "20px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        
        H3 title = new H3("État des Prestations");
        title.getStyle().set("text-align", "center").set("margin-bottom", "20px");
        container.add(title);
        
        List<PrestationRef> allPrestations = prestationService.findAll();
        
        long ouvertes = allPrestations.stream().filter(PrestationRef::isOpen).count();
        long fermees = allPrestations.stream().filter(p -> !p.isOpen()).count();
        
        LocalDateTime now = LocalDateTime.now();
        long aVenir = 0, enCours = 0, terminees = 0;
        
        for (PrestationRef p : allPrestations) {
            if (p.getDateDu() != null && p.getDateAu() != null) {
                LocalDateTime debut = new java.sql.Timestamp(p.getDateDu().getTime()).toLocalDateTime();
                LocalDateTime fin = new java.sql.Timestamp(p.getDateAu().getTime()).toLocalDateTime();
                
                if (now.isBefore(debut)) {
                    aVenir++;
                } else if (now.isAfter(fin)) {
                    terminees++;
                } else {
                    enCours++;
                }
            }
        }
        
        VerticalLayout statusBars = new VerticalLayout();
        statusBars.setSpacing(true);
        statusBars.setPadding(false);
        
        long total = ouvertes + fermees + aVenir + enCours + terminees;
        if (total > 0) {
            statusBars.add(createStatusBar("Ouvertes", ouvertes, total, "var(--lumo-success-color)"));
            statusBars.add(createStatusBar("Fermées", fermees, total, "var(--lumo-error-color)"));
            statusBars.add(createStatusBar("À venir", aVenir, total, "var(--lumo-primary-color)"));
            statusBars.add(createStatusBar("En cours", enCours, total, "var(--lumo-warning-color)"));
            statusBars.add(createStatusBar("Terminées", terminees, total, "var(--lumo-contrast-60pct)"));
        } else {
            Span noData = new Span("Aucune donnée disponible");
            noData.getStyle().set("text-align", "center").set("color", "var(--lumo-secondary-text-color)");
            statusBars.add(noData);
        }
        
        container.add(statusBars);
        return container;
    }
    
    private HorizontalLayout createPrestationMetrics() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        
        List<PrestationRef> prestations = prestationService.findAll();
        long prestationsActives = prestationService.findActivePrestations().size();
        long prestationsOuvertes = prestationService.findOpenPrestations().size();
        
        Map<String, Long> prestationDemandes = prestations.stream()
            .collect(Collectors.toMap(
                PrestationRef::getLabel,
                p -> demandeService.countTotalDemandes(p)
            ));
        
        String prestationPopulaire = prestationDemandes.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("Aucune");
        
        layout.add(
            createMetricCard("Prestations Actives", String.valueOf(prestationsActives), "var(--lumo-success-color)", VaadinIcon.CHECK),
            createMetricCard("Prestations Ouvertes", String.valueOf(prestationsOuvertes), "var(--lumo-primary-color)", VaadinIcon.UNLOCK),
            createMetricCard("Plus Populaire", prestationPopulaire.length() > 15 ? 
                prestationPopulaire.substring(0, 15) + "..." : prestationPopulaire, 
                "var(--lumo-warning-color)", VaadinIcon.STAR)
        );
        
        return layout;
    }
    
    private Grid<PrestationRef> createPrestationAnalysisGrid() {
        Grid<PrestationRef> grid = new Grid<>(PrestationRef.class, false);
        grid.setSizeFull();
        
        grid.addColumn(PrestationRef::getLabel).setHeader("Prestation").setAutoWidth(true);
        grid.addColumn(p -> demandeService.countTotalDemandes(p)).setHeader("Total Demandes").setAutoWidth(true);
        grid.addColumn(p -> demandeService.countSoumises(p)).setHeader("Soumises").setAutoWidth(true);
        grid.addColumn(p -> demandeService.countEnCours(p)).setHeader("En Cours").setAutoWidth(true);
        grid.addColumn(p -> demandeService.countAcceptees(p)).setHeader("Acceptées").setAutoWidth(true);
        grid.addColumn(p -> demandeService.countRefusees(p)).setHeader("Refusées").setAutoWidth(true);
        
        grid.addColumn(p -> {
            long total = demandeService.countTotalDemandes(p);
            if (total == 0) return "N/A";
            long acceptees = demandeService.countAcceptees(p);
            return String.format("%.1f%%", (acceptees * 100.0) / total);
        }).setHeader("Taux d'acceptation").setAutoWidth(true);
        
        grid.addColumn(p -> p.isOpen() ? "Ouverte" : "Fermée").setHeader("Statut").setAutoWidth(true);
        
        grid.setItems(prestationService.findAll());
        
        return grid;
    }
    
    private HorizontalLayout createDemandeMetrics() {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);
        
        long totalDemandes = demandeService.count();
        long demandesAcceptees = demandeService.countByStatut("ACCEPTEE");
        long demandesRefusees = demandeService.countByStatut("REFUSEE");
        
        double tauxAcceptation = totalDemandes > 0 ? (demandesAcceptees * 100.0) / totalDemandes : 0;
        double tauxRefus = totalDemandes > 0 ? (demandesRefusees * 100.0) / totalDemandes : 0;
        
        layout.add(
            createMetricCard("Taux Acceptation", String.format("%.1f%%", tauxAcceptation), "var(--lumo-success-color)", VaadinIcon.CHECK),
            createMetricCard("Taux Refus", String.format("%.1f%%", tauxRefus), "var(--lumo-error-color)", VaadinIcon.CLOSE),
            createMetricCard("En Attente", String.valueOf(demandeService.countByStatut("SOUMISE")), "var(--lumo-warning-color)", VaadinIcon.CLOCK)
        );
        
        return layout;
    }
    
    private Div createDetailedDemandesDisplay() {
        Div container = new Div();
        container.getStyle()
            .set("background", "white")
            .set("border-radius", "8px")
            .set("padding", "20px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        
        H3 title = new H3("Détail des Statuts de Demandes");
        title.getStyle().set("text-align", "center").set("margin-bottom", "20px");
        container.add(title);
        
        VerticalLayout statusBars = new VerticalLayout();
        statusBars.setSpacing(true);
        statusBars.setPadding(false);
        
        long soumises = demandeService.countByStatut("SOUMISE");
        long enCours = demandeService.countByStatut("EN_COURS");
        long acceptees = demandeService.countByStatut("ACCEPTEE");
        long refusees = demandeService.countByStatut("REFUSEE");
        long terminees = demandeService.countByStatut("TERMINEE");
        
        // Find max value for scaling
        long maxValue = Math.max(Math.max(soumises, enCours), Math.max(Math.max(acceptees, refusees), terminees));
        if (maxValue == 0) maxValue = 1; // Avoid division by zero
        
        statusBars.add(createDetailedStatusBar("Soumises", soumises, maxValue, "var(--lumo-warning-color)"));
        statusBars.add(createDetailedStatusBar("En Cours", enCours, maxValue, "var(--lumo-primary-color)"));
        statusBars.add(createDetailedStatusBar("Acceptées", acceptees, maxValue, "var(--lumo-success-color)"));
        statusBars.add(createDetailedStatusBar("Refusées", refusees, maxValue, "var(--lumo-error-color)"));
        statusBars.add(createDetailedStatusBar("Terminées", terminees, maxValue, "var(--lumo-contrast-60pct)"));
        
        container.add(statusBars);
        return container;
    }
    
    private Div createDetailedStatusBar(String label, long value, long maxValue, String color) {
        Div barContainer = new Div();
        barContainer.getStyle().set("margin-bottom", "15px");
        
        HorizontalLayout labelLayout = new HorizontalLayout();
        labelLayout.setWidthFull();
        labelLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        
        Span labelSpan = new Span(label);
        Span valueSpan = new Span(String.valueOf(value));
        valueSpan.getStyle().set("font-weight", "bold");
        labelLayout.add(labelSpan, valueSpan);
        
        ProgressBar progressBar = new ProgressBar();
        progressBar.setWidthFull();
        progressBar.setValue((double) value / maxValue);
        progressBar.getStyle().set("--vaadin-progress-bar-color", color);
        
        barContainer.add(labelLayout, progressBar);
        return barContainer;
    }
    
    private Component createAlertsSection() {
        VerticalLayout alertsLayout = new VerticalLayout();
        alertsLayout.setPadding(false);
        alertsLayout.setSpacing(false);
        
        H3 alertsTitle = new H3("Alertes et Notifications");
        alertsLayout.add(alertsTitle);
        
        List<PrestationRef> prestations = prestationService.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextWeek = now.plusDays(7);
        
        for (PrestationRef prestation : prestations) {
            if (prestation.getDateAu() != null) {
                LocalDateTime fin = new java.sql.Timestamp(prestation.getDateAu().getTime()).toLocalDateTime();
                if (fin.isAfter(now) && fin.isBefore(nextWeek)) {
                    Div alert = createAlertCard(
                        "Prestation expire bientôt: " + prestation.getLabel(),
                        "Expire le " + new SimpleDateFormat("dd/MM/yyyy").format(prestation.getDateAu()),
                        "var(--lumo-warning-color)"
                    );
                    alertsLayout.add(alert);
                }
            }
        }
        
        long demandesEnAttente = demandeService.countByStatut("SOUMISE");
        if (demandesEnAttente > 5) {
            Div alert = createAlertCard(
                "Demandes en attente nécessitent une attention",
                demandesEnAttente + " demandes soumises en attente de traitement",
                "var(--lumo-error-color)"
            );
            alertsLayout.add(alert);
        }
        
        long demandesEnCours = demandeService.countByStatut("EN_COURS");
        if (demandesEnCours > 10) {
            Div alert = createAlertCard(
                "Volume élevé de demandes en cours",
                demandesEnCours + " demandes sont actuellement en cours de traitement",
                "var(--lumo-primary-color)"
            );
            alertsLayout.add(alert);
        }
        
        return alertsLayout;
    }
    
    private Div createAlertCard(String title, String message, String color) {
        Div alert = new Div();
        alert.getStyle()
            .set("background", "white")
            .set("border-left", "4px solid " + color)
            .set("border-radius", "4px")
            .set("padding", "15px")
            .set("margin-bottom", "10px")
            .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        
        Span titleSpan = new Span(title);
        titleSpan.getStyle().set("font-weight", "bold").set("color", color);
        
        Span messageSpan = new Span(message);
        messageSpan.getStyle().set("color", "var(--lumo-secondary-text-color)").set("display", "block").set("margin-top", "5px");
        
        alert.add(titleSpan, messageSpan);
        return alert;
    }
    
    private void refreshData() {
        Tab selectedTab = mainTabs.getSelectedTab();
        if (selectedTab == overviewTab) {
            showOverview();
        } else if (selectedTab == prestationsTab) {
            showPrestations();
        } else if (selectedTab == demandesTab) {
            showDemandes();
        }
    }
}