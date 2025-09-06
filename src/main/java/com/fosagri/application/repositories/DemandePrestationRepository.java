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
}