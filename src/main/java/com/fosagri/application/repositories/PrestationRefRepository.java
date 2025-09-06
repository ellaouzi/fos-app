package com.fosagri.application.repositories;

import com.fosagri.application.entities.PrestationRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface PrestationRefRepository extends JpaRepository<PrestationRef, Long> {
    
    List<PrestationRef> findByOpenTrue();
    
    List<PrestationRef> findByStatut(String statut);
    
    @Query("SELECT p FROM PrestationRef p WHERE p.open = true AND (p.dateDu IS NULL OR p.dateDu <= :currentDate) AND (p.dateAu IS NULL OR p.dateAu >= :currentDate)")
    List<PrestationRef> findActivePrestation(Date currentDate);
    
    List<PrestationRef> findByLabelContainingIgnoreCase(String label);
}