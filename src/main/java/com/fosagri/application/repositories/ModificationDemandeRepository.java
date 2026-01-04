package com.fosagri.application.repositories;

import com.fosagri.application.entities.ModificationDemande;
import com.fosagri.application.entities.ModificationDemande.StatutModification;
import com.fosagri.application.entities.ModificationDemande.TypeEntite;
import com.fosagri.application.model.AdhAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModificationDemandeRepository extends JpaRepository<ModificationDemande, Long> {

    // Find by ID with all fields
    @Query("SELECT m FROM ModificationDemande m LEFT JOIN FETCH m.agent WHERE m.id = :id")
    Optional<ModificationDemande> findByIdWithAgent(@Param("id") Long id);

    // Get documents JSON directly
    @Query(value = "SELECT documents_json FROM modification_demande WHERE id = :id", nativeQuery = true)
    String getDocumentsJsonById(@Param("id") Long id);

    // Find by agent
    List<ModificationDemande> findByAgent(AdhAgent agent);

    @Query("SELECT m FROM ModificationDemande m WHERE m.agent.adhAgentId = :agentId ORDER BY m.dateCreation DESC")
    List<ModificationDemande> findByAgentId(@Param("agentId") int agentId);

    // Find by status
    @Query("SELECT m FROM ModificationDemande m LEFT JOIN FETCH m.agent WHERE m.statut = :statut ORDER BY m.dateCreation DESC")
    List<ModificationDemande> findByStatut(@Param("statut") StatutModification statut);

    // Find pending modifications
    @Query("SELECT m FROM ModificationDemande m LEFT JOIN FETCH m.agent WHERE m.statut = 'EN_ATTENTE' ORDER BY m.dateCreation ASC")
    List<ModificationDemande> findPendingModifications();

    // Count by status
    @Query("SELECT COUNT(m) FROM ModificationDemande m WHERE m.statut = :statut")
    long countByStatut(@Param("statut") StatutModification statut);

    // Count pending
    @Query("SELECT COUNT(m) FROM ModificationDemande m WHERE m.statut = 'EN_ATTENTE'")
    long countPending();

    // Check if agent has pending modification for specific entity
    @Query("SELECT COUNT(m) FROM ModificationDemande m WHERE m.agent = :agent AND m.typeEntite = :type AND m.entiteId = :entiteId AND m.statut = 'EN_ATTENTE'")
    long countPendingByAgentAndEntity(@Param("agent") AdhAgent agent,
                                       @Param("type") TypeEntite type,
                                       @Param("entiteId") Integer entiteId);

    // Check if agent has any pending modification
    @Query("SELECT COUNT(m) FROM ModificationDemande m WHERE m.agent = :agent AND m.statut = 'EN_ATTENTE'")
    long countPendingByAgent(@Param("agent") AdhAgent agent);

    // Search modifications
    @Query("SELECT m FROM ModificationDemande m LEFT JOIN FETCH m.agent WHERE " +
           "m.agent.NOM_AG LIKE %:term% OR m.agent.PR_AG LIKE %:term% OR m.entiteLabel LIKE %:term%")
    List<ModificationDemande> searchModifications(@Param("term") String term);

    // Find by entity type and status
    List<ModificationDemande> findByTypeEntiteAndStatut(TypeEntite type, StatutModification statut);

    // Find by entity type
    List<ModificationDemande> findByTypeEntite(TypeEntite type);

    // Find all ordered by date
    @Query("SELECT m FROM ModificationDemande m LEFT JOIN FETCH m.agent ORDER BY m.dateCreation DESC")
    List<ModificationDemande> findAllOrderByDateDesc();

    // Count by agent
    @Query("SELECT COUNT(m) FROM ModificationDemande m WHERE m.agent = :agent")
    long countByAgent(@Param("agent") AdhAgent agent);

    // Find by agent and status
    List<ModificationDemande> findByAgentAndStatut(AdhAgent agent, StatutModification statut);
}
