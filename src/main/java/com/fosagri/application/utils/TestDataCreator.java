package com.fosagri.application.utils;

import com.fosagri.application.entities.PrestationRef;
import com.fosagri.application.forms.*;
import com.fosagri.application.services.PrestationRefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class TestDataCreator {

    @Autowired
    private PrestationRefService prestationRefService;

    public void createTestPrestation() {
        try {
            // Créer une prestation de test simple
            PrestationRef prestation = new PrestationRef();
            prestation.setLabel("Test - Demande d'Aide Agricole");
            prestation.setType("AIDE");
            prestation.setDescription("Prestation de test pour valider le système de demandes avec formulaire dynamique.");
            prestation.setIs_adh("ALL");
            prestation.setOpen(true);
            prestation.setStatut("ACTIVE");
            prestation.setNombreLimit(100);
            prestation.setIsarabic(false);
            prestation.setIsattached(false);
            
            // Dates
            Calendar cal = Calendar.getInstance();
            prestation.setDateDu(cal.getTime());
            cal.add(Calendar.YEAR, 1);
            prestation.setDateAu(cal.getTime());
            
            // Créer un formulaire simple
            FormSchema schema = new FormSchema();
            schema.setKey("test_aide");
            schema.setTitle("Formulaire de Demande d'Aide");
            
            List<FormField> fields = new ArrayList<>();
            
            // Champ nom du projet
            FormField nomProjet = new FormField();
            nomProjet.setName("nom_projet");
            nomProjet.setLabel("Nom du projet");
            nomProjet.setType("text");
            nomProjet.setPlaceholder("Entrez le nom de votre projet...");
            nomProjet.setRequired(true);
            nomProjet.setOrder(1);
            fields.add(nomProjet);
            
            // Champ description
            FormField description = new FormField();
            description.setName("description_projet");
            description.setLabel("Description du projet");
            description.setType("textarea");
            description.setPlaceholder("Décrivez votre projet en détail...");
            description.setRequired(true);
            description.setOrder(2);
            fields.add(description);
            
            // Champ montant
            FormField montant = new FormField();
            montant.setName("montant_demande");
            montant.setLabel("Montant demandé (DH)");
            montant.setType("number");
            montant.setPlaceholder("0");
            montant.setRequired(true);
            montant.setOrder(3);
            fields.add(montant);
            
            // Champ type d'exploitation
            FormField typeExploitation = new FormField();
            typeExploitation.setName("type_exploitation");
            typeExploitation.setLabel("Type d'exploitation");
            typeExploitation.setType("select");
            typeExploitation.setRequired(true);
            typeExploitation.setOrder(4);
            
            // Options pour le select
            List<FieldOption> options = new ArrayList<>();
            options.add(new FieldOption("cereales", "Céréales"));
            options.add(new FieldOption("legumes", "Légumes"));
            options.add(new FieldOption("fruits", "Fruits"));
            options.add(new FieldOption("elevage", "Élevage"));
            options.add(new FieldOption("mixte", "Exploitation mixte"));
            typeExploitation.setOptions(options);
            fields.add(typeExploitation);
            
            // Champ date de début
            FormField dateDebut = new FormField();
            dateDebut.setName("date_debut_projet");
            dateDebut.setLabel("Date de début prévue");
            dateDebut.setType("date");
            dateDebut.setRequired(true);
            dateDebut.setOrder(5);
            fields.add(dateDebut);
            
            // Champ urgent
            FormField urgent = new FormField();
            urgent.setName("urgent");
            urgent.setLabel("Demande urgente");
            urgent.setType("checkbox");
            urgent.setRequired(false);
            urgent.setOrder(6);
            fields.add(urgent);
            
            schema.setFields(fields);
            prestation.setFormSchemaJson(schema.toJson());
            
            prestationRefService.save(prestation);
            
            System.out.println("✅ Prestation de test créée avec succès !");
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la création de la prestation de test : " + e.getMessage());
            e.printStackTrace();
        }
    }
}