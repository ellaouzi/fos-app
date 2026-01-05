package com.fosagri.application.repositories;

import com.fosagri.application.entities.PrestationField;
import com.fosagri.application.entities.PrestationRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestationFieldRepository extends JpaRepository<PrestationField, Long> {

    List<PrestationField> findByPrestationRefOrderByOrdreAsc(PrestationRef prestationRef);

    List<PrestationField> findByPrestationRefIdOrderByOrdreAsc(Long prestationRefId);

    @Query("SELECT pf FROM PrestationField pf WHERE pf.prestationRef.id = :prestationId AND pf.active = true ORDER BY pf.ordre ASC")
    List<PrestationField> findActiveFieldsByPrestationId(@Param("prestationId") Long prestationId);

    void deleteByPrestationRef(PrestationRef prestationRef);

    @Query("SELECT COALESCE(MAX(pf.id), 0) FROM PrestationField pf")
    Long findMaxId();
}
