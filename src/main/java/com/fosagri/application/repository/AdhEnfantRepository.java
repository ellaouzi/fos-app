package com.fosagri.application.repository;

import com.fosagri.application.model.AdhEnfant;
import com.fosagri.application.model.AdhAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdhEnfantRepository extends JpaRepository<AdhEnfant, Integer> {
    
    List<AdhEnfant> findByAdhAgent(AdhAgent adhAgent);
    
    List<AdhEnfant> findByCodAg(String codAg);
    
    List<AdhEnfant> findByValideTrue();
}