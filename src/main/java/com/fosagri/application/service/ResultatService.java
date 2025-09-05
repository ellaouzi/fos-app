package com.fosagri.application.service;

import com.fosagri.application.model.Resultat;
import com.fosagri.application.repository.ResultatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ResultatService {

    @Autowired
    private ResultatRepository resultatRepository;

    public List<Resultat> findAll() {
        return resultatRepository.findAll();
    }

    public Optional<Resultat> findById(Long id) {
        return resultatRepository.findById(id);
    }

    public List<Resultat> findByPpr(String ppr) {
        return resultatRepository.findByPpr(ppr);
    }

    public List<Resultat> findByAdhId(String idadh) {
        return resultatRepository.findByIdadh(idadh);
    }

    public List<Resultat> findByOperation(String operation) {
        return resultatRepository.findByOperation(operation);
    }

    public List<Resultat> findByStatut(String statut) {
        return resultatRepository.findByStatut(statut);
    }

    public Resultat save(Resultat resultat) {
        return resultatRepository.save(resultat);
    }

    public void delete(Resultat resultat) {
        resultatRepository.delete(resultat);
    }

    public void deleteById(Long id) {
        resultatRepository.deleteById(id);
    }

    public long count() {
        return resultatRepository.count();
    }
}