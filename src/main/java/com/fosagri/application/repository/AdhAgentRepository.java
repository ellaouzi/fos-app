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
    
    @Query("SELECT a FROM AdhAgent a WHERE UPPER(a.NOM_AG) LIKE UPPER(CONCAT('%', :nom, '%'))")
    List<AdhAgent> findByNomContainingIgnoreCase(@Param("nom") String nom);
    
    @Query("SELECT a FROM AdhAgent a WHERE UPPER(a.PR_AG) LIKE UPPER(CONCAT('%', :prenom, '%'))")
    List<AdhAgent> findByPrenomContainingIgnoreCase(@Param("prenom") String prenom);
    
    @Query("SELECT a FROM AdhAgent a WHERE UPPER(a.NOM_AG) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR UPPER(a.PR_AG) LIKE UPPER(CONCAT('%', :searchTerm, '%')) OR UPPER(a.idAdh) LIKE UPPER(CONCAT('%', :searchTerm, '%'))")
    List<AdhAgent> findBySearchTerm(@Param("searchTerm") String searchTerm);
    
    List<AdhAgent> findByVille(String ville);
}