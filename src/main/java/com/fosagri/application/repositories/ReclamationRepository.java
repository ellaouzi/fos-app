package com.fosagri.application.repositories;

import com.fosagri.application.entities.Reclamation;
import com.fosagri.application.entities.Reclamation.StatutReclamation;
import com.fosagri.application.entities.Reclamation.TypeReclamation;
import com.fosagri.application.model.AdhAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {
    
    // Find by agent
    List<Reclamation> findByAgentOrderByDateCreationDesc(AdhAgent agent);
    
    // Find by status
    List<Reclamation> findByStatutOrderByDateCreationDesc(StatutReclamation statut);
    
    // Find by type
    List<Reclamation> findByTypeOrderByDateCreationDesc(TypeReclamation type);
    
    // Find by date range
    List<Reclamation> findByDateCreationBetweenOrderByDateCreationDesc(Date startDate, Date endDate);
    
    // Search by object or detail containing text
    @Query("SELECT r FROM Reclamation r WHERE " +
           "LOWER(r.objet) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(r.detail) LIKE LOWER(CONCAT('%', :searchText, '%')) " +
           "ORDER BY r.dateCreation DESC")
    List<Reclamation> searchByObjetOrDetail(@Param("searchText") String searchText);
    
    // Find all ordered by creation date desc
    List<Reclamation> findAllByOrderByDateCreationDesc();
    
    // Find all without loading agent data (to avoid LOB issues)
    @Query("SELECT r FROM Reclamation r ORDER BY r.dateCreation DESC")
    List<Reclamation> findAllWithoutAgentDataOrderByDateCreationDesc();
    
    // Get agent name for reclamation (avoiding LOB fields)
    @Query("SELECT a.NOM_AG, a.PR_AG FROM Reclamation r JOIN r.agent a WHERE r.id = :id")
    Object[] findAgentNameByReclamationId(@Param("id") Long id);
    
    // Count by status
    long countByStatut(StatutReclamation statut);
    
    // Count by agent
    long countByAgent(AdhAgent agent);
    
    // Find recent reclamations (last 30 days)
    @Query("SELECT r FROM Reclamation r WHERE r.dateCreation >= :thirtyDaysAgo ORDER BY r.dateCreation DESC")
    List<Reclamation> findRecentReclamations(@Param("thirtyDaysAgo") Date thirtyDaysAgo);
    
    // Find pending reclamations (new or in progress)
    @Query("SELECT r FROM Reclamation r WHERE r.statut IN ('NOUVELLE', 'EN_COURS') ORDER BY r.dateCreation DESC")
    List<Reclamation> findPendingReclamations();
}