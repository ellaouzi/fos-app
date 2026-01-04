package com.fosagri.application.services;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FosAgriKnowledgeService {

    public record Organization(String name, String fullName, String website, String email, String phone, String fax, String mission) {}
    public record Category(String id, String name, String icon, String color) {}
    public record ContentItem(int id, String title, String description, String category, String url, String type,
                              List<String> keywords, String details, String eligibility, List<String> services,
                              List<String> rules, List<String> facilities, List<String> steps, String price) {}
    public record SearchResult(ContentItem item, int relevance) {}

    private final Organization organization;
    private final List<Category> categories;
    private final List<ContentItem> content;

    public FosAgriKnowledgeService() {
        this.organization = new Organization(
            "FOS-Agri",
            "Fondation pour la Promotion des Œuvres Sociales du Personnel du Ministère de l'Agriculture",
            "https://www.fos-agri.ma",
            "fos-agri@fos-agri.ma",
            "05 37 66 55 40",
            "05 37 89 84 31",
            "Soutenir les adhérents du Ministère de l'Agriculture à travers des prestations sociales, éducatives, de loisirs et d'accès au logement."
        );

        this.categories = List.of(
            new Category("all", "Tout", "HOME", "blue"),
            new Category("prestations", "Prestations", "HEART", "red"),
            new Category("club", "Club Agriculture", "USERS", "green"),
            new Category("education", "Éducation", "ACADEMY_CAP", "purple"),
            new Category("logement", "Logement", "HOME", "orange"),
            new Category("partenaires", "Partenaires", "HANDSHAKE", "teal"),
            new Category("documents", "Documents PDF", "FILE", "gray")
        );

        this.content = initializeContent();
    }

    private List<ContentItem> initializeContent() {
        List<ContentItem> items = new ArrayList<>();

        // PRESTATIONS
        items.add(new ContentItem(1, "Prévoyance Médico-Sociale",
            "Soutenir nos adhérents face aux aléas de la santé. Conventions et partenariats médicaux au niveau central et régional.",
            "prestations", "https://www.fos-agri.ma/prestations/prest-pms.html", "page",
            List.of("santé", "médical", "assurance", "maladie", "prévoyance", "hospitalisation", "soins", "remboursement"),
            "Programme complet de prévoyance incluant: consultations médicales, analyses, radiologie, hospitalisation, médicaments.",
            "Tous les adhérents FOS-Agri et leurs ayants droit", null, null, null, null, null));

        items.add(new ContentItem(2, "Culture, Loisirs et Voyages",
            "Appuyer nos adhérents pour l'accès à des prestations de loisirs et bien-être.",
            "prestations", "https://fos-agri.ma/prestations/prest-clv.html", "page",
            List.of("loisirs", "voyages", "vacances", "estivage", "omra", "pèlerinage", "excursions", "colonies"),
            null, null,
            List.of("Estivage des familles", "Séjours d'hiver", "Colonies de vacances", "Omra", "Voyages organisés"),
            null, null, null, null));

        // EDUCATION
        items.add(new ContentItem(3, "Formation et Appui à la Scolarisation",
            "Encourager l'excellence scolaire des enfants de nos adhérents et la formation continue.",
            "education", "https://www.fos-agri.ma/prestations/prest-fas.html", "page",
            List.of("éducation", "scolarité", "formation", "bourses", "excellence", "enfants", "école", "études"),
            null, null, null, null, null, null, null));

        items.add(new ContentItem(4, "Bourses d'Excellence",
            "Attribution de bourses aux élèves et étudiants méritants pour encourager l'excellence académique.",
            "education", "https://www.fos-agri.ma/prestations/fas/bourses_excellence.html", "page",
            List.of("bourses", "excellence", "mérite", "étudiants", "aide financière", "récompense"),
            "Basé sur les résultats scolaires exceptionnels et le mérite académique", null, null, null, null, null, null));

        items.add(new ContentItem(5, "Primes de Rentrée Scolaire",
            "Aide financière accordée aux adhérents pour la rentrée scolaire de leurs enfants.",
            "education", "https://www.fos-agri.ma/prestations/fas/primes_de_rentree_scolaire.html", "page",
            List.of("prime", "rentrée", "scolaire", "aide", "fournitures", "frais", "inscription"),
            null, null, null, null, null, null, null));

        items.add(new ContentItem(6, "Coaching Scolaire et Parental",
            "Programme de coaching pour accompagner les élèves et leurs parents dans le parcours scolaire.",
            "education", "https://www.fos-agri.ma/prestations/fas/coaching_scolaire.html", "page",
            List.of("coaching", "scolaire", "parental", "accompagnement", "élèves", "parents", "orientation"),
            null, null, null, null, null, null, null));

        // CLUB
        items.add(new ContentItem(7, "Club de l'Agriculture - Inscription",
            "Plateforme d'inscription au Club de l'Agriculture. Accès à la piscine couverte, salle fitness.",
            "club", "https://fos-agri.ma/club.html", "page",
            List.of("club", "inscription", "piscine", "fitness", "sport", "gym", "adhésion", "natation"),
            null, null, null,
            List.of("Enfant < 5 ans : exonéré des frais", "Enfant ≥ 26 ans : considéré comme adulte", "Enfant < 16 ans : pas accès au GYM"),
            List.of("Piscine couverte", "Salle de fitness", "Salle de sport", "Espaces détente"), null, null));

        // LOGEMENT
        items.add(new ContentItem(8, "Accès au Logement",
            "Accompagner nos adhérents dans la réalisation de leurs projets immobiliers.",
            "logement", "https://www.fos-agri.ma/prestations/prest-aul.html", "page",
            List.of("logement", "immobilier", "achat", "maison", "appartement", "terrain", "villa", "crédit"),
            null, null, null, null, null, null, null));

        items.add(new ContentItem(9, "Offre Alliance Darna - Immobilier",
            "Offre commerciale immobilière: Logements et terrains dans plusieurs villes avec remises exclusives.",
            "logement", "https://www.fos-agri.ma/assets/documents/OFFRE-ALLIANCE-DARNA.pdf", "pdf",
            List.of("Alliance Darna", "immobilier", "logement", "terrain", "villa", "appartement", "remise"),
            null, null, null, null, null, null, null));

        // PARTENAIRES
        items.add(new ContentItem(10, "Partenaire BMCI",
            "Banque BMCI - Partenaire bancaire offrant des crédits et services financiers avantageux.",
            "partenaires", "https://www.fos-agri.ma", "partner",
            List.of("BMCI", "banque", "crédit", "compte", "partenaire", "financement"),
            null, null, List.of("Comptes bancaires", "Crédits immobiliers", "Crédits consommation"), null, null, null, null));

        items.add(new ContentItem(11, "Partenaire Banque Populaire",
            "Banque Populaire - Partenaire bancaire majeur avec services dédiés aux fonctionnaires.",
            "partenaires", "https://www.fos-agri.ma", "partner",
            List.of("Banque Populaire", "banque", "compte", "crédit", "épargne", "fonctionnaire"),
            null, null, null, null, null, null, null));

        items.add(new ContentItem(12, "Partenaire KITEA",
            "KITEA - Partenaire ameublement et décoration avec réductions exclusives.",
            "partenaires", "https://www.fos-agri.ma", "partner",
            List.of("KITEA", "meubles", "ameublement", "décoration", "maison", "mobilier"),
            null, null, null, null, null, null, null));

        items.add(new ContentItem(13, "Partenaire Wafa Assurance",
            "Wafa Assurance - Partenaire assurances avec offres préférentielles.",
            "partenaires", "https://www.fos-agri.ma", "partner",
            List.of("Wafa", "assurance", "couverture", "protection", "auto", "habitation"),
            null, null, null, null, null, null, null));

        // DOCUMENTS
        items.add(new ContentItem(14, "Guide d'Inscription - Club",
            "Guide d'utilisation de la plateforme d'inscription au Club de l'Agriculture.",
            "documents", "https://fos-agri.ma/assets/club/guide.pdf", "pdf",
            List.of("guide", "inscription", "plateforme", "club", "formulaire", "tutoriel"),
            null, null, null, null, null,
            List.of("Connexion plateforme", "Renseignement données", "Ajout membres famille", "Choix du pack", "Paiement"), null));

        items.add(new ContentItem(15, "Procédure Activation INWI",
            "Procédure complète pour bénéficier de l'offre mobile INWI. Forfaits avantageux.",
            "documents", "https://www.fos-agri.ma/assets/news/docs/ProcedureactivationFOSAGRIINWI.pdf", "pdf",
            List.of("INWI", "mobile", "téléphone", "abonnement", "forfait", "activation", "4G", "internet"),
            "Délai: 20 jours après envoi du dossier complet. Engagement 24 mois.", null, null, null, null, null, null));

        items.add(new ContentItem(16, "Assurance Voyage - SNTL",
            "Tarif spécial Fondations: 199 DHS seulement pour les assurés SNTL Assurances.",
            "documents", "https://www.fos-agri.ma/assets/news/docs/AfficheAssuranceVoyage.pdf", "pdf",
            List.of("assurance", "voyage", "SNTL", "tarif spécial", "couverture", "étranger"),
            null, null, null, null, null, null, "199 DHS"));

        items.add(new ContentItem(17, "E-Services FOS-Agri",
            "Portail des services en ligne de la FOS-Agri. Accès aux demandes et suivi des dossiers.",
            "prestations", "https://fos-agri.ma/e-services/", "page",
            List.of("e-services", "en ligne", "portail", "digital", "services", "demande", "suivi"),
            null, null, null, null, null, null, null));

        items.add(new ContentItem(18, "Conventions Médicales",
            "Liste des conventions médicales au niveau central et régional. Cliniques, laboratoires.",
            "prestations", "https://www.fos-agri.ma/prestations/pms/conventions_medicaux.html", "page",
            List.of("conventions", "médicaux", "partenariats", "soins", "cliniques", "hôpitaux", "laboratoires"),
            null, null, null, null, null, null, null));

        return items;
    }

    public Organization getOrganization() {
        return organization;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public List<ContentItem> getAllContent() {
        return content;
    }

    public List<ContentItem> getContentByCategory(String categoryId) {
        if ("all".equals(categoryId)) return content;
        return content.stream()
            .filter(item -> item.category().equals(categoryId))
            .collect(Collectors.toList());
    }

    public List<ContentItem> getContentByType(String type) {
        if ("all".equals(type)) return content;
        return content.stream()
            .filter(item -> item.type().equals(type))
            .collect(Collectors.toList());
    }

    public List<SearchResult> search(String query, String categoryFilter, String typeFilter) {
        if (query == null || query.trim().isEmpty()) {
            return content.stream()
                .filter(item -> "all".equals(categoryFilter) || item.category().equals(categoryFilter))
                .filter(item -> "all".equals(typeFilter) || item.type().equals(typeFilter))
                .map(item -> new SearchResult(item, 0))
                .collect(Collectors.toList());
        }

        String normalizedQuery = normalizeText(query);
        String[] searchTerms = normalizedQuery.split("\\s+");

        return content.stream()
            .filter(item -> "all".equals(categoryFilter) || item.category().equals(categoryFilter))
            .filter(item -> "all".equals(typeFilter) || item.type().equals(typeFilter))
            .map(item -> new SearchResult(item, calculateRelevance(item, searchTerms)))
            .filter(result -> result.relevance() > 0)
            .sorted((a, b) -> Integer.compare(b.relevance(), a.relevance()))
            .collect(Collectors.toList());
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        return java.text.Normalizer.normalize(text.toLowerCase(), java.text.Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .replaceAll("[^\\w\\s]", " ");
    }

    private int calculateRelevance(ContentItem item, String[] searchTerms) {
        int score = 0;
        String titleNorm = normalizeText(item.title());
        String descNorm = normalizeText(item.description());
        List<String> keywordsNorm = item.keywords() != null ?
            item.keywords().stream().map(this::normalizeText).collect(Collectors.toList()) :
            Collections.emptyList();

        for (String term : searchTerms) {
            if (term.length() < 2) continue;

            if (titleNorm.contains(term)) {
                score += 100;
                if (titleNorm.startsWith(term)) score += 50;
            }
            if (keywordsNorm.stream().anyMatch(k -> k.equals(term))) score += 80;
            if (keywordsNorm.stream().anyMatch(k -> k.contains(term))) score += 40;
            if (descNorm.contains(term)) score += 20;
        }

        return score;
    }
}
