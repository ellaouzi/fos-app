package com.fosagri.application.service;

import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.repository.AdhConjointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdhConjointService {

    @Autowired
    private AdhConjointRepository adhConjointRepository;

    public List<AdhConjoint> findAll() {
        return adhConjointRepository.findAll();
    }

    public Optional<AdhConjoint> findById(Integer id) {
        return adhConjointRepository.findById(id);
    }

    public List<AdhConjoint> findByAgent(AdhAgent agent) {
        return adhConjointRepository.findByAdhAgent(agent);
    }
    
    public List<AdhConjoint> findBasicInfoByAgent(AdhAgent agent) {
        return adhConjointRepository.findBasicInfoByAdhAgent(agent);
    }

    public List<AdhConjoint> findByCodeAgent(String codAg) {
        return adhConjointRepository.findByCodAg(codAg);
    }

    public List<AdhConjoint> findValidConjoints() {
        return adhConjointRepository.findByValideTrue();
    }

    public AdhConjoint save(AdhConjoint conjoint) {
        return adhConjointRepository.save(conjoint);
    }

    public void delete(AdhConjoint conjoint) {
        adhConjointRepository.delete(conjoint);
    }

    public void deleteById(Integer id) {
        adhConjointRepository.deleteById(id);
    }

    public long count() {
        return adhConjointRepository.count();
    }

    public long countByAgent(AdhAgent agent) {
        return adhConjointRepository.countByAdhAgent(agent);
    }
}