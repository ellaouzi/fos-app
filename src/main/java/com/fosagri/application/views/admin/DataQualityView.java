package com.fosagri.application.views.admin;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.model.AdhEnfant;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.service.AdhConjointService;
import com.fosagri.application.service.AdhEnfantService;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Qualité des Données")
@Route(value = "admin/data-quality", layout = MainLayout.class)
@Menu(order = 8, icon = LineAwesomeIconUrl.EXCLAMATION_TRIANGLE_SOLID)
@RolesAllowed("ADMIN")
public class DataQualityView extends VerticalLayout {

    private final AdhAgentService agentService;
    private final AdhConjointService conjointService;
    private final AdhEnfantService enfantService;

    // Issue counts
    private Span duplicateCinCount;
    private Span sameNameCount;
    private Span manyConjointsCount;
    private Span manyEnfantsCount;
    private Span missingDataCount;

    // Data holders
    private List<DataIssue> duplicateCinIssues = new ArrayList<>();
    private List<DataIssue> sameNameIssues = new ArrayList<>();
    private List<DataIssue> manyConjointsIssues = new ArrayList<>();
    private List<DataIssue> manyEnfantsIssues = new ArrayList<>();
    private List<DataIssue> missingDataIssues = new ArrayList<>();

    public DataQualityView(AdhAgentService agentService, AdhConjointService conjointService, AdhEnfantService enfantService) {
        this.agentService = agentService;
        this.conjointService = conjointService;
        this.enfantService = enfantService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(createHeader());
        add(createStatsSection());
        add(createTabSheet());

        // Load data
        analyzeData();
    }

    private Component createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setPadding(false);
        titleSection.setSpacing(false);

        H2 title = new H2("Contrôle Qualité des Données");
        title.getStyle().set("margin", "0").set("color", "#1e293b");

        Span subtitle = new Span("Détection des anomalies et données incohérentes");
        subtitle.getStyle().set("color", "#64748b").set("font-size", "0.9rem");

        titleSection.add(title, subtitle);

        Button refreshBtn = new Button("Actualiser l'analyse", VaadinIcon.REFRESH.create());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        refreshBtn.addClickListener(e -> analyzeData());

        header.add(titleSection, refreshBtn);
        return header;
    }

    private Component createStatsSection() {
        HorizontalLayout stats = new HorizontalLayout();
        stats.setWidthFull();
        stats.getStyle().set("flex-wrap", "wrap").set("gap", "1rem");

        duplicateCinCount = new Span("0");
        sameNameCount = new Span("0");
        manyConjointsCount = new Span("0");
        manyEnfantsCount = new Span("0");
        missingDataCount = new Span("0");

        stats.add(
            createStatCard("CIN Dupliqués", duplicateCinCount, VaadinIcon.COPY_O, "#dc2626"),
            createStatCard("Noms Identiques", sameNameCount, VaadinIcon.USERS, "#f59e0b"),
            createStatCard("Plusieurs Conjoints", manyConjointsCount, VaadinIcon.USER, "#8b5cf6"),
            createStatCard("Beaucoup d'Enfants", manyEnfantsCount, VaadinIcon.CHILD, "#10b981"),
            createStatCard("Données Manquantes", missingDataCount, VaadinIcon.EXCLAMATION, "#6366f1")
        );

        return stats;
    }

    private Component createStatCard(String label, Span valueSpan, VaadinIcon iconType, String color) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("padding", "1rem 1.25rem")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
            .set("border-left", "4px solid " + color)
            .set("display", "flex")
            .set("align-items", "center")
            .set("gap", "0.75rem")
            .set("min-width", "160px")
            .set("flex", "1");

        Div iconContainer = new Div();
        iconContainer.getStyle()
            .set("width", "40px")
            .set("height", "40px")
            .set("border-radius", "10px")
            .set("background", color + "15")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center");

        Icon icon = iconType.create();
        icon.setSize("20px");
        icon.getStyle().set("color", color);
        iconContainer.add(icon);

        Div textContainer = new Div();

        valueSpan.getStyle()
            .set("font-size", "1.5rem")
            .set("font-weight", "700")
            .set("color", color)
            .set("display", "block")
            .set("line-height", "1");

        Span labelSpan = new Span(label);
        labelSpan.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.75rem")
            .set("display", "block");

        textContainer.add(valueSpan, labelSpan);
        card.add(iconContainer, textContainer);

        return card;
    }

    private Component createTabSheet() {
        TabSheet tabSheet = new TabSheet();
        tabSheet.setSizeFull();
        tabSheet.getStyle().set("background", "white").set("border-radius", "12px");

        tabSheet.add(createTab("CIN Dupliqués", VaadinIcon.COPY_O), createDuplicateCinContent());
        tabSheet.add(createTab("Noms Identiques", VaadinIcon.USERS), createSameNameContent());
        tabSheet.add(createTab("Plusieurs Conjoints", VaadinIcon.USER), createManyConjointsContent());
        tabSheet.add(createTab("Beaucoup d'Enfants", VaadinIcon.CHILD), createManyEnfantsContent());
        tabSheet.add(createTab("Données Manquantes", VaadinIcon.EXCLAMATION), createMissingDataContent());

        return tabSheet;
    }

    private Tab createTab(String label, VaadinIcon iconType) {
        HorizontalLayout content = new HorizontalLayout();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(false);
        content.getStyle().set("gap", "0.5rem");

        Icon icon = iconType.create();
        icon.setSize("16px");

        content.add(icon, new Span(label));
        return new Tab(content);
    }

    private Component createDuplicateCinContent() {
        return createIssueGrid(duplicateCinIssues, "Adhérents, conjoints ou enfants avec le même CIN");
    }

    private Component createSameNameContent() {
        return createIssueGrid(sameNameIssues, "Conjoints ou enfants avec le même nom que l'adhérent");
    }

    private Component createManyConjointsContent() {
        return createIssueGrid(manyConjointsIssues, "Adhérents avec plus d'un conjoint déclaré");
    }

    private Component createManyEnfantsContent() {
        return createIssueGrid(manyEnfantsIssues, "Adhérents avec plus de 10 enfants déclarés");
    }

    private Component createMissingDataContent() {
        return createIssueGrid(missingDataIssues, "Enregistrements avec des données critiques manquantes");
    }

    private Component createIssueGrid(List<DataIssue> issues, String description) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setPadding(true);

        Span desc = new Span(description);
        desc.getStyle().set("color", "#64748b").set("font-size", "0.9rem").set("margin-bottom", "1rem");

        Grid<DataIssue> grid = new Grid<>();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.setHeight("400px");

        grid.addComponentColumn(issue -> createSeverityBadge(issue.severity))
            .setHeader("Sévérité").setWidth("100px").setFlexGrow(0);
        grid.addColumn(DataIssue::getType).setHeader("Type").setAutoWidth(true);
        grid.addColumn(DataIssue::getAgentInfo).setHeader("Adhérent").setAutoWidth(true);
        grid.addColumn(DataIssue::getDescription).setHeader("Description").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(DataIssue::getValue).setHeader("Valeur").setAutoWidth(true);

        grid.setItems(issues);

        layout.add(desc, grid);
        return layout;
    }

    private Component createSeverityBadge(String severity) {
        Span badge = new Span(severity);
        badge.getStyle()
            .set("padding", "4px 10px")
            .set("border-radius", "9999px")
            .set("font-size", "0.75rem")
            .set("font-weight", "500");

        switch (severity.toLowerCase()) {
            case "haute":
                badge.getStyle().set("background", "#fee2e2").set("color", "#dc2626");
                break;
            case "moyenne":
                badge.getStyle().set("background", "#fef3c7").set("color", "#d97706");
                break;
            default:
                badge.getStyle().set("background", "#dbeafe").set("color", "#2563eb");
        }

        return badge;
    }

    private void analyzeData() {
        // Clear previous data
        duplicateCinIssues.clear();
        sameNameIssues.clear();
        manyConjointsIssues.clear();
        manyEnfantsIssues.clear();
        missingDataIssues.clear();

        List<AdhAgent> agents = agentService.findAll();
        List<AdhConjoint> conjoints = conjointService.findAll();
        List<AdhEnfant> enfants = enfantService.findAll();

        // 1. Check duplicate CINs
        checkDuplicateCins(agents, conjoints, enfants);

        // 2. Check same names (conjoint/enfant with same name as agent)
        checkSameNames(agents, conjoints, enfants);

        // 3. Check agents with multiple conjoints
        checkManyConjoints(agents);

        // 4. Check agents with many enfants
        checkManyEnfants(agents);

        // 5. Check missing critical data
        checkMissingData(agents, conjoints, enfants);

        // Update counts
        duplicateCinCount.setText(String.valueOf(duplicateCinIssues.size()));
        sameNameCount.setText(String.valueOf(sameNameIssues.size()));
        manyConjointsCount.setText(String.valueOf(manyConjointsIssues.size()));
        manyEnfantsCount.setText(String.valueOf(manyEnfantsIssues.size()));
        missingDataCount.setText(String.valueOf(missingDataIssues.size()));
    }

    private void checkDuplicateCins(List<AdhAgent> agents, List<AdhConjoint> conjoints, List<AdhEnfant> enfants) {
        Map<String, List<String>> cinMap = new HashMap<>();

        // Collect all CINs
        for (AdhAgent a : agents) {
            if (a.getCIN_AG() != null && !a.getCIN_AG().trim().isEmpty()) {
                String cin = a.getCIN_AG().trim().toUpperCase();
                cinMap.computeIfAbsent(cin, k -> new ArrayList<>())
                    .add("Agent: " + a.getNOM_AG() + " " + a.getPR_AG() + " (ID: " + a.getIdAdh() + ")");
            }
        }

        for (AdhConjoint c : conjoints) {
            if (c.getCIN_CONJ() != null && !c.getCIN_CONJ().trim().isEmpty()) {
                String cin = c.getCIN_CONJ().trim().toUpperCase();
                cinMap.computeIfAbsent(cin, k -> new ArrayList<>())
                    .add("Conjoint: " + c.getNOM_CONJ() + " " + c.getPR_CONJ() + " (Code: " + c.getCodAg() + ")");
            }
        }

        for (AdhEnfant e : enfants) {
            if (e.getCin_PAC() != null && !e.getCin_PAC().trim().isEmpty()) {
                String cin = e.getCin_PAC().trim().toUpperCase();
                cinMap.computeIfAbsent(cin, k -> new ArrayList<>())
                    .add("Enfant: " + e.getNom_pac() + " " + e.getPr_pac() + " (Code: " + e.getCodAg() + ")");
            }
        }

        // Find duplicates
        for (Map.Entry<String, List<String>> entry : cinMap.entrySet()) {
            if (entry.getValue().size() > 1) {
                String owners = String.join(" | ", entry.getValue());
                duplicateCinIssues.add(new DataIssue(
                    "Haute",
                    "CIN Dupliqué",
                    owners,
                    "Le même CIN est utilisé par " + entry.getValue().size() + " personnes",
                    entry.getKey()
                ));
            }
        }
    }

    private void checkSameNames(List<AdhAgent> agents, List<AdhConjoint> conjoints, List<AdhEnfant> enfants) {
        Map<String, AdhAgent> agentByCode = agents.stream()
            .filter(a -> a.getCodAg() != null)
            .collect(Collectors.toMap(AdhAgent::getCodAg, a -> a, (a1, a2) -> a1));

        // Check conjoints
        for (AdhConjoint c : conjoints) {
            if (c.getCodAg() == null) continue;
            AdhAgent agent = agentByCode.get(c.getCodAg());
            if (agent == null) continue;

            String agentName = normalize(agent.getNOM_AG()) + " " + normalize(agent.getPR_AG());
            String conjointName = normalize(c.getNOM_CONJ()) + " " + normalize(c.getPR_CONJ());

            if (agentName.equals(conjointName) ||
                (normalize(agent.getNOM_AG()).equals(normalize(c.getNOM_CONJ())) &&
                 normalize(agent.getPR_AG()).equals(normalize(c.getPR_CONJ())))) {
                sameNameIssues.add(new DataIssue(
                    "Haute",
                    "Nom Identique (Conjoint)",
                    agent.getNOM_AG() + " " + agent.getPR_AG() + " (ID: " + agent.getIdAdh() + ")",
                    "Le conjoint a exactement le même nom que l'adhérent",
                    c.getNOM_CONJ() + " " + c.getPR_CONJ()
                ));
            }
        }

        // Check enfants
        for (AdhEnfant e : enfants) {
            if (e.getCodAg() == null) continue;
            AdhAgent agent = agentByCode.get(e.getCodAg());
            if (agent == null) continue;

            String agentName = normalize(agent.getNOM_AG()) + " " + normalize(agent.getPR_AG());
            String enfantName = normalize(e.getNom_pac()) + " " + normalize(e.getPr_pac());

            if (agentName.equals(enfantName) ||
                (normalize(agent.getNOM_AG()).equals(normalize(e.getNom_pac())) &&
                 normalize(agent.getPR_AG()).equals(normalize(e.getPr_pac())))) {
                sameNameIssues.add(new DataIssue(
                    "Haute",
                    "Nom Identique (Enfant)",
                    agent.getNOM_AG() + " " + agent.getPR_AG() + " (ID: " + agent.getIdAdh() + ")",
                    "L'enfant a exactement le même nom que l'adhérent",
                    e.getNom_pac() + " " + e.getPr_pac()
                ));
            }
        }
    }

    private void checkManyConjoints(List<AdhAgent> agents) {
        for (AdhAgent agent : agents) {
            long count = conjointService.countByAgent(agent);
            if (count > 1) {
                manyConjointsIssues.add(new DataIssue(
                    count > 2 ? "Haute" : "Moyenne",
                    "Plusieurs Conjoints",
                    agent.getNOM_AG() + " " + agent.getPR_AG() + " (ID: " + agent.getIdAdh() + ")",
                    "Cet adhérent a " + count + " conjoints déclarés",
                    String.valueOf(count) + " conjoints"
                ));
            }
        }
    }

    private void checkManyEnfants(List<AdhAgent> agents) {
        for (AdhAgent agent : agents) {
            long count = enfantService.countByAgent(agent);
            if (count > 10) {
                manyEnfantsIssues.add(new DataIssue(
                    count > 15 ? "Haute" : "Moyenne",
                    "Beaucoup d'Enfants",
                    agent.getNOM_AG() + " " + agent.getPR_AG() + " (ID: " + agent.getIdAdh() + ")",
                    "Cet adhérent a " + count + " enfants déclarés",
                    String.valueOf(count) + " enfants"
                ));
            }
        }
    }

    private void checkMissingData(List<AdhAgent> agents, List<AdhConjoint> conjoints, List<AdhEnfant> enfants) {
        // Check agents
        for (AdhAgent a : agents) {
            List<String> missing = new ArrayList<>();
            if (isEmpty(a.getNOM_AG())) missing.add("Nom");
            if (isEmpty(a.getPR_AG())) missing.add("Prénom");
            if (isEmpty(a.getCIN_AG())) missing.add("CIN");

            if (!missing.isEmpty()) {
                missingDataIssues.add(new DataIssue(
                    "Moyenne",
                    "Agent - Données Manquantes",
                    "ID: " + a.getIdAdh(),
                    "Champs manquants: " + String.join(", ", missing),
                    String.join(", ", missing)
                ));
            }
        }

        // Check conjoints
        for (AdhConjoint c : conjoints) {
            List<String> missing = new ArrayList<>();
            if (isEmpty(c.getNOM_CONJ())) missing.add("Nom");
            if (isEmpty(c.getPR_CONJ())) missing.add("Prénom");

            if (!missing.isEmpty()) {
                missingDataIssues.add(new DataIssue(
                    "Basse",
                    "Conjoint - Données Manquantes",
                    "Code Agent: " + c.getCodAg(),
                    "Champs manquants: " + String.join(", ", missing),
                    String.join(", ", missing)
                ));
            }
        }

        // Check enfants
        for (AdhEnfant e : enfants) {
            List<String> missing = new ArrayList<>();
            if (isEmpty(e.getNom_pac())) missing.add("Nom");
            if (isEmpty(e.getPr_pac())) missing.add("Prénom");

            if (!missing.isEmpty()) {
                missingDataIssues.add(new DataIssue(
                    "Basse",
                    "Enfant - Données Manquantes",
                    "Code Agent: " + e.getCodAg(),
                    "Champs manquants: " + String.join(", ", missing),
                    String.join(", ", missing)
                ));
            }
        }
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    // Inner class to represent a data issue
    public static class DataIssue {
        private final String severity;
        private final String type;
        private final String agentInfo;
        private final String description;
        private final String value;

        public DataIssue(String severity, String type, String agentInfo, String description, String value) {
            this.severity = severity;
            this.type = type;
            this.agentInfo = agentInfo;
            this.description = description;
            this.value = value;
        }

        public String getSeverity() { return severity; }
        public String getType() { return type; }
        public String getAgentInfo() { return agentInfo; }
        public String getDescription() { return description; }
        public String getValue() { return value; }
    }
}
