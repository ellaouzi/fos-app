package com.fosagri.application.service;

import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.repository.AdhAgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Optional<AdhAgent> findByCin(String cin) {
        return adhAgentRepository.findByCin(cin);
    }

    public List<AdhAgent> searchAgents(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }
        return adhAgentRepository.findBySearchTerm(searchTerm.trim());
    }

    /**
     * Fuzzy search - searches across multiple fields and handles multiple words
     * Handles: "al koul" -> "alkoul", "ahmed kalo" -> "kalo ahmed"
     * Searches: nom, prenom, full name (both orders), ID, CIN, code agent, ville, phone
     */
    public List<AdhAgent> fuzzySearch(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return findAll();
        }

        String term = searchTerm.trim().toLowerCase();
        Set<AdhAgent> resultSet = new HashSet<>();

        // 1. Search with original term
        resultSet.addAll(adhAgentRepository.fuzzySearch(term));

        // 2. Search with spaces removed (al koul -> alkoul)
        String noSpaces = term.replaceAll("\\s+", "");
        if (!noSpaces.equals(term)) {
            resultSet.addAll(adhAgentRepository.fuzzySearch(noSpaces));
        }

        // 3. If multiple words, try different combinations
        if (term.contains(" ")) {
            String[] words = term.split("\\s+");

            // Reversed order (ahmed kalo -> kalo ahmed)
            if (words.length == 2) {
                String reversed = words[1] + " " + words[0];
                resultSet.addAll(adhAgentRepository.fuzzySearch(reversed));

                // Also try reversed without space
                resultSet.addAll(adhAgentRepository.fuzzySearch(words[1] + words[0]));
            }

            // Search each word individually and find intersection
            Set<AdhAgent> intersectionSet = null;
            for (String word : words) {
                if (word.length() < 2) continue;

                List<AdhAgent> wordResults = adhAgentRepository.fuzzySearch(word);

                if (intersectionSet == null) {
                    intersectionSet = new HashSet<>(wordResults);
                } else {
                    intersectionSet.retainAll(new HashSet<>(wordResults));
                }
            }

            if (intersectionSet != null) {
                resultSet.addAll(intersectionSet);
            }
        }

        return resultSet.stream().collect(Collectors.toList());
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