package com.fosagri.application.services;

import com.fosagri.application.entities.PrestationField;
import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.repositories.PrestationFieldRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class PrestationFieldService {

    @Autowired
    private PrestationFieldRepository repository;

    public List<PrestationField> findAll() {
        return repository.findAll();
    }

    public Optional<PrestationField> findById(Long id) {
        return repository.findById(id);
    }

    public List<PrestationField> findByPrestationRef(PrestationRef prestationRef) {
        return repository.findByPrestationRefOrderByOrdreAsc(prestationRef);
    }

    public List<PrestationField> findByPrestationRefId(Long prestationRefId) {
        System.out.println("🔎 Service: findByPrestationRefId(" + prestationRefId + ")");
        List<PrestationField> fields = repository.findByPrestationRefIdOrderByOrdreAsc(prestationRefId);
        System.out.println("🔎 Service: Found " + (fields != null ? fields.size() : 0) + " fields");
        if (fields != null) {
            for (PrestationField f : fields) {
                System.out.println("   - " + f.getId() + ": " + f.getLabel() + " (" + f.getFieldtype() + ")");
            }
        }
        return fields;
    }

    public List<PrestationField> findActiveFieldsByPrestationId(Long prestationId) {
        return repository.findActiveFieldsByPrestationId(prestationId);
    }

    public PrestationField save(PrestationField field) {
        if (field.getId() == null) {
            field.setId(repository.findMaxId() + 1);
        }
        if (field.getCreated() == null) {
            field.setCreated(new Date());
        }
        field.setUpdated(new Date());
        return repository.save(field);
    }

    public List<PrestationField> saveAll(List<PrestationField> fields) {
        Date now = new Date();
        Long maxId = repository.findMaxId();
        for (PrestationField field : fields) {
            if (field.getId() == null) {
                field.setId(++maxId);
            }
            if (field.getCreated() == null) {
                field.setCreated(now);
            }
            field.setUpdated(now);
        }
        return repository.saveAll(fields);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public void deleteByPrestationRef(PrestationRef prestationRef) {
        repository.deleteByPrestationRef(prestationRef);
    }

    @Transactional
    public void replaceFieldsForPrestation(PrestationRef prestationRef, List<PrestationField> newFields) {
        // Delete existing fields
        repository.deleteByPrestationRef(prestationRef);

        // Flush to ensure deletes are committed before inserts
        repository.flush();

        // Get max ID after deletion
        Long maxId = repository.findMaxId();

        // Set the reference and save new fields
        Date now = new Date();
        int order = 1;
        for (PrestationField field : newFields) {
            // Manually generate ID
            field.setId(++maxId);
            field.setPrestationRef(prestationRef);
            field.setOrdre(order++);
            field.setCreated(now);
            field.setUpdated(now);
            field.setActive(true);
        }

        repository.saveAll(newFields);
    }

    public long count() {
        return repository.count();
    }
}
