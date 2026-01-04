package com.fosagri.application.repositories;

import com.fosagri.application.entities.Contact;
import com.fosagri.application.entities.Contact.TypeContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByType(TypeContact type);

    List<Contact> findByActifTrue();

    List<Contact> findByActifTrueOrderByOrdreAsc();

    List<Contact> findByTypeAndActifTrue(TypeContact type);

    @Query("SELECT c FROM Contact c WHERE c.actif = true ORDER BY c.ordre ASC, c.nom ASC")
    List<Contact> findAllActiveOrdered();

    @Query("SELECT c FROM Contact c WHERE " +
           "(LOWER(c.nom) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.fonction) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.ville) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :term, '%'))) " +
           "AND c.actif = true")
    List<Contact> searchContacts(@Param("term") String term);

    @Query("SELECT c FROM Contact c WHERE c.type = :type AND c.actif = true ORDER BY c.ordre ASC, c.nom ASC")
    List<Contact> findByTypeOrdered(@Param("type") TypeContact type);

    List<Contact> findByVilleIgnoreCaseAndActifTrue(String ville);

    List<Contact> findByRegionIgnoreCaseAndActifTrue(String region);

    @Query("SELECT DISTINCT c.ville FROM Contact c WHERE c.ville IS NOT NULL AND c.actif = true ORDER BY c.ville")
    List<String> findDistinctVilles();

    @Query("SELECT DISTINCT c.region FROM Contact c WHERE c.region IS NOT NULL AND c.actif = true ORDER BY c.region")
    List<String> findDistinctRegions();

    @Query("SELECT COUNT(c) FROM Contact c WHERE c.type = :type AND c.actif = true")
    long countByType(@Param("type") TypeContact type);
}
