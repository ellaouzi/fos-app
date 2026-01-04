package com.fosagri.application.views.contacts;

import com.fosagri.application.entities.Contact;
import com.fosagri.application.entities.Contact.TypeContact;
import com.fosagri.application.security.AuthenticatedUser;
import com.fosagri.application.services.ContactService;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PageTitle("Contacts FOS-Agri")
@Route(value = "contacts", layout = MainLayout.class)
@Menu(order = 4, icon = LineAwesomeIconUrl.ADDRESS_BOOK)
@PermitAll
public class ContactView extends VerticalLayout {

    private final ContactService contactService;
    private final boolean isAdmin;
    private final TextField searchField = new TextField();
    private TypeContact selectedType = null;
    private final FlexLayout cardsContainer = new FlexLayout();
    private final Map<TypeContact, Button> filterButtons = new HashMap<>();

    // Admin grid
    private final Grid<Contact> grid = new Grid<>(Contact.class, false);
    private final ComboBox<TypeContact> typeFilter = new ComboBox<>();

    public ContactView(ContactService contactService, AuthenticatedUser authenticatedUser) {
        this.contactService = contactService;

        this.isAdmin = authenticatedUser.get()
            .map(u -> u.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getRole())))
            .orElse(false);

        addClassName("contacts-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "#f8fafc");

        add(createHeader());

        if (isAdmin) {
            add(createAdminToolbar());
            add(createAdminGrid());
            refreshAdminGrid();
        } else {
            add(createFilterTabs());
            add(createSearchBar());
            add(createCardsContainer());
            refreshCards();
        }
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle()
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 100%)")
            .set("border-radius", "16px")
            .set("padding", "2rem")
            .set("color", "white")
            .set("box-shadow", "0 10px 40px rgba(59, 107, 53, 0.3)");

        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setSpacing(false);
        titleSection.setPadding(false);

        H2 title = new H2("Annuaire des Contacts");
        title.getStyle()
            .set("color", "white")
            .set("margin", "0")
            .set("font-size", "1.75rem")
            .set("font-weight", "700");

        Span subtitle = new Span("Retrouvez tous les contacts FOS-Agri, partenaires et directions régionales");
        subtitle.getStyle()
            .set("color", "rgba(255,255,255,0.9)")
            .set("font-size", "1rem")
            .set("margin-top", "0.5rem");

        titleSection.add(title, subtitle);
        header.add(titleSection);

        if (isAdmin) {
            Button addButton = new Button("Nouveau Contact", new Icon(VaadinIcon.PLUS));
            addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addButton.getStyle()
                .set("background", "white")
                .set("color", "#3b6b35")
                .set("font-weight", "600");
            addButton.addClickListener(e -> openContactDialog(new Contact()));
            header.add(addButton);
        }

        return header;
    }

    // ==================== ADHERENT VIEW ====================

    private HorizontalLayout createFilterTabs() {
        HorizontalLayout tabs = new HorizontalLayout();
        tabs.setWidthFull();
        tabs.setSpacing(true);
        tabs.getStyle()
            .set("flex-wrap", "wrap")
            .set("gap", "0.5rem")
            .set("margin-top", "1rem");

        // "Tous" button
        Button allBtn = createFilterButton("Tous", VaadinIcon.GRID_BIG_O, null);
        allBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        tabs.add(allBtn);

        // Type buttons with icons
        tabs.add(createFilterButton("Siège", VaadinIcon.BUILDING, TypeContact.SIEGE));
        tabs.add(createFilterButton("Directions", VaadinIcon.OFFICE, TypeContact.DIRECTION_REGIONALE));
        tabs.add(createFilterButton("Médical", VaadinIcon.STETHOSCOPE, TypeContact.PARTENAIRE_MEDICAL));
        tabs.add(createFilterButton("Banques", VaadinIcon.INSTITUTION, TypeContact.PARTENAIRE_BANCAIRE));
        tabs.add(createFilterButton("Assurances", VaadinIcon.SHIELD, TypeContact.PARTENAIRE_ASSURANCE));
        tabs.add(createFilterButton("Commercial", VaadinIcon.CART, TypeContact.PARTENAIRE_COMMERCIAL));
        tabs.add(createFilterButton("Services", VaadinIcon.COG, TypeContact.SERVICE));
        tabs.add(createFilterButton("Urgences", VaadinIcon.AMBULANCE, TypeContact.URGENCE));

        return tabs;
    }

    private Button createFilterButton(String label, VaadinIcon icon, TypeContact type) {
        Button btn = new Button(label, icon.create());
        btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        btn.getStyle()
            .set("border-radius", "20px")
            .set("padding", "0.5rem 1rem")
            .set("font-size", "0.9rem")
            .set("background", "white")
            .set("border", "1px solid #e2e8f0")
            .set("transition", "all 0.2s");

        filterButtons.put(type, btn);

        btn.addClickListener(e -> {
            selectedType = type;
            updateFilterButtonStyles();
            refreshCards();
        });

        return btn;
    }

    private void updateFilterButtonStyles() {
        filterButtons.forEach((type, btn) -> {
            if (type == selectedType) {
                btn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btn.getStyle()
                    .set("background", "#3b6b35")
                    .set("color", "white")
                    .set("border-color", "#3b6b35");
            } else {
                btn.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                btn.getStyle()
                    .set("background", "white")
                    .set("color", "#374151")
                    .set("border-color", "#e2e8f0");
            }
        });
    }

    private HorizontalLayout createSearchBar() {
        HorizontalLayout searchBar = new HorizontalLayout();
        searchBar.setWidthFull();
        searchBar.setAlignItems(FlexComponent.Alignment.CENTER);
        searchBar.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("padding", "1rem")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)")
            .set("margin-top", "1rem");

        Icon searchIcon = VaadinIcon.SEARCH.create();
        searchIcon.getStyle().set("color", "#9ca3af");

        searchField.setPlaceholder("Rechercher un contact, une ville, un partenaire...");
        searchField.setPrefixComponent(searchIcon);
        searchField.setWidthFull();
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.getStyle()
            .set("--vaadin-input-field-border-radius", "8px");
        searchField.addValueChangeListener(e -> refreshCards());

        searchBar.add(searchField);
        return searchBar;
    }

    private FlexLayout createCardsContainer() {
        cardsContainer.setWidthFull();
        cardsContainer.getStyle()
            .set("flex-wrap", "wrap")
            .set("gap", "1.25rem")
            .set("margin-top", "1rem")
            .set("padding-bottom", "2rem");
        return cardsContainer;
    }

    private void refreshCards() {
        cardsContainer.removeAll();

        List<Contact> contacts;
        String searchTerm = searchField.getValue();

        if (selectedType != null) {
            contacts = contactService.findByTypeActive(selectedType);
        } else {
            contacts = contactService.findAllActive();
        }

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String lowerSearch = searchTerm.toLowerCase().trim();
            contacts = contacts.stream()
                .filter(c ->
                    (c.getNom() != null && c.getNom().toLowerCase().contains(lowerSearch)) ||
                    (c.getEmail() != null && c.getEmail().toLowerCase().contains(lowerSearch)) ||
                    (c.getVille() != null && c.getVille().toLowerCase().contains(lowerSearch)) ||
                    (c.getFonction() != null && c.getFonction().toLowerCase().contains(lowerSearch)) ||
                    (c.getDescription() != null && c.getDescription().toLowerCase().contains(lowerSearch))
                )
                .toList();
        }

        if (contacts.isEmpty()) {
            Div emptyState = createEmptyState();
            cardsContainer.add(emptyState);
        } else {
            contacts.forEach(contact -> cardsContainer.add(createContactCard(contact)));
        }
    }

    private Div createEmptyState() {
        Div empty = new Div();
        empty.getStyle()
            .set("width", "100%")
            .set("text-align", "center")
            .set("padding", "3rem");

        Icon icon = VaadinIcon.SEARCH.create();
        icon.setSize("48px");
        icon.getStyle().set("color", "#9ca3af");

        H3 title = new H3("Aucun contact trouvé");
        title.getStyle()
            .set("color", "#6b7280")
            .set("margin", "1rem 0 0.5rem 0");

        Paragraph text = new Paragraph("Essayez de modifier vos critères de recherche");
        text.getStyle().set("color", "#9ca3af");

        empty.add(icon, title, text);
        return empty;
    }

    private Div createContactCard(Contact contact) {
        Div card = new Div();
        card.getStyle()
            .set("background", "white")
            .set("border-radius", "16px")
            .set("padding", "1.5rem")
            .set("box-shadow", "0 4px 15px rgba(0,0,0,0.08)")
            .set("width", "320px")
            .set("transition", "transform 0.2s, box-shadow 0.2s")
            .set("cursor", "pointer")
            .set("border", "1px solid #f1f5f9");

        // Header with icon and type badge
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.START);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        // Icon based on type
        Div iconWrapper = new Div();
        iconWrapper.getStyle()
            .set("width", "48px")
            .set("height", "48px")
            .set("border-radius", "12px")
            .set("display", "flex")
            .set("align-items", "center")
            .set("justify-content", "center")
            .set("background", getTypeColor(contact.getType()) + "15");

        Icon typeIcon = getTypeIcon(contact.getType());
        typeIcon.setSize("24px");
        typeIcon.getStyle().set("color", getTypeColor(contact.getType()));
        iconWrapper.add(typeIcon);

        // Type badge
        Span typeBadge = new Span(contact.getType() != null ? contact.getType().getLabel() : "");
        typeBadge.getStyle()
            .set("background", getTypeColor(contact.getType()) + "20")
            .set("color", getTypeColor(contact.getType()))
            .set("padding", "0.25rem 0.75rem")
            .set("border-radius", "12px")
            .set("font-size", "0.75rem")
            .set("font-weight", "600");

        header.add(iconWrapper, typeBadge);

        // Name and function
        H3 name = new H3(contact.getNom());
        name.getStyle()
            .set("margin", "1rem 0 0.25rem 0")
            .set("font-size", "1.1rem")
            .set("color", "#1e293b")
            .set("font-weight", "600");

        Span function = new Span(contact.getFonction() != null ? contact.getFonction() : "");
        function.getStyle()
            .set("color", "#64748b")
            .set("font-size", "0.875rem")
            .set("display", "block")
            .set("margin-bottom", "1rem");

        // Contact details
        VerticalLayout details = new VerticalLayout();
        details.setPadding(false);
        details.setSpacing(false);
        details.getStyle().set("gap", "0.5rem");

        if (contact.getTelephone() != null && !contact.getTelephone().isEmpty()) {
            details.add(createContactDetail(VaadinIcon.PHONE, contact.getTelephone(), "tel:" + contact.getTelephone()));
        }

        if (contact.getTelephoneMobile() != null && !contact.getTelephoneMobile().isEmpty()) {
            details.add(createContactDetail(VaadinIcon.MOBILE, contact.getTelephoneMobile(), "tel:" + contact.getTelephoneMobile()));
        }

        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            details.add(createContactDetail(VaadinIcon.ENVELOPE, contact.getEmail(), "mailto:" + contact.getEmail()));
        }

        if (contact.getVille() != null && !contact.getVille().isEmpty()) {
            String location = contact.getVille();
            if (contact.getRegion() != null && !contact.getRegion().isEmpty()) {
                location += ", " + contact.getRegion();
            }
            details.add(createContactDetail(VaadinIcon.MAP_MARKER, location, null));
        }

        if (contact.getHoraires() != null && !contact.getHoraires().isEmpty()) {
            details.add(createContactDetail(VaadinIcon.CLOCK, contact.getHoraires(), null));
        }

        // Action buttons
        HorizontalLayout actions = new HorizontalLayout();
        actions.setWidthFull();
        actions.setSpacing(true);
        actions.getStyle().set("margin-top", "1rem");

        if (contact.getTelephone() != null && !contact.getTelephone().isEmpty()) {
            Button callBtn = new Button("Appeler", VaadinIcon.PHONE.create());
            callBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            callBtn.getStyle()
                .set("background", "#3b6b35")
                .set("border-radius", "8px");
            callBtn.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.getPage().open("tel:" + contact.getTelephone()));
            });
            actions.add(callBtn);
        }

        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            Button emailBtn = new Button("Email", VaadinIcon.ENVELOPE.create());
            emailBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            emailBtn.getStyle().set("border-radius", "8px");
            emailBtn.addClickListener(e -> {
                getUI().ifPresent(ui -> ui.getPage().open("mailto:" + contact.getEmail()));
            });
            actions.add(emailBtn);
        }

        // Info button for details
        Button infoBtn = new Button(VaadinIcon.INFO_CIRCLE.create());
        infoBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        infoBtn.getStyle().set("margin-left", "auto");
        infoBtn.addClickListener(e -> showContactDetails(contact));
        actions.add(infoBtn);

        card.add(header, name, function, details, actions);
        return card;
    }

    private HorizontalLayout createContactDetail(VaadinIcon iconType, String text, String link) {
        HorizontalLayout row = new HorizontalLayout();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.setSpacing(true);
        row.setPadding(false);

        Icon icon = iconType.create();
        icon.setSize("16px");
        icon.getStyle().set("color", "#9ca3af");

        if (link != null) {
            Anchor anchor = new Anchor(link, text);
            anchor.getStyle()
                .set("color", "#2c5aa0")
                .set("text-decoration", "none")
                .set("font-size", "0.875rem");
            row.add(icon, anchor);
        } else {
            Span span = new Span(text);
            span.getStyle()
                .set("color", "#4b5563")
                .set("font-size", "0.875rem");
            row.add(icon, span);
        }

        return row;
    }

    private void showContactDetails(Contact contact) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(contact.getNom());
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        // Type and function
        if (contact.getType() != null) {
            Span typeBadge = new Span(contact.getType().getLabel());
            typeBadge.getStyle()
                .set("background", getTypeColor(contact.getType()) + "20")
                .set("color", getTypeColor(contact.getType()))
                .set("padding", "0.35rem 1rem")
                .set("border-radius", "15px")
                .set("font-size", "0.85rem")
                .set("font-weight", "600");
            content.add(typeBadge);
        }

        if (contact.getFonction() != null && !contact.getFonction().isEmpty()) {
            Paragraph func = new Paragraph(contact.getFonction());
            func.getStyle().set("color", "#64748b").set("margin", "0.5rem 0");
            content.add(func);
        }

        // Description
        if (contact.getDescription() != null && !contact.getDescription().isEmpty()) {
            Paragraph desc = new Paragraph(contact.getDescription());
            desc.getStyle()
                .set("color", "#374151")
                .set("background", "#f8fafc")
                .set("padding", "1rem")
                .set("border-radius", "8px")
                .set("margin", "1rem 0");
            content.add(desc);
        }

        // Contact info section
        H4 contactTitle = new H4("Coordonnées");
        contactTitle.getStyle().set("margin", "1rem 0 0.5rem 0").set("color", "#1e293b");
        content.add(contactTitle);

        if (contact.getTelephone() != null && !contact.getTelephone().isEmpty()) {
            content.add(createDetailRow("Téléphone", contact.getTelephone(), "tel:" + contact.getTelephone()));
        }
        if (contact.getTelephoneMobile() != null && !contact.getTelephoneMobile().isEmpty()) {
            content.add(createDetailRow("Mobile", contact.getTelephoneMobile(), "tel:" + contact.getTelephoneMobile()));
        }
        if (contact.getFax() != null && !contact.getFax().isEmpty()) {
            content.add(createDetailRow("Fax", contact.getFax(), null));
        }
        if (contact.getEmail() != null && !contact.getEmail().isEmpty()) {
            content.add(createDetailRow("Email", contact.getEmail(), "mailto:" + contact.getEmail()));
        }
        if (contact.getSiteWeb() != null && !contact.getSiteWeb().isEmpty()) {
            content.add(createDetailRow("Site Web", contact.getSiteWeb(), contact.getSiteWeb()));
        }

        // Address section
        if (contact.getAdresse() != null || contact.getVille() != null) {
            H4 addressTitle = new H4("Adresse");
            addressTitle.getStyle().set("margin", "1rem 0 0.5rem 0").set("color", "#1e293b");
            content.add(addressTitle);

            StringBuilder address = new StringBuilder();
            if (contact.getAdresse() != null) address.append(contact.getAdresse()).append("\n");
            if (contact.getCodePostal() != null) address.append(contact.getCodePostal()).append(" ");
            if (contact.getVille() != null) address.append(contact.getVille());
            if (contact.getRegion() != null) address.append(", ").append(contact.getRegion());

            Paragraph addressPara = new Paragraph(address.toString());
            addressPara.getStyle()
                .set("white-space", "pre-line")
                .set("color", "#4b5563")
                .set("margin", "0");
            content.add(addressPara);
        }

        // Hours
        if (contact.getHoraires() != null && !contact.getHoraires().isEmpty()) {
            H4 hoursTitle = new H4("Horaires");
            hoursTitle.getStyle().set("margin", "1rem 0 0.5rem 0").set("color", "#1e293b");
            content.add(hoursTitle);

            Paragraph hours = new Paragraph(contact.getHoraires());
            hours.getStyle().set("color", "#4b5563").set("margin", "0");
            content.add(hours);
        }

        dialog.add(content);
        dialog.getFooter().add(new Button("Fermer", e -> dialog.close()));
        dialog.open();
    }

    private HorizontalLayout createDetailRow(String label, String value, String link) {
        HorizontalLayout row = new HorizontalLayout();
        row.setWidthFull();
        row.setAlignItems(FlexComponent.Alignment.CENTER);
        row.getStyle().set("margin", "0.25rem 0");

        Span labelSpan = new Span(label + ":");
        labelSpan.getStyle()
            .set("color", "#6b7280")
            .set("font-size", "0.875rem")
            .set("width", "100px");

        if (link != null) {
            Anchor valueAnchor = new Anchor(link, value);
            valueAnchor.getStyle()
                .set("color", "#2c5aa0")
                .set("text-decoration", "none")
                .set("font-size", "0.875rem");
            if (link.startsWith("http")) {
                valueAnchor.setTarget("_blank");
            }
            row.add(labelSpan, valueAnchor);
        } else {
            Span valueSpan = new Span(value);
            valueSpan.getStyle()
                .set("color", "#1e293b")
                .set("font-size", "0.875rem");
            row.add(labelSpan, valueSpan);
        }

        return row;
    }

    private String getTypeColor(TypeContact type) {
        if (type == null) return "#6b7280";
        return switch (type) {
            case SIEGE -> "#3b6b35";
            case DIRECTION_REGIONALE -> "#2c5aa0";
            case PARTENAIRE_MEDICAL -> "#ef4444";
            case PARTENAIRE_BANCAIRE -> "#d4a949";
            case PARTENAIRE_ASSURANCE -> "#8b5cf6";
            case PARTENAIRE_COMMERCIAL -> "#f59e0b";
            case SERVICE -> "#06b6d4";
            case URGENCE -> "#dc2626";
            case ANTENNE -> "#10b981";
            case AUTRE -> "#6b7280";
        };
    }

    private Icon getTypeIcon(TypeContact type) {
        if (type == null) return VaadinIcon.OFFICE.create();
        return switch (type) {
            case SIEGE -> VaadinIcon.BUILDING.create();
            case DIRECTION_REGIONALE -> VaadinIcon.OFFICE.create();
            case PARTENAIRE_MEDICAL -> VaadinIcon.STETHOSCOPE.create();
            case PARTENAIRE_BANCAIRE -> VaadinIcon.INSTITUTION.create();
            case PARTENAIRE_ASSURANCE -> VaadinIcon.SHIELD.create();
            case PARTENAIRE_COMMERCIAL -> VaadinIcon.CART.create();
            case SERVICE -> VaadinIcon.COG.create();
            case URGENCE -> VaadinIcon.AMBULANCE.create();
            case ANTENNE -> VaadinIcon.CONNECT.create();
            case AUTRE -> VaadinIcon.ELLIPSIS_DOTS_H.create();
        };
    }

    // ==================== ADMIN VIEW ====================

    private HorizontalLayout createAdminToolbar() {
        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.END);
        toolbar.setSpacing(true);
        toolbar.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("padding", "1rem")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)")
            .set("margin-top", "1rem");

        TextField adminSearchField = new TextField();
        adminSearchField.setPlaceholder("Rechercher...");
        adminSearchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        adminSearchField.setWidth("300px");
        adminSearchField.setValueChangeMode(ValueChangeMode.LAZY);
        adminSearchField.addValueChangeListener(e -> {
            searchField.setValue(e.getValue());
            refreshAdminGrid();
        });

        typeFilter.setPlaceholder("Tous les types");
        typeFilter.setItems(TypeContact.values());
        typeFilter.setItemLabelGenerator(TypeContact::getLabel);
        typeFilter.setClearButtonVisible(true);
        typeFilter.setWidth("200px");
        typeFilter.addValueChangeListener(e -> refreshAdminGrid());

        Button refreshBtn = new Button(new Icon(VaadinIcon.REFRESH));
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        refreshBtn.addClickListener(e -> {
            adminSearchField.clear();
            typeFilter.clear();
            refreshAdminGrid();
        });

        toolbar.add(adminSearchField, typeFilter, refreshBtn);
        return toolbar;
    }

    private Grid<Contact> createAdminGrid() {
        grid.addColumn(Contact::getNom)
            .setHeader("Nom")
            .setSortable(true)
            .setAutoWidth(true);

        grid.addColumn(contact -> contact.getType() != null ? contact.getType().getLabel() : "")
            .setHeader("Type")
            .setSortable(true)
            .setAutoWidth(true);

        grid.addColumn(Contact::getFonction)
            .setHeader("Fonction")
            .setSortable(true)
            .setAutoWidth(true);

        grid.addColumn(Contact::getTelephone)
            .setHeader("Téléphone")
            .setAutoWidth(true);

        grid.addColumn(Contact::getEmail)
            .setHeader("Email")
            .setAutoWidth(true);

        grid.addColumn(Contact::getVille)
            .setHeader("Ville")
            .setSortable(true)
            .setAutoWidth(true);

        grid.addComponentColumn(contact -> {
            Span badge = new Span(contact.isActif() ? "Actif" : "Inactif");
            badge.getStyle()
                .set("padding", "0.25rem 0.75rem")
                .set("border-radius", "12px")
                .set("font-size", "0.8rem")
                .set("font-weight", "500")
                .set("background", contact.isActif() ? "#dcfce7" : "#fee2e2")
                .set("color", contact.isActif() ? "#166534" : "#991b1b");
            return badge;
        }).setHeader("Statut").setAutoWidth(true);

        grid.addComponentColumn(contact -> {
            HorizontalLayout actions = new HorizontalLayout();
            actions.setSpacing(true);

            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            editBtn.getStyle().set("color", "#2c5aa0");
            editBtn.addClickListener(e -> openContactDialog(contact));

            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> confirmDelete(contact));

            actions.add(editBtn, deleteBtn);
            return actions;
        }).setHeader("Actions").setAutoWidth(true);

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.setHeight("100%");
        grid.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)")
            .set("margin-top", "1rem");

        return grid;
    }

    private void refreshAdminGrid() {
        List<Contact> contacts;
        String searchTerm = searchField.getValue();
        TypeContact selectedType = typeFilter.getValue();

        if (selectedType != null) {
            contacts = contactService.findByType(selectedType);
        } else {
            contacts = contactService.findAll();
        }

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearch = searchTerm.toLowerCase();
            contacts = contacts.stream()
                .filter(c ->
                    (c.getNom() != null && c.getNom().toLowerCase().contains(lowerSearch)) ||
                    (c.getEmail() != null && c.getEmail().toLowerCase().contains(lowerSearch)) ||
                    (c.getVille() != null && c.getVille().toLowerCase().contains(lowerSearch))
                )
                .toList();
        }

        grid.setItems(contacts);
    }

    // ==================== DIALOGS ====================

    private void openContactDialog(Contact contact) {
        boolean isNew = contact.getId() == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Nouveau Contact" : "Modifier Contact");
        dialog.setWidth("700px");
        dialog.setModal(true);
        dialog.setDraggable(true);

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        TextField nomField = new TextField("Nom");
        nomField.setRequired(true);
        nomField.setWidthFull();

        ComboBox<TypeContact> typeField = new ComboBox<>("Type");
        typeField.setItems(TypeContact.values());
        typeField.setItemLabelGenerator(TypeContact::getLabel);
        typeField.setRequired(true);
        typeField.setWidthFull();

        TextField fonctionField = new TextField("Fonction");
        fonctionField.setWidthFull();

        TextField telephoneField = new TextField("Téléphone");
        telephoneField.setWidthFull();

        TextField telephoneMobileField = new TextField("Téléphone Mobile");
        telephoneMobileField.setWidthFull();

        TextField faxField = new TextField("Fax");
        faxField.setWidthFull();

        TextField emailField = new TextField("Email");
        emailField.setWidthFull();

        TextField siteWebField = new TextField("Site Web");
        siteWebField.setWidthFull();

        TextArea adresseField = new TextArea("Adresse");
        adresseField.setWidthFull();

        TextField villeField = new TextField("Ville");
        villeField.setWidthFull();

        TextField regionField = new TextField("Région");
        regionField.setWidthFull();

        TextField codePostalField = new TextField("Code Postal");
        codePostalField.setWidthFull();

        TextArea horairesField = new TextArea("Horaires");
        horairesField.setPlaceholder("Lun-Ven: 8h30-16h30");
        horairesField.setWidthFull();

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();

        IntegerField ordreField = new IntegerField("Ordre d'affichage");
        ordreField.setMin(0);
        ordreField.setWidthFull();

        Checkbox actifField = new Checkbox("Actif");

        form.add(nomField, typeField);
        form.add(fonctionField, emailField);
        form.add(telephoneField, telephoneMobileField);
        form.add(faxField, siteWebField);
        form.add(adresseField);
        form.setColspan(adresseField, 2);
        form.add(villeField, regionField);
        form.add(codePostalField, ordreField);
        form.add(horairesField);
        form.setColspan(horairesField, 2);
        form.add(descriptionField);
        form.setColspan(descriptionField, 2);
        form.add(actifField);

        Binder<Contact> binder = new BeanValidationBinder<>(Contact.class);
        binder.forField(nomField).asRequired("Le nom est requis").bind(Contact::getNom, Contact::setNom);
        binder.forField(typeField).asRequired("Le type est requis").bind(Contact::getType, Contact::setType);
        binder.bind(fonctionField, Contact::getFonction, Contact::setFonction);
        binder.bind(telephoneField, Contact::getTelephone, Contact::setTelephone);
        binder.bind(telephoneMobileField, Contact::getTelephoneMobile, Contact::setTelephoneMobile);
        binder.bind(faxField, Contact::getFax, Contact::setFax);
        binder.bind(emailField, Contact::getEmail, Contact::setEmail);
        binder.bind(siteWebField, Contact::getSiteWeb, Contact::setSiteWeb);
        binder.bind(adresseField, Contact::getAdresse, Contact::setAdresse);
        binder.bind(villeField, Contact::getVille, Contact::setVille);
        binder.bind(regionField, Contact::getRegion, Contact::setRegion);
        binder.bind(codePostalField, Contact::getCodePostal, Contact::setCodePostal);
        binder.bind(horairesField, Contact::getHoraires, Contact::setHoraires);
        binder.bind(descriptionField, Contact::getDescription, Contact::setDescription);
        binder.bind(ordreField, Contact::getOrdre, Contact::setOrdre);
        binder.bind(actifField, Contact::isActif, Contact::setActif);

        binder.readBean(contact);

        Button saveBtn = new Button("Enregistrer", new Icon(VaadinIcon.CHECK));
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("background", "#3b6b35");
        saveBtn.addClickListener(e -> {
            if (binder.writeBeanIfValid(contact)) {
                contactService.save(contact);
                if (isAdmin) {
                    refreshAdminGrid();
                } else {
                    refreshCards();
                }
                dialog.close();
                Notification.show(isNew ? "Contact créé avec succès" : "Contact mis à jour",
                    3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });

        Button cancelBtn = new Button("Annuler", e -> dialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout buttons = new HorizontalLayout(saveBtn, cancelBtn);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        dialog.add(form);
        dialog.getFooter().add(buttons);
        dialog.open();
    }

    private void confirmDelete(Contact contact) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Supprimer le contact");
        dialog.setText("Êtes-vous sûr de vouloir supprimer le contact \"" + contact.getNom() + "\" ?");
        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");
        dialog.setConfirmText("Supprimer");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            contactService.delete(contact);
            refreshAdminGrid();
            Notification.show("Contact supprimé", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        dialog.open();
    }
}
