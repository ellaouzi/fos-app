package com.fosagri.application.repository;

import com.fosagri.application.model.Authority;
import com.fosagri.application.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, Long> {
    
    List<Authority> findByUtilisateur(Utilisateur utilisateur);
    
    List<Authority> findByUsername(String username);
    
    List<Authority> findByRole(String role);
}