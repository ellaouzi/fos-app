package com.fosagri.application.repositories;

import com.fosagri.application.entities.Event;
import com.fosagri.application.entities.Event.TypeEvent;
import com.fosagri.application.entities.Event.CategorieEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByActifTrue();

    List<Event> findByActifTrueOrderByDateDebutAsc();

    List<Event> findByType(TypeEvent type);

    List<Event> findByCategorie(CategorieEvent categorie);

    @Query("SELECT e FROM Event e WHERE e.actif = true AND e.publique = true ORDER BY e.dateDebut ASC")
    List<Event> findAllPublicActive();

    @Query("SELECT e FROM Event e WHERE e.actif = true AND " +
           "((e.dateDebut BETWEEN :start AND :end) OR " +
           "(e.dateFin BETWEEN :start AND :end) OR " +
           "(e.dateDebut <= :start AND e.dateFin >= :end))")
    List<Event> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e FROM Event e WHERE e.actif = true AND e.publique = true AND " +
           "((e.dateDebut BETWEEN :start AND :end) OR " +
           "(e.dateFin BETWEEN :start AND :end) OR " +
           "(e.dateDebut <= :start AND e.dateFin >= :end)) " +
           "ORDER BY e.dateDebut ASC")
    List<Event> findPublicByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT e FROM Event e WHERE e.actif = true AND e.dateDebut >= :date ORDER BY e.dateDebut ASC")
    List<Event> findUpcoming(@Param("date") LocalDate date);

    @Query("SELECT e FROM Event e WHERE e.actif = true AND e.publique = true AND e.dateDebut >= :date ORDER BY e.dateDebut ASC")
    List<Event> findUpcomingPublic(@Param("date") LocalDate date);

    @Query("SELECT e FROM Event e WHERE e.actif = true AND " +
           ":date BETWEEN e.dateDebut AND e.dateFin ORDER BY e.heureDebut ASC")
    List<Event> findByDate(@Param("date") LocalDate date);

    @Query("SELECT e FROM Event e WHERE e.actif = true AND e.type = :type AND " +
           "e.dateDebut >= :date ORDER BY e.dateDebut ASC")
    List<Event> findUpcomingByType(@Param("type") TypeEvent type, @Param("date") LocalDate date);

    @Query("SELECT e FROM Event e WHERE e.actif = true AND e.categorie = :categorie AND " +
           "e.dateDebut >= :date ORDER BY e.dateDebut ASC")
    List<Event> findUpcomingByCategorie(@Param("categorie") CategorieEvent categorie, @Param("date") LocalDate date);

    @Query("SELECT e FROM Event e WHERE e.actif = true AND " +
           "(LOWER(e.titre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(e.description) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(e.lieu) LIKE LOWER(CONCAT('%', :term, '%')))")
    List<Event> searchEvents(@Param("term") String term);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.actif = true AND e.dateDebut >= :date")
    long countUpcoming(@Param("date") LocalDate date);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.actif = true AND e.type = :type")
    long countByType(@Param("type") TypeEvent type);

    @Query("SELECT e FROM Event e WHERE e.actif = true AND " +
           "e.dateDebut = :date ORDER BY e.heureDebut ASC NULLS LAST")
    List<Event> findEventsStartingOn(@Param("date") LocalDate date);
}
