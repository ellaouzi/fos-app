package com.fosagri.application.repository;

import com.fosagri.application.model.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByUsername(String username);

    @Query("SELECT u FROM Utilisateur u LEFT JOIN FETCH u.authorities WHERE u.username = :username")
    Optional<Utilisateur> findByUsernameWithAuthorities(@Param("username") String username);

    Optional<Utilisateur> findByCin(String cin);

    Optional<Utilisateur> findByPpr(String ppr);

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByCin(String cin);

    boolean existsByPpr(String ppr);

    Page<Utilisateur> findByUsernameContainingIgnoreCase(String username, Pageable pageable);

    long countByUsernameContainingIgnoreCase(String username);
}