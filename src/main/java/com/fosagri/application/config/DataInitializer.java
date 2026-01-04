package com.fosagri.application.config;

import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.forms.FormSchema;
import com.fosagri.application.forms.FormField;
import com.fosagri.application.model.Authority;
import com.fosagri.application.model.Utilisateur;
import com.fosagri.application.repository.UtilisateurRepository;
import com.fosagri.application.services.PrestationRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PrestationRefService prestationRefService;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialize default admin user
        initializeDefaultUsers();

        // Vérifier s'il y a déjà des prestations
        if (prestationRefService.count() == 0) {
            initializeSamplePrestations();
        }
    }

    private void initializeDefaultUsers() {
        // Only create users if the database is empty (fresh install)
        if (utilisateurRepository.count() > 0) {
            System.out.println("Users already exist in database, skipping default user creation.");
            return;
        }

        try {
            // Create admin user
            Utilisateur admin = new Utilisateur();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNom("Administrateur");
            admin.setPrenom("Système");
            admin.setEmail("admin@fosagri.ma");
            admin.setEnabled(true);
            admin.setCreated(new Date());

            // Create authority
            List<Authority> authorities = new ArrayList<>();
            Authority adminRole = new Authority("admin", "ADMIN", admin);
            authorities.add(adminRole);
            admin.setAuthorities(authorities);

            utilisateurRepository.save(admin);
            System.out.println("Admin user created: admin / admin123");

            // Create a regular user
            Utilisateur user = new Utilisateur();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setNom("Utilisateur");
            user.setPrenom("Test");
            user.setEmail("user@fosagri.ma");
            user.setEnabled(true);
            user.setCreated(new Date());

            // Create authority
            List<Authority> userAuthorities = new ArrayList<>();
            Authority userRole = new Authority("user", "USER", user);
            userAuthorities.add(userRole);
            user.setAuthorities(userAuthorities);

            utilisateurRepository.save(user);
            System.out.println("Regular user created: user / user123");
        } catch (Exception e) {
            System.out.println("Default users already exist or could not be created: " + e.getMessage());
        }
    }

    private void initializeSamplePrestations() {
        try {
            // 1. Prestation de Formation
            PrestationRef formation = new PrestationRef();
            formation.setLabel("Formation en Agriculture Durable");
            formation.setType("FORMATION");
            formation.setDescription("Formation destinée aux agriculteurs pour les techniques d'agriculture durable et biologique.");
            formation.setIs_adh("ADH_ONLY");
            formation.setOpen(true);
            formation.setStatut("ACTIVE");
            formation.setNombreLimit(20);
            formation.setIsarabic(false);
            formation.setIsattached(true);
            
            // Dates
            Calendar cal = Calendar.getInstance();
            formation.setDateDu(cal.getTime());
            cal.add(Calendar.MONTH, 3);
            formation.setDateAu(cal.getTime());
            
            // Formulaire dynamique pour la formation
            FormSchema formationSchema = new FormSchema();
            formationSchema.setKey("formation_agriculture");
            formationSchema.setTitle("Demande de Formation en Agriculture Durable");
            
            List<FormField> formationFields = new ArrayList<>();
            
            FormField motif = new FormField();
            motif.setName("motif");
            motif.setLabel("Motif de la demande");
            motif.setType("textarea");
            motif.setPlaceholder("Décrivez pourquoi vous souhaitez suivre cette formation...");
            motif.setRequired(true);
            motif.setOrder(1);
            formationFields.add(motif);
            
            FormField experience = new FormField();
            experience.setName("experience");
            experience.setLabel("Années d'expérience en agriculture");
            experience.setType("number");
            experience.setPlaceholder("Nombre d'années");
            experience.setRequired(true);
            experience.setOrder(2);
            formationFields.add(experience);
            
            FormField disponibilite = new FormField();
            disponibilite.setName("disponibilite");
            disponibilite.setLabel("Période de disponibilité préférée");
            disponibilite.setType("select");
            disponibilite.setRequired(true);
            disponibilite.setOrder(3);
            // Nous ajouterons les options via la méthode setValeurs pour simplifier
            formationFields.add(disponibilite);
            
            formationSchema.setFields(formationFields);
            formation.setFormSchemaJson(formationSchema.toJson());
            
            prestationRefService.save(formation);
            
            // 2. Prestation de Consultation
            PrestationRef consultation = new PrestationRef();
            consultation.setLabel("Consultation Technique Agricole");
            consultation.setType("CONSULTATION");
            consultation.setDescription("Consultation technique personnalisée pour optimiser vos pratiques agricoles.");
            consultation.setIs_adh("ALL");
            consultation.setOpen(true);
            consultation.setStatut("ACTIVE");
            consultation.setNombreLimit(50);
            consultation.setIsarabic(false);
            consultation.setIsattached(false);
            
            // Dates
            cal = Calendar.getInstance();
            consultation.setDateDu(cal.getTime());
            cal.add(Calendar.YEAR, 1);
            consultation.setDateAu(cal.getTime());
            
            // Formulaire pour consultation
            FormSchema consultationSchema = new FormSchema();
            consultationSchema.setKey("consultation_technique");
            consultationSchema.setTitle("Demande de Consultation Technique");
            
            List<FormField> consultationFields = new ArrayList<>();
            
            FormField problematique = new FormField();
            problematique.setName("problematique");
            problematique.setLabel("Problématique à résoudre");
            problematique.setType("textarea");
            problematique.setPlaceholder("Décrivez votre problématique technique...");
            problematique.setRequired(true);
            problematique.setOrder(1);
            consultationFields.add(problematique);
            
            FormField superficie = new FormField();
            superficie.setName("superficie");
            superficie.setLabel("Superficie concernée (en hectares)");
            superficie.setType("number");
            superficie.setPlaceholder("Surface en hectares");
            superficie.setRequired(false);
            superficie.setOrder(2);
            consultationFields.add(superficie);
            
            FormField urgence = new FormField();
            urgence.setName("urgence");
            urgence.setLabel("Caractère urgent");
            urgence.setType("checkbox");
            urgence.setRequired(false);
            urgence.setOrder(3);
            consultationFields.add(urgence);
            
            consultationSchema.setFields(consultationFields);
            consultation.setFormSchemaJson(consultationSchema.toJson());
            
            prestationRefService.save(consultation);
            
            // 3. Prestation d'Aide
            PrestationRef aide = new PrestationRef();
            aide.setLabel("Aide aux Jeunes Agriculteurs");
            aide.setType("AIDE");
            aide.setDescription("Programme d'aide financière et technique pour les jeunes agriculteurs de moins de 35 ans.");
            aide.setIs_adh("ADH_ONLY");
            aide.setOpen(true);
            aide.setStatut("ACTIVE");
            aide.setNombreLimit(10);
            aide.setIsarabic(false);
            aide.setIsattached(true);
            
            // Dates
            cal = Calendar.getInstance();
            aide.setDateDu(cal.getTime());
            cal.add(Calendar.MONTH, 6);
            aide.setDateAu(cal.getTime());
            
            // Formulaire simple pour aide
            FormSchema aideSchema = new FormSchema();
            aideSchema.setKey("aide_jeunes");
            aideSchema.setTitle("Demande d'Aide pour Jeunes Agriculteurs");
            
            List<FormField> aideFields = new ArrayList<>();
            
            FormField age = new FormField();
            age.setName("age");
            age.setLabel("Âge");
            age.setType("number");
            age.setPlaceholder("Votre âge");
            age.setRequired(true);
            age.setOrder(1);
            aideFields.add(age);
            
            FormField projet = new FormField();
            projet.setName("projet");
            projet.setLabel("Description du projet agricole");
            projet.setType("textarea");
            projet.setPlaceholder("Décrivez votre projet agricole...");
            projet.setRequired(true);
            projet.setOrder(2);
            aideFields.add(projet);
            
            FormField montant = new FormField();
            montant.setName("montant_demande");
            montant.setLabel("Montant d'aide demandé (DH)");
            montant.setType("number");
            montant.setPlaceholder("Montant en dirhams");
            montant.setRequired(true);
            montant.setOrder(3);
            aideFields.add(montant);
            
            aideSchema.setFields(aideFields);
            aide.setFormSchemaJson(aideSchema.toJson());
            
            prestationRefService.save(aide);
            
            System.out.println("✅ Prestations d'exemple créées avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'initialisation des prestations : " + e.getMessage());
            e.printStackTrace();
        }
    }
}