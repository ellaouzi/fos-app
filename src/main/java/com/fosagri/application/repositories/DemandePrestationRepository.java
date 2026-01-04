package com.fosagri.application.repositories;

import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.entities.PrestationRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DemandePrestationRepository extends JpaRepository<DemandePrestation, Long> {
    
    List<DemandePrestation> findByAgent(AdhAgent agent);

    @Query("SELECT d FROM DemandePrestation d WHERE d.agent.adhAgentId = :agentId ORDER BY d.dateDemande DESC")
    List<DemandePrestation> findByAgentId(@Param("agentId") int agentId);

    List<DemandePrestation> findByPrestation(PrestationRef prestation);
    
    List<DemandePrestation> findByStatut(String statut);
    
    List<DemandePrestation> findByAgentAndPrestation(AdhAgent agent, PrestationRef prestation);
    
    @Query("SELECT d FROM DemandePrestation d WHERE d.dateDemande BETWEEN :dateDebut AND :dateFin")
    List<DemandePrestation> findByDateDemandeBetween(@Param("dateDebut") Date dateDebut, @Param("dateFin") Date dateFin);
    
    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.prestation = :prestation AND d.statut IN ('SOUMISE', 'EN_COURS', 'ACCEPTEE')")
    long countActiveDemandes(@Param("prestation") PrestationRef prestation);
    
    @Query("SELECT d FROM DemandePrestation d WHERE d.statut IN ('SOUMISE', 'EN_COURS') ORDER BY d.dateDemande ASC")
    List<DemandePrestation> findPendingDemandes();
    
    @Query("SELECT d FROM DemandePrestation d WHERE d.agent.NOM_AG LIKE %:searchTerm% OR d.agent.PR_AG LIKE %:searchTerm% OR d.prestation.label LIKE %:searchTerm%")
    List<DemandePrestation> searchDemandes(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.agent = :agent AND d.prestation = :prestation AND d.statut IN ('SOUMISE', 'EN_COURS', 'ACCEPTEE')")
    long countActiveDemandsByAgentAndPrestation(@Param("agent") AdhAgent agent, @Param("prestation") PrestationRef prestation);
    
    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.prestation = :prestation")
    long countTotalDemandes(@Param("prestation") PrestationRef prestation);
    
    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.prestation = :prestation AND d.statut = 'SOUMISE'")
    long countSoumises(@Param("prestation") PrestationRef prestation);
    
    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.prestation = :prestation AND d.statut = 'EN_COURS'")
    long countEnCours(@Param("prestation") PrestationRef prestation);
    
    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.prestation = :prestation AND d.statut = 'ACCEPTEE'")
    long countAcceptees(@Param("prestation") PrestationRef prestation);
    
    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.prestation = :prestation AND d.statut = 'REFUSEE'")
    long countRefusees(@Param("prestation") PrestationRef prestation);
    
    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.prestation = :prestation AND d.statut = 'TERMINEE'")
    long countTerminees(@Param("prestation") PrestationRef prestation);
    
    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.statut = :statut")
    long countByStatutOnly(@Param("statut") String statut);
    
    @Query("SELECT d.id, d.statut, d.dateDemande, d.dateTraitement, d.agent.adhAgentId, d.agent.NOM_AG, d.agent.PR_AG, d.prestation.id FROM DemandePrestation d WHERE d.prestation = :prestation")
    List<Object[]> findByPrestationWithoutBlobs(@Param("prestation") PrestationRef prestation);
    
    @Query("SELECT d.id, d.statut, d.dateDemande, d.dateTraitement, d.agent.adhAgentId, d.agent.NOM_AG, d.agent.PR_AG, d.prestation.id, d.reponseJson FROM DemandePrestation d WHERE d.prestation = :prestation")
    List<Object[]> findByPrestationWithJsonData(@Param("prestation") PrestationRef prestation);

    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.agent = :agent")
    long countByAgent(@Param("agent") AdhAgent agent);

    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.agent.adhAgentId = :agentId")
    long countByAgentId(@Param("agentId") int agentId);

    @Query("SELECT COUNT(d) FROM DemandePrestation d WHERE d.agent = :agent AND d.statut = :statut")
    long countByAgentAndStatut(@Param("agent") AdhAgent agent, @Param("statut") String statut);

    // Native query to directly query by agent_id column
    @Query(value = "SELECT * FROM demande_prestation WHERE agent_id = :agentId ORDER BY date_demande DESC", nativeQuery = true)
    List<DemandePrestation> findByAgentIdNative(@Param("agentId") int agentId);

    @Query(value = "SELECT COUNT(*) FROM demande_prestation WHERE agent_id = :agentId", nativeQuery = true)
    long countByAgentIdNative(@Param("agentId") int agentId);
}