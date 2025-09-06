package com.fosagri.application.services;

import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.repositories.PrestationRefRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PrestationRefService {
    
    @Autowired
    private PrestationRefRepository repository;
    
    public List<PrestationRef> findAll() {
        return repository.findAll();
    }
    
    public Optional<PrestationRef> findById(Long id) {
        return repository.findById(id);
    }
    
    public PrestationRef save(PrestationRef prestationRef) {
        return repository.save(prestationRef);
    }
    
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    public List<PrestationRef> findOpenPrestations() {
        return repository.findByOpenTrue();
    }
    
    public List<PrestationRef> findByStatut(String statut) {
        return repository.findByStatut(statut);
    }
    
    public List<PrestationRef> findActivePrestations() {
        return repository.findActivePrestation(new Date());
    }
    
    public List<PrestationRef> searchByLabel(String searchTerm) {
        return repository.findByLabelContainingIgnoreCase(searchTerm);
    }
    
    public long count() {
        return repository.count();
    }
}