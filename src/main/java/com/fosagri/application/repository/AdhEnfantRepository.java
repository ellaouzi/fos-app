package com.fosagri.application.repository;

import com.fosagri.application.model.AdhEnfant;
import com.fosagri.application.model.AdhAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdhEnfantRepository extends JpaRepository<AdhEnfant, Integer> {
    
    List<AdhEnfant> findByAdhAgent(AdhAgent adhAgent);
    
    @Query("SELECT e FROM AdhEnfant e WHERE e.adhAgent = :agent AND e.adhEnfantId IS NOT NULL AND e.nom_pac IS NOT NULL AND e.pr_pac IS NOT NULL")
    List<AdhEnfant> findBasicInfoByAdhAgent(@Param("agent") AdhAgent agent);
    
    List<AdhEnfant> findByCodAg(String codAg);
    
    List<AdhEnfant> findByValideTrue();

    long countByAdhAgent(AdhAgent adhAgent);
}