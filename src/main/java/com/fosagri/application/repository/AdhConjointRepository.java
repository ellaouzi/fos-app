package com.fosagri.application.repository;

import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.model.AdhAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdhConjointRepository extends JpaRepository<AdhConjoint, Integer> {
    
    List<AdhConjoint> findByAdhAgent(AdhAgent adhAgent);
    
    List<AdhConjoint> findByCodAg(String codAg);
    
    List<AdhConjoint> findByValideTrue();
}