package com.fosagri.application.service;

import com.fosagri.application.model.*;
import com.fosagri.application.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Arrays;

@Service
public class DataInitializationService implements CommandLineRunner {

    @Autowired
    private AdhAgentRepository adhAgentRepository;
    
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    
    @Autowired
    private AdhConjointRepository adhConjointRepository;
    
    @Autowired
    private AdhEnfantRepository adhEnfantRepository;
    
    @Autowired
    private FichierRepository fichierRepository;
    
    @Autowired
    private ResultatRepository resultatRepository;
    
    @Autowired
    private AuthorityRepository authorityRepository;

    @Override
    public void run(String... args) throws Exception {
        if (adhAgentRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        // Create sample agents
        AdhAgent agent1 = createAgent("ADH001", "AGT001", "ALAMI", "Mohammed", "AB123456", "M", 
                "mohammed.alami@fosagri.ma", "0661234567", "Rabat", "10000", "Avenue Mohammed V");
        
        AdhAgent agent2 = createAgent("ADH002", "AGT002", "BENALI", "Fatima", "CD789012", "F", 
                "fatima.benali@fosagri.ma", "0662345678", "Casablanca", "20000", "Boulevard Hassan II");
        
        AdhAgent agent3 = createAgent("ADH003", "AGT003", "ELKHADIR", "Ahmed", "EF345678", "M", 
                "ahmed.elkhadir@fosagri.ma", "0663456789", "Fes", "30000", "Rue des Jardins");

        // Save agents
        agent1 = adhAgentRepository.save(agent1);
        agent2 = adhAgentRepository.save(agent2);
        agent3 = adhAgentRepository.save(agent3);

        // Create sample users
        Utilisateur user1 = createUser("malami", "password123", "ALAMI", "Mohammed", "AB123456", "PPR001", "ADH001");
        Utilisateur user2 = createUser("fbenali", "password123", "BENALI", "Fatima", "CD789012", "PPR002", "ADH002");
        
        user1 = utilisateurRepository.save(user1);
        user2 = utilisateurRepository.save(user2);

        // Create authorities for users
        Authority auth1 = new Authority("malami", "ROLE_ADMIN", user1);
        Authority auth2 = new Authority("fbenali", "ROLE_USER", user2);
        
        authorityRepository.saveAll(Arrays.asList(auth1, auth2));

        // Create sample conjoint data
        AdhConjoint conjoint1 = createConjoint(agent1, "CONJOINT001", "ALAMI", "Aicha", "GH901234");
        AdhConjoint conjoint2 = createConjoint(agent2, "CONJOINT002", "BENALI", "Omar", "IJ567890");
        
        adhConjointRepository.saveAll(Arrays.asList(conjoint1, conjoint2));

        // Create sample enfant data
        AdhEnfant enfant1 = createEnfant(agent1, "ALAMI", "Youssef", "KL123456", "M");
        AdhEnfant enfant2 = createEnfant(agent1, "ALAMI", "Sara", "MN789012", "F");
        AdhEnfant enfant3 = createEnfant(agent2, "BENALI", "Hassan", "OP345678", "M");
        
        adhEnfantRepository.saveAll(Arrays.asList(enfant1, enfant2, enfant3));

        // Create sample fichier data
        Fichier fichier1 = createFichier("AGT001", "ADH001", "CV_ALAMI.pdf", "Curriculum Vitae", "pdf");
        Fichier fichier2 = createFichier("AGT002", "ADH002", "PHOTO_BENALI.jpg", "Photo d'identité", "jpg");
        
        fichierRepository.saveAll(Arrays.asList(fichier1, fichier2));

        // Create sample resultat data
        Resultat resultat1 = createResultat("INSCRIPTION", 1L, "SUCCESS", "PPR001", "ADH001");
        Resultat resultat2 = createResultat("MODIFICATION", 2L, "SUCCESS", "PPR002", "ADH002");
        
        resultatRepository.saveAll(Arrays.asList(resultat1, resultat2));

        System.out.println("Sample data initialized successfully!");
        System.out.println("Created " + adhAgentRepository.count() + " agents");
        System.out.println("Created " + utilisateurRepository.count() + " users");
        System.out.println("Created " + adhConjointRepository.count() + " conjoints");
        System.out.println("Created " + adhEnfantRepository.count() + " enfants");
    }

    private AdhAgent createAgent(String idAdh, String codAg, String nom, String prenom, String cin, String sexe, 
                                String email, String telephone, String ville, String codePoste, String adresse) {
        AdhAgent agent = new AdhAgent();
        agent.setIdAdh(idAdh);
        agent.setCodAg(codAg);
        agent.setNOM_AG(nom);
        agent.setPR_AG(prenom);
        agent.setCIN_AG(cin);
        agent.setSex_AG(sexe);
        agent.setMail(email);
        agent.setNum_Tel(telephone);
        agent.setVille(ville);
        agent.setCode_POSTE(codePoste);
        agent.setAdresse(adresse);
        agent.setIS_ADH("OUI");
        agent.setSituation_familiale("Marié(e)");
        agent.setNaissance(new Date(System.currentTimeMillis() - (30L * 365 * 24 * 60 * 60 * 1000))); // 30 years ago
        agent.setUpdated(new Date());
        return agent;
    }

    private Utilisateur createUser(String username, String password, String nom, String prenom, 
                                  String cin, String ppr, String adhid) {
        Utilisateur user = new Utilisateur();
        user.setUsername(username);
        user.setPassword(password);
        user.setNom(nom);
        user.setPrenom(prenom);
        user.setCin(cin);
        user.setPpr(ppr);
        user.setAdhid(adhid);
        user.setEmail(username + "@fosagri.ma");
        user.setEnabled(true);
        user.setCreated(new Date());
        user.setUpdated(new Date());
        user.setIsadh("OUI");
        return user;
    }

    private AdhConjoint createConjoint(AdhAgent agent, String numConj, String nom, String prenom, String cin) {
        AdhConjoint conjoint = new AdhConjoint();
        conjoint.setAdhAgent(agent);
        conjoint.setCodAg(agent.getCodAg());
        conjoint.setNUM_CONJ(numConj);
        conjoint.setNOM_CONJ(nom);
        conjoint.setPR_CONJ(prenom);
        conjoint.setCIN_CONJ(cin);
        conjoint.setSex_CONJ("F".equals(agent.getSex_AG()) ? "M" : "F");
        conjoint.setDat_N_CONJ(new Date(System.currentTimeMillis() - (28L * 365 * 24 * 60 * 60 * 1000))); // 28 years ago
        conjoint.setDat_MAR(new Date(System.currentTimeMillis() - (5L * 365 * 24 * 60 * 60 * 1000))); // 5 years ago
        conjoint.setSit_CJ("Marié(e)");
        conjoint.setValide(true);
        conjoint.setUpdated(new Date());
        return conjoint;
    }

    private AdhEnfant createEnfant(AdhAgent agent, String nom, String prenom, String cin, String sexe) {
        AdhEnfant enfant = new AdhEnfant();
        enfant.setAdhAgent(agent);
        enfant.setCodAg(agent.getCodAg());
        enfant.setNom_pac(nom);
        enfant.setPr_pac(prenom);
        enfant.setCin_PAC(cin);
        enfant.setSex_pac(sexe);
        enfant.setDat_n_pac(new Date(System.currentTimeMillis() - (10L * 365 * 24 * 60 * 60 * 1000))); // 10 years ago
        enfant.setNiv_INSTRUCTION("Primaire");
        enfant.setLien_PAR("Enfant");
        enfant.setValide(true);
        enfant.setUpdated(new Date());
        enfant.setCreated(new Date());
        return enfant;
    }

    private Fichier createFichier(String codAg, String idAdh, String fileName, String designation, String extension) {
        Fichier fichier = new Fichier();
        fichier.setCod_ag(codAg);
        fichier.setIdadh(idAdh);
        fichier.setFileName(fileName);
        fichier.setDesignation(designation);
        fichier.setExtention(extension);
        fichier.setCreated(new Date());
        fichier.setDocument("sample_document_content");
        return fichier;
    }

    private Resultat createResultat(String operation, Long operationId, String statut, String ppr, String idAdh) {
        Resultat resultat = new Resultat();
        resultat.setOperation(operation);
        resultat.setOperation_id(operationId);
        resultat.setStatut(statut);
        resultat.setPpr(ppr);
        resultat.setIdadh(idAdh);
        return resultat;
    }
}