package com.fosagri.application.services;

import com.fosagri.application.entities.Event;
import com.fosagri.application.entities.Event.TypeEvent;
import com.fosagri.application.entities.Event.CategorieEvent;
import com.fosagri.application.repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    @Autowired
    private EventRepository repository;

    public List<Event> findAll() {
        return repository.findAll();
    }

    public List<Event> findAllActive() {
        return repository.findByActifTrueOrderByDateDebutAsc();
    }

    public List<Event> findAllPublicActive() {
        return repository.findAllPublicActive();
    }

    public Optional<Event> findById(Long id) {
        return repository.findById(id);
    }

    public Event save(Event event) {
        return repository.save(event);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public void delete(Event event) {
        repository.delete(event);
    }

    public List<Event> findByType(TypeEvent type) {
        return repository.findByType(type);
    }

    public List<Event> findByCategorie(CategorieEvent categorie) {
        return repository.findByCategorie(categorie);
    }

    public List<Event> findByDateRange(LocalDate start, LocalDate end) {
        return repository.findByDateRange(start, end);
    }

    public List<Event> findPublicByDateRange(LocalDate start, LocalDate end) {
        return repository.findPublicByDateRange(start, end);
    }

    public List<Event> findByMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return findByDateRange(start, end);
    }

    public List<Event> findPublicByMonth(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();
        return findPublicByDateRange(start, end);
    }

    public List<Event> findUpcoming() {
        return repository.findUpcoming(LocalDate.now());
    }

    public List<Event> findUpcomingPublic() {
        return repository.findUpcomingPublic(LocalDate.now());
    }

    public List<Event> findUpcomingByType(TypeEvent type) {
        return repository.findUpcomingByType(type, LocalDate.now());
    }

    public List<Event> findUpcomingByCategorie(CategorieEvent categorie) {
        return repository.findUpcomingByCategorie(categorie, LocalDate.now());
    }

    public List<Event> findByDate(LocalDate date) {
        return repository.findByDate(date);
    }

    public List<Event> findEventsStartingOn(LocalDate date) {
        return repository.findEventsStartingOn(date);
    }

    public List<Event> search(String term) {
        if (term == null || term.trim().isEmpty()) {
            return findAllActive();
        }
        return repository.searchEvents(term.trim());
    }

    public long count() {
        return repository.count();
    }

    public long countUpcoming() {
        return repository.countUpcoming(LocalDate.now());
    }

    public long countByType(TypeEvent type) {
        return repository.countByType(type);
    }

    public void deactivate(Long id) {
        repository.findById(id).ifPresent(event -> {
            event.setActif(false);
            repository.save(event);
        });
    }

    public void activate(Long id) {
        repository.findById(id).ifPresent(event -> {
            event.setActif(true);
            repository.save(event);
        });
    }

    public List<Event> findTodayEvents() {
        return findByDate(LocalDate.now());
    }

    public List<Event> findThisWeekEvents() {
        LocalDate today = LocalDate.now();
        LocalDate endOfWeek = today.plusDays(7);
        return findByDateRange(today, endOfWeek);
    }

    public List<Event> findThisMonthEvents() {
        LocalDate today = LocalDate.now();
        return findByMonth(today.getYear(), today.getMonthValue());
    }
}
