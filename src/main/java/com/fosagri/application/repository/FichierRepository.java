package com.fosagri.application.repository;

import com.fosagri.application.model.Fichier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FichierRepository extends JpaRepository<Fichier, Long> {
    
    @Query("SELECT f FROM Fichier f WHERE f.cod_ag = :codAg")
    List<Fichier> findByCodAg(@Param("codAg") String codAg);
    
    List<Fichier> findByIdadh(String idadh);
    
    List<Fichier> findByExtention(String extention);
}