package com.fosagri.application.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fosagri.application.dto.DocumentUpload;
import com.fosagri.application.dto.FieldChange;
import com.fosagri.application.entities.ModificationDemande;
import com.fosagri.application.entities.ModificationDemande.StatutModification;
import com.fosagri.application.entities.ModificationDemande.TypeEntite;
import com.fosagri.application.model.AdhAgent;
import com.fosagri.application.model.AdhConjoint;
import com.fosagri.application.model.AdhEnfant;
import com.fosagri.application.repositories.ModificationDemandeRepository;
import com.fosagri.application.service.AdhAgentService;
import com.fosagri.application.service.AdhConjointService;
import com.fosagri.application.service.AdhEnfantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ModificationDemandeService {

    @Autowired
    private ModificationDemandeRepository repository;

    @Autowired
    private AdhAgentService agentService;

    @Autowired
    private AdhConjointService conjointService;

    @Autowired
    private AdhEnfantService enfantService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    // Field labels mapping (French)
    private static final Map<String, String> FIELD_LABELS = new HashMap<>();
    static {
        // Agent fields
        FIELD_LABELS.put("num_Tel", "Telephone");
        FIELD_LABELS.put("mail", "Email");
        FIELD_LABELS.put("adresse", "Adresse");
        FIELD_LABELS.put("ville", "Ville");
        FIELD_LABELS.put("code_POSTE", "Code postal");
        FIELD_LABELS.put("situation_familiale", "Situation familiale");
        FIELD_LABELS.put("agent_photo", "Photo de profil");
        FIELD_LABELS.put("cin_image", "Image CIN");
        FIELD_LABELS.put("rib", "RIB");

        // Conjoint fields
        FIELD_LABELS.put("NOM_CONJ", "Nom");
        FIELD_LABELS.put("PR_CONJ", "Prenom");
        FIELD_LABELS.put("CIN_CONJ", "CIN");
        FIELD_LABELS.put("dat_N_CONJ", "Date de naissance");
        FIELD_LABELS.put("sex_CONJ", "Sexe");
        FIELD_LABELS.put("tele", "Telephone");
        FIELD_LABELS.put("email", "Email");
        FIELD_LABELS.put("conjoint_photo", "Photo");
        FIELD_LABELS.put("acte_mariage_photo", "Acte de mariage");

        // Enfant fields
        FIELD_LABELS.put("nom_pac", "Nom");
        FIELD_LABELS.put("pr_pac", "Prenom");
        FIELD_LABELS.put("dat_n_pac", "Date de naissance");
        FIELD_LABELS.put("sex_pac", "Sexe");
        FIELD_LABELS.put("cin_PAC", "CIN");
        FIELD_LABELS.put("niv_INSTRUCTION", "Niveau d'instruction");
        FIELD_LABELS.put("enfant_photo", "Photo");
        FIELD_LABELS.put("attestation_scolarite_photo", "Attestation de scolarite");
    }

    // ==================== CRUD Operations ====================

    @Transactional
    public ModificationDemande save(ModificationDemande demande) {
        return repository.save(demande);
    }

    @Transactional(readOnly = true)
    public Optional<ModificationDemande> findById(Long id) {
        return repository.findByIdWithAgent(id);
    }

    @Transactional(readOnly = true)
    public String getDocumentsJsonById(Long id) {
        return repository.getDocumentsJsonById(id);
    }

    @Transactional(readOnly = true)
    public List<ModificationDemande> findAll() {
        return repository.findAllOrderByDateDesc();
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    // ==================== Query Operations ====================

    @Transactional(readOnly = true)
    public List<ModificationDemande> findByAgent(AdhAgent agent) {
        return repository.findByAgent(agent);
    }

    @Transactional(readOnly = true)
    public List<ModificationDemande> findPendingModifications() {
        return repository.findPendingModifications();
    }

    @Transactional(readOnly = true)
    public List<ModificationDemande> findByStatut(StatutModification statut) {
        return repository.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public List<ModificationDemande> findByTypeEntite(TypeEntite type) {
        return repository.findByTypeEntite(type);
    }

    @Transactional(readOnly = true)
    public List<ModificationDemande> searchModifications(String term) {
        return repository.searchModifications(term);
    }

    @Transactional(readOnly = true)
    public long countByStatut(StatutModification statut) {
        return repository.countByStatut(statut);
    }

    @Transactional(readOnly = true)
    public long countPending() {
        return repository.countPending();
    }

    @Transactional(readOnly = true)
    public boolean hasPendingModification(AdhAgent agent, TypeEntite type, Integer entiteId) {
        return repository.countPendingByAgentAndEntity(agent, type, entiteId) > 0;
    }

    @Transactional(readOnly = true)
    public long countPendingByAgent(AdhAgent agent) {
        return repository.countPendingByAgent(agent);
    }

    // ==================== Create Modification Requests ====================

    @Transactional
    public ModificationDemande createAgentModificationRequest(
            AdhAgent agent,
            Map<String, Object> newValues,
            List<DocumentUpload> newDocuments) {

        // Check if there's already a pending modification
        if (hasPendingModification(agent, TypeEntite.AGENT, agent.getAdhAgentId())) {
            throw new RuntimeException("Une demande de modification est deja en attente pour cet agent");
        }

        ModificationDemande demande = new ModificationDemande();
        demande.setAgent(agent);
        demande.setTypeEntite(TypeEntite.AGENT);
        demande.setEntiteId(agent.getAdhAgentId());
        demande.setEntiteLabel("Agent: " + agent.getNOM_AG() + " " + agent.getPR_AG());

        // Extract current values
        Map<String, Object> oldValues = extractAgentValues(agent);

        try {
            demande.setAnciennesValeursJson(objectMapper.writeValueAsString(oldValues));
            demande.setNouvellesValeursJson(objectMapper.writeValueAsString(newValues));

            if (newDocuments != null && !newDocuments.isEmpty()) {
                demande.setDocumentsJson(objectMapper.writeValueAsString(newDocuments));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la serialisation des donnees", e);
        }

        return save(demande);
    }

    @Transactional
    public ModificationDemande createConjointModificationRequest(
            AdhAgent agent,
            AdhConjoint conjoint,
            Map<String, Object> newValues,
            List<DocumentUpload> newDocuments) {

        // Check if there's already a pending modification
        if (hasPendingModification(agent, TypeEntite.CONJOINT, conjoint.getAdhConjointId())) {
            throw new RuntimeException("Une demande de modification est deja en attente pour ce conjoint");
        }

        ModificationDemande demande = new ModificationDemande();
        demande.setAgent(agent);
        demande.setTypeEntite(TypeEntite.CONJOINT);
        demande.setEntiteId(conjoint.getAdhConjointId());
        demande.setEntiteLabel("Conjoint: " + conjoint.getNOM_CONJ() + " " + conjoint.getPR_CONJ());

        // Extract current values
        Map<String, Object> oldValues = extractConjointValues(conjoint);

        try {
            demande.setAnciennesValeursJson(objectMapper.writeValueAsString(oldValues));
            demande.setNouvellesValeursJson(objectMapper.writeValueAsString(newValues));

            if (newDocuments != null && !newDocuments.isEmpty()) {
                demande.setDocumentsJson(objectMapper.writeValueAsString(newDocuments));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la serialisation des donnees", e);
        }

        return save(demande);
    }

    @Transactional
    public ModificationDemande createEnfantModificationRequest(
            AdhAgent agent,
            AdhEnfant enfant,
            Map<String, Object> newValues,
            List<DocumentUpload> newDocuments) {

        // Check if there's already a pending modification
        if (hasPendingModification(agent, TypeEntite.ENFANT, enfant.getAdhEnfantId())) {
            throw new RuntimeException("Une demande de modification est deja en attente pour cet enfant");
        }

        ModificationDemande demande = new ModificationDemande();
        demande.setAgent(agent);
        demande.setTypeEntite(TypeEntite.ENFANT);
        demande.setEntiteId(enfant.getAdhEnfantId());
        demande.setEntiteLabel("Enfant: " + enfant.getNom_pac() + " " + enfant.getPr_pac());

        // Extract current values
        Map<String, Object> oldValues = extractEnfantValues(enfant);

        try {
            demande.setAnciennesValeursJson(objectMapper.writeValueAsString(oldValues));
            demande.setNouvellesValeursJson(objectMapper.writeValueAsString(newValues));

            if (newDocuments != null && !newDocuments.isEmpty()) {
                demande.setDocumentsJson(objectMapper.writeValueAsString(newDocuments));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la serialisation des donnees", e);
        }

        return save(demande);
    }

    // ==================== Create New Entity Requests ====================

    @Transactional
    public ModificationDemande createConjointCreationRequest(
            AdhAgent agent,
            Map<String, Object> newValues,
            List<DocumentUpload> newDocuments) {

        ModificationDemande demande = new ModificationDemande();
        demande.setAgent(agent);
        demande.setTypeEntite(TypeEntite.CONJOINT);
        demande.setTypeAction(ModificationDemande.TypeAction.CREATION);
        demande.setEntiteId(null); // No entity ID yet

        String nom = newValues.get("NOM_CONJ") != null ? newValues.get("NOM_CONJ").toString() : "";
        String prenom = newValues.get("PR_CONJ") != null ? newValues.get("PR_CONJ").toString() : "";
        demande.setEntiteLabel("Nouveau Conjoint: " + nom + " " + prenom);

        try {
            demande.setAnciennesValeursJson(null); // No old values for creation
            demande.setNouvellesValeursJson(objectMapper.writeValueAsString(newValues));

            if (newDocuments != null && !newDocuments.isEmpty()) {
                demande.setDocumentsJson(objectMapper.writeValueAsString(newDocuments));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la serialisation des donnees", e);
        }

        return save(demande);
    }

    @Transactional
    public ModificationDemande createEnfantCreationRequest(
            AdhAgent agent,
            Map<String, Object> newValues,
            List<DocumentUpload> newDocuments) {

        ModificationDemande demande = new ModificationDemande();
        demande.setAgent(agent);
        demande.setTypeEntite(TypeEntite.ENFANT);
        demande.setTypeAction(ModificationDemande.TypeAction.CREATION);
        demande.setEntiteId(null); // No entity ID yet

        String nom = newValues.get("nom_pac") != null ? newValues.get("nom_pac").toString() : "";
        String prenom = newValues.get("pr_pac") != null ? newValues.get("pr_pac").toString() : "";
        demande.setEntiteLabel("Nouvel Enfant: " + nom + " " + prenom);

        try {
            demande.setAnciennesValeursJson(null); // No old values for creation
            demande.setNouvellesValeursJson(objectMapper.writeValueAsString(newValues));

            if (newDocuments != null && !newDocuments.isEmpty()) {
                demande.setDocumentsJson(objectMapper.writeValueAsString(newDocuments));
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la serialisation des donnees", e);
        }

        return save(demande);
    }

    // ==================== Approve / Reject ====================

    @Transactional
    public void approveModification(Long demandeId, Long adminUserId, String comment) {
        ModificationDemande demande = repository.findById(demandeId)
            .orElseThrow(() -> new RuntimeException("Modification non trouvee"));

        if (demande.getStatut() != StatutModification.EN_ATTENTE) {
            throw new RuntimeException("Cette modification a deja ete traitee");
        }

        // Check if this is a creation or modification
        if (demande.isCreation()) {
            // Apply creation based on entity type
            switch (demande.getTypeEntite()) {
                case CONJOINT -> createNewConjoint(demande);
                case ENFANT -> createNewEnfant(demande);
                default -> throw new RuntimeException("Creation non supportee pour ce type d'entite");
            }
        } else {
            // Apply changes based on entity type
            switch (demande.getTypeEntite()) {
                case AGENT -> applyAgentChanges(demande);
                case CONJOINT -> applyConjointChanges(demande);
                case ENFANT -> applyEnfantChanges(demande);
            }
        }

        demande.setStatut(StatutModification.VALIDE);
        demande.setDateTraitement(new Date());
        demande.setTraitePar(adminUserId);
        demande.setCommentaireAdmin(comment);
        save(demande);
    }

    @Transactional
    public void rejectModification(Long demandeId, Long adminUserId, String reason) {
        ModificationDemande demande = repository.findById(demandeId)
            .orElseThrow(() -> new RuntimeException("Modification non trouvee"));

        if (demande.getStatut() != StatutModification.EN_ATTENTE) {
            throw new RuntimeException("Cette modification a deja ete traitee");
        }

        demande.setStatut(StatutModification.REFUSE);
        demande.setDateTraitement(new Date());
        demande.setTraitePar(adminUserId);
        demande.setCommentaireAdmin(reason);
        save(demande);
    }

    // ==================== Apply Changes ====================

    private void applyAgentChanges(ModificationDemande demande) {
        AdhAgent agent = agentService.findById(demande.getEntiteId())
            .orElseThrow(() -> new RuntimeException("Agent non trouve"));

        try {
            Map<String, Object> newValues = objectMapper.readValue(
                demande.getNouvellesValeursJson(),
                new TypeReference<Map<String, Object>>() {}
            );

            // Apply field changes
            if (newValues.containsKey("num_Tel")) agent.setNum_Tel((String) newValues.get("num_Tel"));
            if (newValues.containsKey("mail")) agent.setMail((String) newValues.get("mail"));
            if (newValues.containsKey("adresse")) agent.setAdresse((String) newValues.get("adresse"));
            if (newValues.containsKey("ville")) agent.setVille((String) newValues.get("ville"));
            if (newValues.containsKey("code_POSTE")) agent.setCode_POSTE((String) newValues.get("code_POSTE"));
            if (newValues.containsKey("situation_familiale")) agent.setSituation_familiale((String) newValues.get("situation_familiale"));

            // Apply document changes
            applyDocumentChanges(demande, agent);

            agent.setUpdated(new Date());
            agentService.save(agent);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'application des modifications: " + e.getMessage(), e);
        }
    }

    private void applyConjointChanges(ModificationDemande demande) {
        AdhConjoint conjoint = conjointService.findById(demande.getEntiteId())
            .orElseThrow(() -> new RuntimeException("Conjoint non trouve"));

        try {
            Map<String, Object> newValues = objectMapper.readValue(
                demande.getNouvellesValeursJson(),
                new TypeReference<Map<String, Object>>() {}
            );

            // Apply field changes
            if (newValues.containsKey("NOM_CONJ")) conjoint.setNOM_CONJ((String) newValues.get("NOM_CONJ"));
            if (newValues.containsKey("PR_CONJ")) conjoint.setPR_CONJ((String) newValues.get("PR_CONJ"));
            if (newValues.containsKey("CIN_CONJ")) conjoint.setCIN_CONJ((String) newValues.get("CIN_CONJ"));
            if (newValues.containsKey("sex_CONJ")) conjoint.setSex_CONJ((String) newValues.get("sex_CONJ"));
            if (newValues.containsKey("tele")) conjoint.setTele((String) newValues.get("tele"));
            if (newValues.containsKey("email")) conjoint.setEmail((String) newValues.get("email"));
            if (newValues.containsKey("ville")) conjoint.setVille((String) newValues.get("ville"));

            // Apply document changes for conjoint
            applyConjointDocumentChanges(demande, conjoint);

            conjoint.setUpdated(new Date());
            conjointService.save(conjoint);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'application des modifications: " + e.getMessage(), e);
        }
    }

    private void applyEnfantChanges(ModificationDemande demande) {
        AdhEnfant enfant = enfantService.findById(demande.getEntiteId())
            .orElseThrow(() -> new RuntimeException("Enfant non trouve"));

        try {
            Map<String, Object> newValues = objectMapper.readValue(
                demande.getNouvellesValeursJson(),
                new TypeReference<Map<String, Object>>() {}
            );

            // Apply field changes
            if (newValues.containsKey("nom_pac")) enfant.setNom_pac((String) newValues.get("nom_pac"));
            if (newValues.containsKey("pr_pac")) enfant.setPr_pac((String) newValues.get("pr_pac"));
            if (newValues.containsKey("sex_pac")) enfant.setSex_pac((String) newValues.get("sex_pac"));
            if (newValues.containsKey("cin_PAC")) enfant.setCin_PAC((String) newValues.get("cin_PAC"));
            if (newValues.containsKey("tele")) enfant.setTele((String) newValues.get("tele"));
            if (newValues.containsKey("email")) enfant.setEmail((String) newValues.get("email"));
            if (newValues.containsKey("niv_INSTRUCTION")) enfant.setNiv_INSTRUCTION((String) newValues.get("niv_INSTRUCTION"));

            // Apply document changes for enfant
            applyEnfantDocumentChanges(demande, enfant);

            enfant.setUpdated(new Date());
            enfantService.save(enfant);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'application des modifications: " + e.getMessage(), e);
        }
    }

    // ==================== Create New Entities ====================

    private void createNewConjoint(ModificationDemande demande) {
        try {
            Map<String, Object> values = objectMapper.readValue(
                demande.getNouvellesValeursJson(),
                new TypeReference<Map<String, Object>>() {}
            );

            AdhConjoint conjoint = new AdhConjoint();
            conjoint.setAdhAgent(demande.getAgent());

            // Set fields
            if (values.containsKey("NOM_CONJ")) conjoint.setNOM_CONJ((String) values.get("NOM_CONJ"));
            if (values.containsKey("PR_CONJ")) conjoint.setPR_CONJ((String) values.get("PR_CONJ"));
            if (values.containsKey("CIN_CONJ")) conjoint.setCIN_CONJ((String) values.get("CIN_CONJ"));
            if (values.containsKey("sex_CONJ")) conjoint.setSex_CONJ((String) values.get("sex_CONJ"));
            if (values.containsKey("tele")) conjoint.setTele((String) values.get("tele"));
            if (values.containsKey("email")) conjoint.setEmail((String) values.get("email"));
            if (values.containsKey("ville")) conjoint.setVille((String) values.get("ville"));

            // Parse date if present
            if (values.containsKey("dat_N_CONJ") && values.get("dat_N_CONJ") != null) {
                String dateStr = values.get("dat_N_CONJ").toString();
                try {
                    conjoint.setDat_N_CONJ(dateFormat.parse(dateStr));
                } catch (Exception ignored) {}
            }

            conjoint.setUpdated(new Date());

            // Apply document changes
            applyConjointDocumentChanges(demande, conjoint);

            conjointService.save(conjoint);

            // Update the demande with the new entity ID
            demande.setEntiteId(conjoint.getAdhConjointId());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la creation du conjoint: " + e.getMessage(), e);
        }
    }

    private void createNewEnfant(ModificationDemande demande) {
        try {
            Map<String, Object> values = objectMapper.readValue(
                demande.getNouvellesValeursJson(),
                new TypeReference<Map<String, Object>>() {}
            );

            AdhEnfant enfant = new AdhEnfant();
            enfant.setAdhAgent(demande.getAgent());

            // Set fields
            if (values.containsKey("nom_pac")) enfant.setNom_pac((String) values.get("nom_pac"));
            if (values.containsKey("pr_pac")) enfant.setPr_pac((String) values.get("pr_pac"));
            if (values.containsKey("sex_pac")) enfant.setSex_pac((String) values.get("sex_pac"));
            if (values.containsKey("cin_PAC")) enfant.setCin_PAC((String) values.get("cin_PAC"));
            if (values.containsKey("tele")) enfant.setTele((String) values.get("tele"));
            if (values.containsKey("email")) enfant.setEmail((String) values.get("email"));
            if (values.containsKey("niv_INSTRUCTION")) enfant.setNiv_INSTRUCTION((String) values.get("niv_INSTRUCTION"));

            // Parse date if present
            if (values.containsKey("dat_n_pac") && values.get("dat_n_pac") != null) {
                String dateStr = values.get("dat_n_pac").toString();
                try {
                    enfant.setDat_n_pac(dateFormat.parse(dateStr));
                } catch (Exception ignored) {}
            }

            enfant.setCreated(new Date());
            enfant.setUpdated(new Date());

            // Apply document changes
            applyEnfantDocumentChanges(demande, enfant);

            enfantService.save(enfant);

            // Update the demande with the new entity ID
            demande.setEntiteId(enfant.getAdhEnfantId());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la creation de l'enfant: " + e.getMessage(), e);
        }
    }

    private void applyDocumentChanges(ModificationDemande demande, AdhAgent agent) {
        if (demande.getDocumentsJson() == null || demande.getDocumentsJson().isEmpty()) return;

        try {
            List<DocumentUpload> documents = objectMapper.readValue(
                demande.getDocumentsJson(),
                new TypeReference<List<DocumentUpload>>() {}
            );

            for (DocumentUpload doc : documents) {
                switch (doc.getFieldName()) {
                    case "agent_photo" -> {
                        agent.setAgent_photo(doc.getData());
                        agent.setAgent_photo_filename(doc.getFilename());
                        agent.setAgent_photo_contentType(doc.getContentType());
                    }
                    case "cin_image" -> {
                        agent.setCin_image(doc.getData());
                        agent.setCin_image_filename(doc.getFilename());
                        agent.setCin_image_contentType(doc.getContentType());
                    }
                    case "rib" -> {
                        agent.setRib(doc.getData());
                        agent.setRib_filename(doc.getFilename());
                        agent.setRib_contentType(doc.getContentType());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error applying document changes: " + e.getMessage());
        }
    }

    private void applyConjointDocumentChanges(ModificationDemande demande, AdhConjoint conjoint) {
        if (demande.getDocumentsJson() == null || demande.getDocumentsJson().isEmpty()) return;

        try {
            List<DocumentUpload> documents = objectMapper.readValue(
                demande.getDocumentsJson(),
                new TypeReference<List<DocumentUpload>>() {}
            );

            for (DocumentUpload doc : documents) {
                switch (doc.getFieldName()) {
                    case "conjoint_photo" -> {
                        conjoint.setConjoint_photo(doc.getData());
                        conjoint.setConjoint_photo_filename(doc.getFilename());
                        conjoint.setConjoint_photo_contentType(doc.getContentType());
                    }
                    case "cin_image" -> {
                        conjoint.setCin_image(doc.getData());
                        conjoint.setCin_image_filename(doc.getFilename());
                        conjoint.setCin_image_contentType(doc.getContentType());
                    }
                    case "acte_mariage_photo" -> {
                        conjoint.setActe_mariage_photo(doc.getData());
                        conjoint.setActe_mariage_photo_filename(doc.getFilename());
                        conjoint.setActe_mariage_photo_contentType(doc.getContentType());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error applying conjoint document changes: " + e.getMessage());
        }
    }

    private void applyEnfantDocumentChanges(ModificationDemande demande, AdhEnfant enfant) {
        if (demande.getDocumentsJson() == null || demande.getDocumentsJson().isEmpty()) return;

        try {
            List<DocumentUpload> documents = objectMapper.readValue(
                demande.getDocumentsJson(),
                new TypeReference<List<DocumentUpload>>() {}
            );

            for (DocumentUpload doc : documents) {
                switch (doc.getFieldName()) {
                    case "enfant_photo" -> {
                        enfant.setEnfant_photo(doc.getData());
                        enfant.setEnfant_photo_filename(doc.getFilename());
                        enfant.setEnfant_photo_contentType(doc.getContentType());
                    }
                    case "cin_image" -> {
                        enfant.setCin_image(doc.getData());
                        enfant.setCin_image_filename(doc.getFilename());
                        enfant.setCin_image_contentType(doc.getContentType());
                    }
                    case "attestation_scolarite_photo" -> {
                        enfant.setAttestation_scolarite_photo(doc.getData());
                        enfant.setAttestation_scolarite_photo_filename(doc.getFilename());
                        enfant.setAttestation_scolarite_photo_contentType(doc.getContentType());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error applying enfant document changes: " + e.getMessage());
        }
    }

    // ==================== Value Extraction ====================

    private Map<String, Object> extractAgentValues(AdhAgent agent) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("num_Tel", agent.getNum_Tel());
        values.put("mail", agent.getMail());
        values.put("adresse", agent.getAdresse());
        values.put("ville", agent.getVille());
        values.put("code_POSTE", agent.getCode_POSTE());
        values.put("situation_familiale", agent.getSituation_familiale());
        return values;
    }

    private Map<String, Object> extractConjointValues(AdhConjoint conjoint) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("NOM_CONJ", conjoint.getNOM_CONJ());
        values.put("PR_CONJ", conjoint.getPR_CONJ());
        values.put("CIN_CONJ", conjoint.getCIN_CONJ());
        values.put("dat_N_CONJ", conjoint.getDat_N_CONJ() != null ? dateFormat.format(conjoint.getDat_N_CONJ()) : null);
        values.put("sex_CONJ", conjoint.getSex_CONJ());
        values.put("tele", conjoint.getTele());
        values.put("email", conjoint.getEmail());
        values.put("ville", conjoint.getVille());
        return values;
    }

    private Map<String, Object> extractEnfantValues(AdhEnfant enfant) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("nom_pac", enfant.getNom_pac());
        values.put("pr_pac", enfant.getPr_pac());
        values.put("dat_n_pac", enfant.getDat_n_pac() != null ? dateFormat.format(enfant.getDat_n_pac()) : null);
        values.put("sex_pac", enfant.getSex_pac());
        values.put("cin_PAC", enfant.getCin_PAC());
        values.put("tele", enfant.getTele());
        values.put("email", enfant.getEmail());
        values.put("niv_INSTRUCTION", enfant.getNiv_INSTRUCTION());
        return values;
    }

    // ==================== Comparison ====================

    public List<FieldChange> compareValues(String oldJson, String newJson) {
        List<FieldChange> changes = new ArrayList<>();

        try {
            Map<String, Object> oldValues = oldJson != null ?
                objectMapper.readValue(oldJson, new TypeReference<Map<String, Object>>() {}) :
                new HashMap<>();
            Map<String, Object> newValues = newJson != null ?
                objectMapper.readValue(newJson, new TypeReference<Map<String, Object>>() {}) :
                new HashMap<>();

            // Get all keys from both maps
            Set<String> allKeys = new HashSet<>();
            allKeys.addAll(oldValues.keySet());
            allKeys.addAll(newValues.keySet());

            for (String key : allKeys) {
                Object oldValue = oldValues.get(key);
                Object newValue = newValues.get(key);

                // Check if values are different
                boolean changed = (oldValue == null && newValue != null) ||
                                  (oldValue != null && !oldValue.equals(newValue));

                if (changed) {
                    String label = FIELD_LABELS.getOrDefault(key, key);
                    boolean isDocument = key.contains("photo") || key.contains("image") || key.equals("rib");
                    changes.add(new FieldChange(key, label, oldValue, newValue, isDocument));
                }
            }

        } catch (Exception e) {
            System.err.println("Error comparing values: " + e.getMessage());
        }

        return changes;
    }

    public String getFieldLabel(String fieldName) {
        return FIELD_LABELS.getOrDefault(fieldName, fieldName);
    }
}
