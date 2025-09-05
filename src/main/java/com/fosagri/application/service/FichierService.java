package com.fosagri.application.service;

import com.fosagri.application.model.Fichier;
import com.fosagri.application.repository.FichierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class FichierService {

    @Autowired
    private FichierRepository fichierRepository;

    public List<Fichier> findAll() {
        return fichierRepository.findAll();
    }

    public Optional<Fichier> findById(Long id) {
        return fichierRepository.findById(id);
    }

    public List<Fichier> findByCodeAgent(String codAg) {
        return fichierRepository.findByCodAg(codAg);
    }

    public List<Fichier> findByAdhId(String idadh) {
        return fichierRepository.findByIdadh(idadh);
    }

    public List<Fichier> findByExtension(String extension) {
        return fichierRepository.findByExtention(extension);
    }

    public Fichier save(Fichier fichier) {
        return fichierRepository.save(fichier);
    }

    public void delete(Fichier fichier) {
        fichierRepository.delete(fichier);
    }

    public void deleteById(Long id) {
        fichierRepository.deleteById(id);
    }

    public long count() {
        return fichierRepository.count();
    }
}