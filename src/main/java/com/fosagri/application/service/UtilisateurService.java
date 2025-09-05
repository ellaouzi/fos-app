package com.fosagri.application.service;

import com.fosagri.application.model.Utilisateur;
import com.fosagri.application.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    public List<Utilisateur> findAll() {
        return utilisateurRepository.findAll();
    }

    public Optional<Utilisateur> findById(Long id) {
        return utilisateurRepository.findById(id);
    }

    public Optional<Utilisateur> findByUsername(String username) {
        return utilisateurRepository.findByUsername(username);
    }

    public Utilisateur save(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }

    public void delete(Utilisateur utilisateur) {
        utilisateurRepository.delete(utilisateur);
    }

    public boolean existsByUsername(String username) {
        return utilisateurRepository.existsByUsername(username);
    }

    public boolean existsByCin(String cin) {
        return utilisateurRepository.existsByCin(cin);
    }

    public long count() {
        return utilisateurRepository.count();
    }
}