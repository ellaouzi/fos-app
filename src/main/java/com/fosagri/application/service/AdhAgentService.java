package com.fosagri.application.service;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.repository.AdhAgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdhAgentService {

    @Autowired
    private AdhAgentRepository adhAgentRepository;

    public List<AdhAgent> findAll() {
        return adhAgentRepository.findAll();
    }

    public Optional<AdhAgent> findById(Integer id) {
        return adhAgentRepository.findById(id);
    }

    public Optional<AdhAgent> findByIdAdh(String idAdh) {
        return adhAgentRepository.findByIdAdh(idAdh);
    }

    public List<AdhAgent> searchAgents(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return adhAgentRepository.findBySearchTerm(searchTerm.trim());
    }

    public AdhAgent save(AdhAgent agent) {
        return adhAgentRepository.save(agent);
    }

    public void delete(AdhAgent agent) {
        adhAgentRepository.delete(agent);
    }

    public void deleteById(Integer id) {
        adhAgentRepository.deleteById(id);
    }

    public boolean existsByIdAdh(String idAdh) {
        return adhAgentRepository.findByIdAdh(idAdh).isPresent();
    }

    public long count() {
        return adhAgentRepository.count();
    }
}