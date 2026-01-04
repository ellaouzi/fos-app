package com.fosagri.application.views.search;

import com.fosagri.application.services.FosAgriKnowledgeService;
import com.fosagri.application.services.FosAgriKnowledgeService.*;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.List;

@PageTitle("Recherche FOS-Agri")
@Route(value = "search", layout = MainLayout.class)
@Menu(order = 0, icon = LineAwesomeIconUrl.SEARCH_SOLID)
@PermitAll
public class SearchView extends VerticalLayout {

    private final FosAgriKnowledgeService knowledgeService;
    private final TextField searchField;
    private final VerticalLayout resultsContainer;
    private final FlexLayout categoryButtons;
    private final FlexLayout typeButtons;
    private final Span resultsCount;

    private String selectedCategory = "all";
    private String selectedType = "all";

    public SearchView(FosAgriKnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background", "#f8fafc");

        // Header section
        add(createHeader());

        // Main content
        VerticalLayout mainContent = new VerticalLayout();
        mainContent.setSizeFull();
        mainContent.setPadding(true);
        mainContent.getStyle().set("max-width", "1200px").set("margin", "0 auto");

        // Search field
        searchField = createSearchField();

        // Category filters
        categoryButtons = createCategoryFilters();

        // Type filters
        typeButtons = createTypeFilters();

        // Results count
        resultsCount = new Span();
        resultsCount.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.9rem")
            .set("margin-bottom", "1rem");

        // Results container
        resultsContainer = new VerticalLayout();
        resultsContainer.setPadding(false);
        resultsContainer.setSpacing(true);

        mainContent.add(searchField, categoryButtons, typeButtons, resultsCount, resultsContainer);
        add(mainContent);

        // Initial load
        performSearch();
    }

    private Component createHeader() {
        Div header = new Div();
        header.setWidthFull();
        header.getStyle()
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 100%)")
            .set("padding", "2rem")
            .set("color", "white");

        VerticalLayout headerContent = new VerticalLayout();
        headerContent.setPadding(false);
        headerContent.setSpacing(false);
        headerContent.getStyle().set("max-width", "1200px").set("margin", "0 auto");

        // Title row
        HorizontalLayout titleRow = new HorizontalLayout();
        titleRow.setAlignItems(FlexComponent.Alignment.CENTER);
        titleRow.setSpacing(true);

        Icon searchIcon = VaadinIcon.SEARCH.create();
        searchIcon.setSize("32px");
        searchIcon.getStyle().set("color", "rgba(255,255,255,0.9)");

        H2 title = new H2("Recherche FOS-Agri");
        title.getStyle()
            .set("margin", "0")
            .set("color", "white")
            .set("font-weight", "700");

        titleRow.add(searchIcon, title);

        Paragraph subtitle = new Paragraph("Trouvez rapidement les informations sur les prestations, services et partenaires");
        subtitle.getStyle()
            .set("color", "rgba(255,255,255,0.8)")
            .set("margin", "0.5rem 0 0 0")
            .set("font-size", "0.95rem");

        // Quick suggestions
        HorizontalLayout suggestions = new HorizontalLayout();
        suggestions.getStyle()
            .set("flex-wrap", "wrap")
            .set("gap", "0.5rem")
            .set("margin-top", "1rem");

        String[] quickSearches = {"Club", "Bourses", "Logement", "INWI", "Assurance"};
        for (String search : quickSearches) {
            Button btn = new Button(search);
            btn.getStyle()
                .set("background", "rgba(255,255,255,0.2)")
                .set("color", "white")
                .set("border", "none")
                .set("border-radius", "20px")
                .set("padding", "0.4rem 1rem")
                .set("font-size", "0.85rem")
                .set("cursor", "pointer");
            btn.addClickListener(e -> {
                searchField.setValue(search);
                performSearch();
            });
            suggestions.add(btn);
        }

        headerContent.add(titleRow, subtitle, suggestions);
        header.add(headerContent);
        return header;
    }

    private TextField createSearchField() {
        TextField field = new TextField();
        field.setPlaceholder("Rechercher des prestations, services, partenaires...");
        field.setWidthFull();
        field.setClearButtonVisible(true);
        field.setPrefixComponent(VaadinIcon.SEARCH.create());

        field.getStyle()
            .set("--vaadin-input-field-border-radius", "12px")
            .set("--vaadin-input-field-background", "white")
            .set("font-size", "1.1rem")
            .set("margin-bottom", "1rem");

        field.addKeyPressListener(Key.ENTER, e -> performSearch());
        field.addValueChangeListener(e -> performSearch());

        return field;
    }

    private FlexLayout createCategoryFilters() {
        FlexLayout layout = new FlexLayout();
        layout.getStyle()
            .set("flex-wrap", "wrap")
            .set("gap", "0.5rem")
            .set("margin-bottom", "1rem");

        for (Category category : knowledgeService.getCategories()) {
            Button btn = new Button(category.name());
            btn.getStyle()
                .set("border-radius", "20px")
                .set("padding", "0.5rem 1rem")
                .set("font-weight", "500")
                .set("transition", "all 0.2s ease");

            if (category.id().equals(selectedCategory)) {
                btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            } else {
                btn.getStyle()
                    .set("background", "white")
                    .set("color", "#475569")
                    .set("border", "1px solid #e2e8f0");
            }

            btn.addClickListener(e -> {
                selectedCategory = category.id();
                updateCategoryButtons();
                performSearch();
            });

            layout.add(btn);
        }

        return layout;
    }

    private void updateCategoryButtons() {
        int index = 0;
        for (Category category : knowledgeService.getCategories()) {
            Button btn = (Button) categoryButtons.getComponentAt(index);
            btn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);

            if (category.id().equals(selectedCategory)) {
                btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btn.getStyle().remove("background").remove("color").remove("border");
            } else {
                btn.getStyle()
                    .set("background", "white")
                    .set("color", "#475569")
                    .set("border", "1px solid #e2e8f0");
            }
            index++;
        }
    }

    private FlexLayout createTypeFilters() {
        FlexLayout layout = new FlexLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        layout.getStyle()
            .set("gap", "0.5rem")
            .set("margin-bottom", "1rem");

        Icon filterIcon = VaadinIcon.FILTER.create();
        filterIcon.setSize("16px");
        filterIcon.getStyle().set("color", "#64748b");

        Span label = new Span("Type:");
        label.getStyle().set("color", "#64748b").set("font-size", "0.9rem");

        layout.add(filterIcon, label);

        String[][] types = {{"all", "Tous"}, {"page", "Pages Web"}, {"pdf", "Documents PDF"}, {"partner", "Partenaires"}};

        for (String[] type : types) {
            Button btn = new Button(type[1]);
            btn.getStyle()
                .set("border-radius", "8px")
                .set("padding", "0.3rem 0.75rem")
                .set("font-size", "0.85rem")
                .set("transition", "all 0.2s ease");

            if (type[0].equals(selectedType)) {
                btn.getStyle()
                    .set("background", "#1e293b")
                    .set("color", "white");
            } else {
                btn.getStyle()
                    .set("background", "#f1f5f9")
                    .set("color", "#475569");
            }

            final String typeId = type[0];
            btn.addClickListener(e -> {
                selectedType = typeId;
                updateTypeButtons();
                performSearch();
            });

            layout.add(btn);
        }

        return layout;
    }

    private void updateTypeButtons() {
        String[][] types = {{"all", "Tous"}, {"page", "Pages Web"}, {"pdf", "Documents PDF"}, {"partner", "Partenaires"}};
        int index = 2; // Skip filter icon and label

        for (String[] type : types) {
            Button btn = (Button) typeButtons.getComponentAt(index);

            if (type[0].equals(selectedType)) {
                btn.getStyle()
                    .set("background", "#1e293b")
                    .set("color", "white");
            } else {
                btn.getStyle()
                    .set("background", "#f1f5f9")
                    .set("color", "#475569");
            }
            index++;
        }
    }

    private void performSearch() {
        String query = searchField.getValue();
        List<SearchResult> results = knowledgeService.search(query, selectedCategory, selectedType);

        resultsCount.setText(results.size() + " résultat" + (results.size() > 1 ? "s" : "") +
            " trouvé" + (results.size() > 1 ? "s" : "") +
            (query != null && !query.isEmpty() ? " pour \"" + query + "\"" : ""));

        resultsContainer.removeAll();

        if (results.isEmpty()) {
            resultsContainer.add(createEmptyState());
        } else {
            for (SearchResult result : results) {
                resultsContainer.add(createResultCard(result));
            }
        }
    }

    private Component createResultCard(SearchResult result) {
        ContentItem item = result.item();

        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("padding", "1.25rem")
            .set("box-shadow", "0 2px 12px rgba(0,0,0,0.06)")
            .set("border", "1px solid #f1f5f9")
            .set("transition", "all 0.3s ease")
            .set("cursor", "pointer");

        card.getElement().addEventListener("mouseover", e ->
            card.getStyle().set("transform", "translateY(-2px)").set("box-shadow", "0 8px 24px rgba(0,0,0,0.1)"));
        card.getElement().addEventListener("mouseout", e ->
            card.getStyle().set("transform", "translateY(0)").set("box-shadow", "0 2px 12px rgba(0,0,0,0.06)"));

        HorizontalLayout content = new HorizontalLayout();
        content.setWidthFull();
        content.setAlignItems(FlexComponent.Alignment.START);
        content.setSpacing(true);

        // Icon
        Div iconContainer = new Div();
        iconContainer.getStyle()
            .set("width", "48px")
            .set("height", "48px")
            .set("border-radius", "12px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("flex-shrink", "0");

        Icon icon;
        String bgColor;
        switch (item.type()) {
            case "pdf":
                icon = VaadinIcon.FILE_TEXT.create();
                bgColor = "#fee2e2";
                icon.getStyle().set("color", "#dc2626");
                break;
            case "partner":
                icon = VaadinIcon.HANDSHAKE.create();
                bgColor = "#d1fae5";
                icon.getStyle().set("color", "#059669");
                break;
            default:
                icon = VaadinIcon.GLOBE.create();
                bgColor = "#dbeafe";
                icon.getStyle().set("color", "#2563eb");
        }
        iconContainer.getStyle().set("background", bgColor);
        icon.setSize("24px");
        iconContainer.add(icon);

        // Content
        VerticalLayout textContent = new VerticalLayout();
        textContent.setPadding(false);
        textContent.setSpacing(false);
        textContent.getStyle().set("flex", "1");

        // Badges row
        HorizontalLayout badges = new HorizontalLayout();
        badges.setSpacing(true);
        badges.getStyle().set("margin-bottom", "0.5rem");

        Span typeBadge = createTypeBadge(item.type());
        badges.add(typeBadge);

        if (result.relevance() > 0) {
            Span relevanceBadge = new Span("Pertinence: " + Math.min(100, result.relevance() / 2) + "%");
            relevanceBadge.getStyle()
                .set("background", "#dcfce7")
                .set("color", "#166534")
                .set("padding", "2px 8px")
                .set("border-radius", "10px")
                .set("font-size", "0.7rem")
                .set("font-weight", "500");
            badges.add(relevanceBadge);
        }

        // Title
        Span title = new Span(item.title());
        title.getStyle()
            .set("font-weight", "600")
            .set("font-size", "1.1rem")
            .set("color", "#1e293b")
            .set("display", "block")
            .set("margin-bottom", "0.25rem");

        // Description
        Span description = new Span(item.description());
        description.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.9rem")
            .set("display", "block")
            .set("margin-bottom", "0.5rem");

        // Keywords
        FlexLayout keywords = new FlexLayout();
        keywords.getStyle().set("flex-wrap", "wrap").set("gap", "4px");

        if (item.keywords() != null) {
            for (int i = 0; i < Math.min(5, item.keywords().size()); i++) {
                Span keyword = new Span(item.keywords().get(i));
                keyword.getStyle()
                    .set("background", "#f1f5f9")
                    .set("color", "#64748b")
                    .set("padding", "2px 8px")
                    .set("border-radius", "4px")
                    .set("font-size", "0.75rem");
                keywords.add(keyword);
            }
        }

        textContent.add(badges, title, description, keywords);

        // Action button
        Anchor link = new Anchor(item.url(), "");
        link.setTarget("_blank");

        Button actionBtn = new Button(item.type().equals("pdf") ? "Télécharger" : "Visiter");
        actionBtn.setIcon(item.type().equals("pdf") ? VaadinIcon.DOWNLOAD.create() : VaadinIcon.EXTERNAL_LINK.create());
        actionBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        actionBtn.getStyle()
            .set("border-radius", "10px")
            .set("flex-shrink", "0");

        link.add(actionBtn);

        content.add(iconContainer, textContent, link);
        card.add(content);

        return card;
    }

    private Span createTypeBadge(String type) {
        Span badge = new Span();
        badge.getStyle()
            .set("padding", "3px 10px")
            .set("border-radius", "10px")
            .set("font-size", "0.7rem")
            .set("font-weight", "600")
            .set("text-transform", "uppercase");

        switch (type) {
            case "pdf":
                badge.setText("PDF");
                badge.getStyle().set("background", "#fee2e2").set("color", "#dc2626");
                break;
            case "partner":
                badge.setText("Partenaire");
                badge.getStyle().set("background", "#d1fae5").set("color", "#059669");
                break;
            default:
                badge.setText("Page Web");
                badge.getStyle().set("background", "#dbeafe").set("color", "#2563eb");
        }

        return badge;
    }

    private Component createEmptyState() {
        VerticalLayout empty = new VerticalLayout();
        empty.setAlignItems(FlexComponent.Alignment.CENTER);
        empty.setPadding(true);
        empty.getStyle().set("padding", "3rem");

        Icon icon = VaadinIcon.SEARCH.create();
        icon.setSize("64px");
        icon.getStyle().set("color", "#cbd5e1");

        H3 title = new H3("Aucun résultat trouvé");
        title.getStyle().set("color", "#64748b").set("margin", "1rem 0 0.5rem 0");

        Paragraph hint = new Paragraph("Essayez de modifier vos critères de recherche");
        hint.getStyle().set("color", "#94a3b8");

        empty.add(icon, title, hint);
        return empty;
    }
}
