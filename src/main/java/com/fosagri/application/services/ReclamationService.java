package com.fosagri.application.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.entities.Reclamation;
import com.fosagri.application.entities.Reclamation.StatutReclamation;
import com.fosagri.application.entities.Reclamation.TypeReclamation;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.repositories.ReclamationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class ReclamationService {
    
    @Autowired
    private ReclamationRepository reclamationRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // CRUD Operations
    public List<Reclamation> findAll() {
        return reclamationRepository.findAllWithoutAgentDataOrderByDateCreationDesc();
    }
    
    public Optional<Reclamation> findById(Long id) {
        return reclamationRepository.findById(id);
    }
    
    public Optional<Reclamation> findByIdWithAgent(Long id) {
        return reclamationRepository.findById(id);
    }
    
    public String getAgentNameForReclamation(Long reclamationId) {
        try {
            Object[] agentData = reclamationRepository.findAgentNameByReclamationId(reclamationId);
            if (agentData != null && agentData.length >= 2) {
                return agentData[0] + " " + agentData[1];
            }
        } catch (Exception e) {
            // Handle any errors gracefully
        }
        return "Agent (non accessible)";
    }
    
    public Reclamation save(Reclamation reclamation) {
        return reclamationRepository.save(reclamation);
    }
    
    public void deleteById(Long id) {
        reclamationRepository.deleteById(id);
    }
    
    // Business Logic Methods
    public List<Reclamation> findByAgent(AdhAgent agent) {
        return reclamationRepository.findByAgentOrderByDateCreationDesc(agent);
    }
    
    public List<Reclamation> findByStatut(StatutReclamation statut) {
        return reclamationRepository.findByStatutOrderByDateCreationDesc(statut);
    }
    
    public List<Reclamation> findByType(TypeReclamation type) {
        return reclamationRepository.findByTypeOrderByDateCreationDesc(type);
    }
    
    public List<Reclamation> searchReclamations(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return findAll();
        }
        return reclamationRepository.searchByObjetOrDetail(searchText.trim());
    }
    
    public List<Reclamation> findPendingReclamations() {
        return reclamationRepository.findPendingReclamations();
    }
    
    public List<Reclamation> findRecentReclamations() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = cal.getTime();
        return reclamationRepository.findRecentReclamations(thirtyDaysAgo);
    }
    
    // File handling methods
    public String saveFileAttachment(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        
        Map<String, Object> fileData = new HashMap<>();
        fileData.put("filename", file.getOriginalFilename());
        fileData.put("contentType", file.getContentType());
        fileData.put("size", file.getSize());
        fileData.put("content", Base64.getEncoder().encodeToString(file.getBytes()));
        fileData.put("uploadDate", new Date());
        
        return objectMapper.writeValueAsString(fileData);
    }
    
    public Map<String, Object> getFileAttachment(String fichierAttacheJson) {
        if (fichierAttacheJson == null || fichierAttacheJson.trim().isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(fichierAttacheJson, Map.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
    
    // Status management methods
    public Reclamation updateStatut(Long reclamationId, StatutReclamation newStatut) {
        Optional<Reclamation> optionalReclamation = findById(reclamationId);
        if (optionalReclamation.isPresent()) {
            Reclamation reclamation = optionalReclamation.get();
            reclamation.setStatut(newStatut);
            
            // Set appropriate dates based on status
            Date now = new Date();
            switch (newStatut) {
                case EN_COURS:
                    if (reclamation.getDateTraitement() == null) {
                        reclamation.setDateTraitement(now);
                    }
                    break;
                case RESOLUE:
                case FERMEE:
                case REJETEE:
                    if (reclamation.getDateTraitement() == null) {
                        reclamation.setDateTraitement(now);
                    }
                    reclamation.setDateCloture(now);
                    break;
            }
            
            return save(reclamation);
        }
        return null;
    }
    
    public Reclamation addOrganizationResponse(Long reclamationId, String response, Long userId) {
        Optional<Reclamation> optionalReclamation = findById(reclamationId);
        if (optionalReclamation.isPresent()) {
            Reclamation reclamation = optionalReclamation.get();
            reclamation.setReponseOrganisation(response);
            reclamation.setTraitePar(userId);
            
            // If no status update has been made, set to EN_COURS
            if (reclamation.getStatut() == StatutReclamation.NOUVELLE) {
                reclamation.setStatut(StatutReclamation.EN_COURS);
                reclamation.setDateTraitement(new Date());
            }
            
            return save(reclamation);
        }
        return null;
    }
    
    // Statistics methods
    public Map<StatutReclamation, Long> getStatusStatistics() {
        Map<StatutReclamation, Long> stats = new HashMap<>();
        for (StatutReclamation statut : StatutReclamation.values()) {
            stats.put(statut, reclamationRepository.countByStatut(statut));
        }
        return stats;
    }
    
    public long countByAgent(AdhAgent agent) {
        return reclamationRepository.countByAgent(agent);
    }
    
    // Create new reclamation with file attachment
    public Reclamation createReclamation(AdhAgent agent, String objet, String detail, 
                                        TypeReclamation type, MultipartFile file) throws IOException {
        Reclamation reclamation = new Reclamation();
        reclamation.setAgent(agent);
        reclamation.setObjet(objet);
        reclamation.setDetail(detail);
        reclamation.setType(type);
        
        if (file != null && !file.isEmpty()) {
            reclamation.setFichierAttache(saveFileAttachment(file));
        }
        
        return save(reclamation);
    }
}