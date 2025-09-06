package com.fosagri.application.services;

import com.fosagri.application.entities.DemandePrestation;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.repositories.DemandePrestationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DemandePrestationService {
    
    @Autowired
    private DemandePrestationRepository repository;
    
    @Autowired
    private PrestationRefService prestationRefService;
    
    @Autowired
    private com.fosagri.application.service.AdhAgentService adhAgentService;
    
    public List<DemandePrestation> findAll() {
        return repository.findAll();
    }
    
    public Optional<DemandePrestation> findById(Long id) {
        return repository.findById(id);
    }
    
    public DemandePrestation save(DemandePrestation demande) {
        return repository.save(demande);
    }
    
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
    
    public List<DemandePrestation> findByAgent(AdhAgent agent) {
        return repository.findByAgent(agent);
    }
    
    public List<DemandePrestation> findByPrestation(PrestationRef prestation) {
        return repository.findByPrestation(prestation);
    }
    
    public List<DemandePrestation> findByStatut(String statut) {
        return repository.findByStatut(statut);
    }
    
    public List<DemandePrestation> findPendingDemandes() {
        return repository.findPendingDemandes();
    }
    
    public List<DemandePrestation> searchDemandes(String searchTerm) {
        return repository.searchDemandes(searchTerm);
    }
    
    public boolean canAgentApplyToPrestation(AdhAgent agent, PrestationRef prestation) {
        // Vérifier si la prestation est ouverte
        if (!prestation.isOpen()) {
            System.out.println("❌ Prestation fermée: " + prestation.getLabel() + " - isOpen: " + prestation.isOpen());
            return false;
        }
        
        // Vérifier les dates
        Date now = new Date();
        if (prestation.getDateDu() != null && now.before(prestation.getDateDu())) {
            System.out.println("❌ Prestation pas encore ouverte: " + prestation.getLabel() + " - Date début: " + prestation.getDateDu() + ", Maintenant: " + now);
            return false;
        }
        if (prestation.getDateAu() != null && now.after(prestation.getDateAu())) {
            System.out.println("❌ Prestation fermée (date expirée): " + prestation.getLabel() + " - Date fin: " + prestation.getDateAu() + ", Maintenant: " + now);
            return false;
        }
        
        // Vérifier la limite de demandes
        if (prestation.getNombreLimit() > 0) {
            long activeDemandes = repository.countActiveDemandes(prestation);
            if (activeDemandes >= prestation.getNombreLimit()) {
                System.out.println("❌ Limite atteinte pour prestation: " + prestation.getLabel() + " - Limite: " + prestation.getNombreLimit() + ", Demandes actives: " + activeDemandes);
                return false;
            }
        }
        
        // Vérifier si l'agent a déjà une demande active pour cette prestation
        long activeDemandes = repository.countActiveDemandsByAgentAndPrestation(agent, prestation);
        
        if (activeDemandes > 0) {
            System.out.println("❌ Agent a déjà une demande active: " + agent.getNOM_AG() + " pour prestation: " + prestation.getLabel());
            return false;
        }
        
        System.out.println("✅ Agent éligible: " + agent.getNOM_AG() + " pour prestation: " + prestation.getLabel());
        return true;
    }
    
    public DemandePrestation submitDemandePrestation(AdhAgent agent, PrestationRef prestation, String reponseJson) {
        if (!canAgentApplyToPrestation(agent, prestation)) {
            throw new RuntimeException("L'agent ne peut pas soumettre une demande pour cette prestation");
        }
        
        DemandePrestation demande = new DemandePrestation();
        demande.setAgent(agent);
        demande.setPrestation(prestation);
        demande.setReponseJson(reponseJson);
        demande.setStatut("SOUMISE");
        demande.setDateDemande(new Date());
        
        return save(demande);
    }
    
    public void updateStatut(Long demandeId, String nouveauStatut, String commentaire, Long traitePar) {
        Optional<DemandePrestation> demandeOpt = findById(demandeId);
        if (demandeOpt.isPresent()) {
            DemandePrestation demande = demandeOpt.get();
            demande.setStatut(nouveauStatut);
            demande.setCommentaire(commentaire);
            demande.setTraitePar(traitePar);
            
            if ("EN_COURS".equals(nouveauStatut) && demande.getDateTraitement() == null) {
                demande.setDateTraitement(new Date());
            }
            
            if ("ACCEPTEE".equals(nouveauStatut) || "REFUSEE".equals(nouveauStatut) || "TERMINEE".equals(nouveauStatut)) {
                demande.setDateFinalisation(new Date());
            }
            
            save(demande);
        }
    }
    
    public long count() {
        return repository.count();
    }
    
    public long countByStatut(String statut) {
        return findByStatut(statut).size();
    }
}