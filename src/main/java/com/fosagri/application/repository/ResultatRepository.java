package com.fosagri.application.repository;

import com.fosagri.application.model.Resultat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResultatRepository extends JpaRepository<Resultat, Long> {
    
    List<Resultat> findByPpr(String ppr);
    
    List<Resultat> findByIdadh(String idadh);
    
    List<Resultat> findByOperation(String operation);
    
    List<Resultat> findByStatut(String statut);
}