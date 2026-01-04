package com.fosagri.application.repository;

import com.fosagri.application.model.AdhAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdhAgentRepository extends JpaRepository<AdhAgent, Integer> {

    Optional<AdhAgent> findByIdAdh(String idAdh);

    Optional<AdhAgent> findByCodAg(String codAg);

    @Query("SELECT a FROM AdhAgent a WHERE a.CIN_AG = :cin")
    Optional<AdhAgent> findByCin(@Param("cin") String cin);
    
    @Query("SELECT a FROM AdhAgent a WHERE UPPER(a.NOM_AG) LIKE UPPER(CONCAT('%', :nom, '%'))")
    List<AdhAgent> findByNomContainingIgnoreCase(@Param("nom") String nom);
    
    @Query("SELECT a FROM AdhAgent a WHERE UPPER(a.PR_AG) LIKE UPPER(CONCAT('%', :prenom, '%'))")
    List<AdhAgent> findByPrenomContainingIgnoreCase(@Param("prenom") String prenom);
    
    @Query("SELECT a FROM AdhAgent a WHERE UPPER(a.NOM_AG) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR UPPER(a.PR_AG) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR UPPER(a.idAdh) LIKE UPPER(CONCAT('%', :searchTerm, '%'))")
    List<AdhAgent> findBySearchTerm(@Param("searchTerm") String searchTerm);

    // Fuzzy search - searches across multiple fields with flexible matching
    @Query("SELECT DISTINCT a FROM AdhAgent a WHERE " +
           "LOWER(a.NOM_AG) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(a.PR_AG) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(CONCAT(a.NOM_AG, ' ', a.PR_AG)) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(CONCAT(a.PR_AG, ' ', a.NOM_AG)) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(a.idAdh) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(a.CIN_AG) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(a.codAg) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(a.ville) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(a.num_Tel) LIKE LOWER(CONCAT('%', :term, '%'))")
    List<AdhAgent> fuzzySearch(@Param("term") String term);

    List<AdhAgent> findByVille(String ville);
}