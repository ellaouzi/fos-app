package com.fosagri.application.repository;

import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.model.AdhAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdhConjointRepository extends JpaRepository<AdhConjoint, Integer> {
    
    List<AdhConjoint> findByAdhAgent(AdhAgent adhAgent);
    
    @Query("SELECT c FROM AdhConjoint c WHERE c.adhAgent = :agent AND c.adhConjointId IS NOT NULL AND c.NOM_CONJ IS NOT NULL AND c.PR_CONJ IS NOT NULL")
    List<AdhConjoint> findBasicInfoByAdhAgent(@Param("agent") AdhAgent agent);
    
    List<AdhConjoint> findByCodAg(String codAg);
    
    List<AdhConjoint> findByValideTrue();
}