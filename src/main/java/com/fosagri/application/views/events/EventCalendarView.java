package com.fosagri.application.views.events;

import com.fosagri.application.entities.Event;
import com.fosagri.application.entities.Event.TypeEvent;
import com.fosagri.application.entities.Event.CategorieEvent;
import com.fosagri.application.services.EventService;
import com.fosagri.application.views.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@PageTitle("Calendrier des Événements")
@Route(value = "events", layout = MainLayout.class)
@Menu(order = 3, icon = LineAwesomeIconUrl.CALENDAR_ALT)
@PermitAll
public class EventCalendarView extends VerticalLayout {

    private final EventService eventService;
    private YearMonth currentMonth;
    private final Div calendarGrid = new Div();
    private final VerticalLayout eventsList = new VerticalLayout();
    private final H3 monthLabel = new H3();
    private final Locale locale = Locale.FRENCH;
    private final boolean isAdmin;

    public EventCalendarView(EventService eventService,
                             com.fosagri.application.security.AuthenticatedUser authenticatedUser) {
        this.eventService = eventService;
        this.currentMonth = YearMonth.now();

        // Check if user is admin
        this.isAdmin = authenticatedUser.get()
            .map(u -> u.getAuthorities().stream()
                .anyMatch(a -> "ADMIN".equals(a.getRole())))
            .orElse(false);

        addClassName("events-calendar-view");
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background", "#f8fafc");

        add(createHeader());
        add(createMainContent());

        refreshCalendar();
        refreshEventsList();
    }

    private HorizontalLayout createHeader() {
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.getStyle()
            .set("background", "linear-gradient(135deg, #3b6b35 0%, #2c5aa0 100%)")
            .set("border-radius", "12px")
            .set("padding", "1.5rem 2rem")
            .set("color", "white");

        VerticalLayout titleSection = new VerticalLayout();
        titleSection.setSpacing(false);
        titleSection.setPadding(false);

        H2 title = new H2("Calendrier des Événements");
        title.getStyle()
            .set("color", "white")
            .set("margin", "0")
            .set("font-size", "1.5rem");

        Span subtitle = new Span("Retrouvez tous les événements et activités FOS-Agri");
        subtitle.getStyle()
            .set("color", "rgba(255,255,255,0.85)")
            .set("font-size", "0.9rem");

        titleSection.add(title, subtitle);

        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        if (isAdmin) {
            Button addButton = new Button("Nouvel Événement", new Icon(VaadinIcon.PLUS));
            addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addButton.getStyle()
                .set("background", "white")
                .set("color", "#3b6b35");
            addButton.addClickListener(e -> openEventDialog(new Event()));
            actions.add(addButton);
        }

        header.add(titleSection, actions);
        return header;
    }

    private HorizontalLayout createMainContent() {
        HorizontalLayout content = new HorizontalLayout();
        content.setWidthFull();
        content.setHeight("calc(100vh - 250px)");
        content.setSpacing(true);

        // Calendar section (left)
        VerticalLayout calendarSection = new VerticalLayout();
        calendarSection.setWidth("65%");
        calendarSection.setPadding(true);
        calendarSection.setSpacing(true);
        calendarSection.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)");

        calendarSection.add(createCalendarNavigation());
        calendarSection.add(createCalendarHeader());
        calendarSection.add(calendarGrid);

        // Events list section (right)
        VerticalLayout eventsSection = new VerticalLayout();
        eventsSection.setWidth("35%");
        eventsSection.setPadding(true);
        eventsSection.setSpacing(true);
        eventsSection.getStyle()
            .set("background", "white")
            .set("border-radius", "12px")
            .set("box-shadow", "0 2px 8px rgba(0,0,0,0.05)")
            .set("overflow-y", "auto");

        H3 eventsTitle = new H3("Événements à venir");
        eventsTitle.getStyle()
            .set("margin", "0 0 1rem 0")
            .set("color", "#1e293b");

        eventsList.setPadding(false);
        eventsList.setSpacing(true);

        eventsSection.add(eventsTitle, eventsList);

        content.add(calendarSection, eventsSection);
        return content;
    }

    private HorizontalLayout createCalendarNavigation() {
        HorizontalLayout nav = new HorizontalLayout();
        nav.setWidthFull();
        nav.setAlignItems(FlexComponent.Alignment.CENTER);
        nav.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Button prevBtn = new Button(new Icon(VaadinIcon.ANGLE_LEFT));
        prevBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        prevBtn.addClickListener(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });

        Button nextBtn = new Button(new Icon(VaadinIcon.ANGLE_RIGHT));
        nextBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        nextBtn.addClickListener(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });

        Button todayBtn = new Button("Aujourd'hui");
        todayBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        todayBtn.addClickListener(e -> {
            currentMonth = YearMonth.now();
            refreshCalendar();
        });

        monthLabel.getStyle()
            .set("margin", "0")
            .set("color", "#1e293b")
            .set("font-size", "1.25rem");

        HorizontalLayout leftNav = new HorizontalLayout(prevBtn, nextBtn, todayBtn);
        leftNav.setAlignItems(FlexComponent.Alignment.CENTER);
        leftNav.setSpacing(true);

        nav.add(leftNav, monthLabel);
        return nav;
    }

    private Div createCalendarHeader() {
        Div header = new Div();
        header.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(7, 1fr)")
            .set("gap", "2px")
            .set("margin-bottom", "0.5rem");

        String[] days = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};
        for (String day : days) {
            Span daySpan = new Span(day);
            daySpan.getStyle()
                .set("text-align", "center")
                .set("font-weight", "600")
                .set("color", "#64748b")
                .set("font-size", "0.85rem")
                .set("padding", "0.5rem");
            header.add(daySpan);
        }

        return header;
    }

    private void refreshCalendar() {
        calendarGrid.removeAll();
        calendarGrid.getStyle()
            .set("display", "grid")
            .set("grid-template-columns", "repeat(7, 1fr)")
            .set("gap", "2px");

        // Update month label
        String monthName = currentMonth.getMonth().getDisplayName(TextStyle.FULL, locale);
        monthLabel.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1) + " " + currentMonth.getYear());

        // Get events for the month
        List<Event> monthEvents = eventService.findByMonth(currentMonth.getYear(), currentMonth.getMonthValue());

        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue(); // Monday = 1

        // Add empty cells for days before the first of the month
        for (int i = 1; i < dayOfWeekValue; i++) {
            Div emptyCell = new Div();
            emptyCell.getStyle()
                .set("min-height", "80px")
                .set("background", "#f8fafc");
            calendarGrid.add(emptyCell);
        }

        // Add cells for each day of the month
        int daysInMonth = currentMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = currentMonth.atDay(day);
            Div dayCell = createDayCell(date, monthEvents, today);
            calendarGrid.add(dayCell);
        }
    }

    private Div createDayCell(LocalDate date, List<Event> monthEvents, LocalDate today) {
        Div cell = new Div();
        cell.getStyle()
            .set("min-height", "80px")
            .set("border", "1px solid #e2e8f0")
            .set("border-radius", "4px")
            .set("padding", "0.25rem")
            .set("cursor", "pointer")
            .set("transition", "background 0.2s");

        boolean isToday = date.equals(today);
        boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY ||
                           date.getDayOfWeek() == DayOfWeek.SUNDAY;

        if (isToday) {
            cell.getStyle().set("background", "#dbeafe").set("border-color", "#3b82f6");
        } else if (isWeekend) {
            cell.getStyle().set("background", "#fef3c7");
        } else {
            cell.getStyle().set("background", "white");
        }

        // Day number
        Span dayNumber = new Span(String.valueOf(date.getDayOfMonth()));
        dayNumber.getStyle()
            .set("font-weight", isToday ? "700" : "500")
            .set("color", isToday ? "#1d4ed8" : "#374151")
            .set("font-size", "0.9rem");
        cell.add(dayNumber);

        // Events for this day
        List<Event> dayEvents = monthEvents.stream()
            .filter(e -> !date.isBefore(e.getDateDebut()) && !date.isAfter(e.getDateFin()))
            .limit(3)
            .toList();

        for (Event event : dayEvents) {
            Div eventTag = new Div();
            eventTag.setText(truncate(event.getTitre(), 12));
            eventTag.getStyle()
                .set("background", event.getCouleur() != null ? event.getCouleur() : "#3b6b35")
                .set("color", "white")
                .set("font-size", "0.7rem")
                .set("padding", "1px 4px")
                .set("border-radius", "3px")
                .set("margin-top", "2px")
                .set("overflow", "hidden")
                .set("text-overflow", "ellipsis")
                .set("white-space", "nowrap");
            cell.add(eventTag);
        }

        // Show more indicator
        long totalEvents = monthEvents.stream()
            .filter(e -> !date.isBefore(e.getDateDebut()) && !date.isAfter(e.getDateFin()))
            .count();
        if (totalEvents > 3) {
            Span more = new Span("+" + (totalEvents - 3) + " plus");
            more.getStyle()
                .set("font-size", "0.65rem")
                .set("color", "#6b7280")
                .set("margin-top", "2px");
            cell.add(more);
        }

        // Click handler to show day events
        cell.addClickListener(e -> showDayEvents(date));

        return cell;
    }

    private void showDayEvents(LocalDate date) {
        List<Event> events = eventService.findByDate(date);

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Événements du " + date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", locale)));
        dialog.setWidth("500px");

        VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        content.setSpacing(true);

        if (events.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement pour cette date");
            noEvents.getStyle().set("color", "#6b7280");
            content.add(noEvents);
        } else {
            for (Event event : events) {
                content.add(createEventCard(event, true));
            }
        }

        if (isAdmin) {
            Button addBtn = new Button("Ajouter un événement", new Icon(VaadinIcon.PLUS));
            addBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            addBtn.addClickListener(e -> {
                dialog.close();
                Event newEvent = new Event();
                newEvent.setDateDebut(date);
                newEvent.setDateFin(date);
                openEventDialog(newEvent);
            });
            content.add(addBtn);
        }

        dialog.add(content);
        dialog.getFooter().add(new Button("Fermer", e -> dialog.close()));
        dialog.open();
    }

    private void refreshEventsList() {
        eventsList.removeAll();

        List<Event> upcoming = eventService.findUpcoming();

        if (upcoming.isEmpty()) {
            Paragraph noEvents = new Paragraph("Aucun événement à venir");
            noEvents.getStyle().set("color", "#6b7280");
            eventsList.add(noEvents);
        } else {
            upcoming.stream().limit(10).forEach(event -> {
                eventsList.add(createEventCard(event, false));
            });
        }
    }

    private Div createEventCard(Event event, boolean showActions) {
        Div card = new Div();
        card.getStyle()
            .set("background", "#f8fafc")
            .set("border-radius", "8px")
            .set("padding", "1rem")
            .set("border-left", "4px solid " + (event.getCouleur() != null ? event.getCouleur() : "#3b6b35"));

        // Title
        Span title = new Span(event.getTitre());
        title.getStyle()
            .set("font-weight", "600")
            .set("color", "#1e293b")
            .set("display", "block");

        // Date and time
        String dateText = formatEventDate(event);
        Span date = new Span(dateText);
        date.getStyle()
            .set("font-size", "0.85rem")
            .set("color", "#64748b")
            .set("display", "block")
            .set("margin-top", "0.25rem");

        // Type badge
        Span typeBadge = new Span(event.getType() != null ? event.getType().getLabel() : "");
        typeBadge.getStyle()
            .set("background", "#e0f2fe")
            .set("color", "#0369a1")
            .set("padding", "0.15rem 0.5rem")
            .set("border-radius", "10px")
            .set("font-size", "0.75rem")
            .set("margin-top", "0.5rem")
            .set("display", "inline-block");

        card.add(title, date, typeBadge);

        // Location if available
        if (event.getLieu() != null && !event.getLieu().isEmpty()) {
            HorizontalLayout locationRow = new HorizontalLayout();
            locationRow.setSpacing(true);
            locationRow.setAlignItems(FlexComponent.Alignment.CENTER);
            locationRow.getStyle().set("margin-top", "0.5rem");

            Icon locationIcon = VaadinIcon.MAP_MARKER.create();
            locationIcon.setSize("14px");
            locationIcon.getStyle().set("color", "#6b7280");

            Span location = new Span(event.getLieu());
            location.getStyle()
                .set("font-size", "0.8rem")
                .set("color", "#6b7280");

            locationRow.add(locationIcon, location);
            card.add(locationRow);
        }

        // Admin actions
        if (showActions && isAdmin) {
            HorizontalLayout actions = new HorizontalLayout();
            actions.getStyle().set("margin-top", "0.75rem");
            actions.setSpacing(true);

            Button editBtn = new Button("Modifier", new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
            editBtn.addClickListener(e -> openEventDialog(event));

            Button deleteBtn = new Button("Supprimer", new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> confirmDelete(event));

            actions.add(editBtn, deleteBtn);
            card.add(actions);
        }

        return card;
    }

    private String formatEventDate(Event event) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", locale);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        StringBuilder sb = new StringBuilder();

        if (event.isMultiDay()) {
            sb.append(event.getDateDebut().format(dateFormatter))
              .append(" - ")
              .append(event.getDateFin().format(dateFormatter));
        } else {
            sb.append(event.getDateDebut().format(dateFormatter));
        }

        if (!event.isJourneeEntiere() && event.getHeureDebut() != null) {
            sb.append(" à ").append(event.getHeureDebut().format(timeFormatter));
            if (event.getHeureFin() != null) {
                sb.append(" - ").append(event.getHeureFin().format(timeFormatter));
            }
        } else if (event.isJourneeEntiere()) {
            sb.append(" (Journée entière)");
        }

        return sb.toString();
    }

    private void openEventDialog(Event event) {
        boolean isNew = event.getId() == null;
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(isNew ? "Nouvel Événement" : "Modifier Événement");
        dialog.setWidth("700px");
        dialog.setModal(true);
        dialog.setDraggable(true);

        FormLayout form = new FormLayout();
        form.setResponsiveSteps(
            new FormLayout.ResponsiveStep("0", 1),
            new FormLayout.ResponsiveStep("500px", 2)
        );

        TextField titreField = new TextField("Titre");
        titreField.setRequired(true);
        titreField.setWidthFull();

        ComboBox<TypeEvent> typeField = new ComboBox<>("Type");
        typeField.setItems(TypeEvent.values());
        typeField.setItemLabelGenerator(TypeEvent::getLabel);
        typeField.setRequired(true);
        typeField.setWidthFull();

        ComboBox<CategorieEvent> categorieField = new ComboBox<>("Catégorie");
        categorieField.setItems(CategorieEvent.values());
        categorieField.setItemLabelGenerator(CategorieEvent::getLabel);
        categorieField.setWidthFull();

        DatePicker dateDebutField = new DatePicker("Date de début");
        dateDebutField.setRequired(true);
        dateDebutField.setWidthFull();

        DatePicker dateFinField = new DatePicker("Date de fin");
        dateFinField.setRequired(true);
        dateFinField.setWidthFull();

        Checkbox journeeEntiereField = new Checkbox("Journée entière");

        TimePicker heureDebutField = new TimePicker("Heure de début");
        heureDebutField.setWidthFull();

        TimePicker heureFinField = new TimePicker("Heure de fin");
        heureFinField.setWidthFull();

        // Toggle time fields based on journeeEntiere
        journeeEntiereField.addValueChangeListener(e -> {
            heureDebutField.setEnabled(!e.getValue());
            heureFinField.setEnabled(!e.getValue());
        });

        TextField lieuField = new TextField("Lieu");
        lieuField.setWidthFull();

        TextField organisateurField = new TextField("Organisateur");
        organisateurField.setWidthFull();

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();

        TextField couleurField = new TextField("Couleur (hex)");
        couleurField.setPlaceholder("#3b6b35");
        couleurField.setWidthFull();

        IntegerField nombrePlacesField = new IntegerField("Nombre de places");
        nombrePlacesField.setMin(0);
        nombrePlacesField.setWidthFull();

        TextField lienInscriptionField = new TextField("Lien d'inscription");
        lienInscriptionField.setWidthFull();

        Checkbox actifField = new Checkbox("Actif");
        Checkbox publiqueField = new Checkbox("Visible pour tous");

        form.add(titreField, typeField);
        form.add(categorieField, organisateurField);
        form.add(dateDebutField, dateFinField);
        form.add(journeeEntiereField);
        form.setColspan(journeeEntiereField, 2);
        form.add(heureDebutField, heureFinField);
        form.add(lieuField, couleurField);
        form.add(descriptionField);
        form.setColspan(descriptionField, 2);
        form.add(nombrePlacesField, lienInscriptionField);
        form.add(actifField, publiqueField);

        // Bind values
        Binder<Event> binder = new BeanValidationBinder<>(Event.class);
        binder.forField(titreField).asRequired("Le titre est requis").bind(Event::getTitre, Event::setTitre);
        binder.forField(typeField).asRequired("Le type est requis").bind(Event::getType, Event::setType);
        binder.bind(categorieField, Event::getCategorie, Event::setCategorie);
        binder.forField(dateDebutField).asRequired("La date de début est requise").bind(Event::getDateDebut, Event::setDateDebut);
        binder.forField(dateFinField).asRequired("La date de fin est requise").bind(Event::getDateFin, Event::setDateFin);
        binder.bind(journeeEntiereField, Event::isJourneeEntiere, Event::setJourneeEntiere);
        binder.bind(heureDebutField, Event::getHeureDebut, Event::setHeureDebut);
        binder.bind(heureFinField, Event::getHeureFin, Event::setHeureFin);
        binder.bind(lieuField, Event::getLieu, Event::setLieu);
        binder.bind(organisateurField, Event::getOrganisateur, Event::setOrganisateur);
        binder.bind(descriptionField, Event::getDescription, Event::setDescription);
        binder.bind(couleurField, Event::getCouleur, Event::setCouleur);
        binder.bind(nombrePlacesField, Event::getNombrePlaces, Event::setNombrePlaces);
        binder.bind(lienInscriptionField, Event::getLienInscription, Event::setLienInscription);
        binder.bind(actifField, Event::isActif, Event::setActif);
        binder.bind(publiqueField, Event::isPublique, Event::setPublique);

        binder.readBean(event);

        // Set default values for new event
        if (isNew) {
            actifField.setValue(true);
            publiqueField.setValue(true);
            couleurField.setValue("#3b6b35");
        }

        // Buttons
        Button saveBtn = new Button("Enregistrer", new Icon(VaadinIcon.CHECK));
        saveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveBtn.getStyle().set("background", "#3b6b35");
        saveBtn.addClickListener(e -> {
            if (binder.writeBeanIfValid(event)) {
                eventService.save(event);
                refreshCalendar();
                refreshEventsList();
                dialog.close();
                Notification.show(isNew ? "Événement créé avec succès" : "Événement mis à jour",
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

    private void confirmDelete(Event event) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Supprimer l'événement");
        dialog.setText("Êtes-vous sûr de vouloir supprimer l'événement \"" + event.getTitre() + "\" ?");
        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");
        dialog.setConfirmText("Supprimer");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> {
            eventService.delete(event);
            refreshCalendar();
            refreshEventsList();
            Notification.show("Événement supprimé", 3000, Notification.Position.TOP_CENTER)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        dialog.open();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 1) + "…";
    }
}
