package com.fosagri.application.repository;

import com.fosagri.application.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByUsername(String username);
    
    Optional<Utilisateur> findByCin(String cin);
    
    Optional<Utilisateur> findByPpr(String ppr);
    
    Optional<Utilisateur> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByCin(String cin);
    
    boolean existsByPpr(String ppr);
}