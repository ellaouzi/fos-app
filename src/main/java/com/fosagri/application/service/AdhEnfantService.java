package com.fosagri.application.service;

import com.fosagri.application.model.AdhEnfant;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.repository.AdhEnfantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdhEnfantService {

    @Autowired
    private AdhEnfantRepository adhEnfantRepository;

    public List<AdhEnfant> findAll() {
        return adhEnfantRepository.findAll();
    }

    public Optional<AdhEnfant> findById(Integer id) {
        return adhEnfantRepository.findById(id);
    }

    public List<AdhEnfant> findByAgent(AdhAgent agent) {
        return adhEnfantRepository.findByAdhAgent(agent);
    }
    
    public List<AdhEnfant> findBasicInfoByAgent(AdhAgent agent) {
        return adhEnfantRepository.findBasicInfoByAdhAgent(agent);
    }

    public List<AdhEnfant> findByCodeAgent(String codAg) {
        return adhEnfantRepository.findByCodAg(codAg);
    }

    public List<AdhEnfant> findValidEnfants() {
        return adhEnfantRepository.findByValideTrue();
    }

    public AdhEnfant save(AdhEnfant enfant) {
        return adhEnfantRepository.save(enfant);
    }

    public void delete(AdhEnfant enfant) {
        adhEnfantRepository.delete(enfant);
    }

    public void deleteById(Integer id) {
        adhEnfantRepository.deleteById(id);
    }

    public long count() {
        return adhEnfantRepository.count();
    }
}