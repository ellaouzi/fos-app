package com.fosagri.application.services;

import com.fosagri.application.entities.Contact;
import com.fosagri.application.entities.Contact.TypeContact;
import com.fosagri.application.repositories.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ContactService {

    @Autowired
    private ContactRepository repository;

    public List<Contact> findAll() {
        return repository.findAll();
    }

    public List<Contact> findAllActive() {
        return repository.findByActifTrue();
    }

    public List<Contact> findAllActiveOrdered() {
        return repository.findAllActiveOrdered();
    }

    public Optional<Contact> findById(Long id) {
        return repository.findById(id);
    }

    public Contact save(Contact contact) {
        return repository.save(contact);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    public void delete(Contact contact) {
        repository.delete(contact);
    }

    public List<Contact> findByType(TypeContact type) {
        return repository.findByType(type);
    }

    public List<Contact> findByTypeOrdered(TypeContact type) {
        return repository.findByTypeOrdered(type);
    }

    public List<Contact> findByTypeActive(TypeContact type) {
        return repository.findByTypeAndActifTrue(type);
    }

    public List<Contact> search(String term) {
        if (term == null || term.trim().isEmpty()) {
            return findAllActiveOrdered();
        }
        return repository.searchContacts(term.trim());
    }

    public List<Contact> findByVille(String ville) {
        return repository.findByVilleIgnoreCaseAndActifTrue(ville);
    }

    public List<Contact> findByRegion(String region) {
        return repository.findByRegionIgnoreCaseAndActifTrue(region);
    }

    public List<String> getDistinctVilles() {
        return repository.findDistinctVilles();
    }

    public List<String> getDistinctRegions() {
        return repository.findDistinctRegions();
    }

    public long count() {
        return repository.count();
    }

    public long countByType(TypeContact type) {
        return repository.countByType(type);
    }

    public void deactivate(Long id) {
        repository.findById(id).ifPresent(contact -> {
            contact.setActif(false);
            repository.save(contact);
        });
    }

    public void activate(Long id) {
        repository.findById(id).ifPresent(contact -> {
            contact.setActif(true);
            repository.save(contact);
        });
    }
}
