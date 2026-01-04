import React, { useState, useMemo, useCallback } from 'react';
import { Search, FileText, ExternalLink, Filter, Home, Users, GraduationCap, Heart, Building, Handshake, ChevronDown, ChevronUp, MapPin, Phone, Mail, Download, Sparkles, Bot, Loader2, RefreshCw, Globe, Lightbulb, MessageSquare, X, Send, Zap } from 'lucide-react';

// ============================================
// FOS-AGRI KNOWLEDGE BASE
// ============================================
const fosAgriKnowledgeBase = {
  organization: {
    name: "FOS-Agri",
    fullName: "Fondation pour la Promotion des Œuvres Sociales du Personnel du Ministère de l'Agriculture et de la Pêche Maritime - Département de l'Agriculture",
    website: "https://www.fos-agri.ma",
    email: "fos-agri@fos-agri.ma",
    phone: "05 37 66 55 40",
    fax: "05 37 89 84 31",
    mission: "Soutenir les adhérents du Ministère de l'Agriculture à travers des prestations sociales, éducatives, de loisirs et d'accès au logement."
  },
  
  categories: [
    { id: "all", name: "Tout", icon: "Home", color: "blue" },
    { id: "prestations", name: "Prestations", icon: "Heart", color: "red" },
    { id: "club", name: "Club Agriculture", icon: "Users", color: "green" },
    { id: "education", name: "Éducation", icon: "GraduationCap", color: "purple" },
    { id: "logement", name: "Logement", icon: "Building", color: "orange" },
    { id: "partenaires", name: "Partenaires", icon: "Handshake", color: "teal" },
    { id: "documents", name: "Documents PDF", icon: "FileText", color: "gray" }
  ],

  content: [
    // PRESTATIONS MÉDICO-SOCIALES
    {
      id: 1,
      title: "Prévoyance Médico-Sociale",
      description: "Soutenir nos adhérents face aux aléas de la santé. Conventions et partenariats médicaux au niveau central et régional. Couverture des frais médicaux, hospitalisation, et soins spécialisés.",
      category: "prestations",
      url: "https://www.fos-agri.ma/prestations/prest-pms.html",
      type: "page",
      keywords: ["santé", "médical", "assurance", "maladie", "prévoyance", "convention médicale", "partenariat médical", "hospitalisation", "soins", "remboursement"],
      details: "Programme complet de prévoyance incluant: consultations médicales, analyses, radiologie, hospitalisation, médicaments, lunetterie, dentaire, maternité.",
      eligibility: "Tous les adhérents FOS-Agri et leurs ayants droit (conjoint et enfants)"
    },
    {
      id: 2,
      title: "Culture, Loisirs et Voyages",
      description: "Appuyer nos adhérents pour l'accès à des prestations de loisirs et bien-être. Services variés pour le divertissement et l'épanouissement des familles.",
      category: "prestations",
      url: "https://fos-agri.ma/prestations/prest-clv.html",
      type: "page",
      keywords: ["loisirs", "voyages", "vacances", "estivage", "omra", "pèlerinage", "excursions", "colonies", "hiver", "été", "tourisme", "détente"],
      services: ["Estivage des familles", "Séjours d'hiver", "Colonies de vacances", "Omra", "Pèlerinage", "Voyages organisés", "Excursions", "Conventions hébergement"]
    },
    {
      id: 3,
      title: "Formation et Appui à la Scolarisation",
      description: "Encourager l'excellence scolaire des enfants de nos adhérents et la formation continue. Multiples programmes de soutien éducatif.",
      category: "education",
      url: "https://www.fos-agri.ma/prestations/prest-fas.html",
      type: "page",
      keywords: ["éducation", "scolarité", "formation", "bourses", "excellence", "enfants", "école", "études", "université"]
    },
    {
      id: 4,
      title: "Coaching Scolaire et Parental",
      description: "Programme de coaching pour accompagner les élèves et leurs parents dans le parcours scolaire. Orientation, motivation et techniques d'apprentissage.",
      category: "education",
      url: "https://www.fos-agri.ma/prestations/fas/coaching_scolaire_et_parental.html",
      type: "page",
      keywords: ["coaching", "scolaire", "parental", "accompagnement", "élèves", "parents", "orientation", "motivation"]
    },
    {
      id: 5,
      title: "Préparation aux Grandes Écoles",
      description: "Programme de préparation pour l'accès aux grandes écoles et établissements prestigieux. Classes préparatoires et concours.",
      category: "education",
      url: "https://www.fos-agri.ma/prestations/fas/preparation_aux_grandes_ecoles.html",
      type: "page",
      keywords: ["grandes écoles", "préparation", "concours", "études supérieures", "CPGE", "ingénieur", "médecine"]
    },
    {
      id: 6,
      title: "Bourses d'Excellence",
      description: "Attribution de bourses aux élèves et étudiants méritants pour encourager l'excellence académique. Aide financière basée sur les résultats.",
      category: "education",
      url: "https://www.fos-agri.ma/prestations/fas/bourses_excellence_et_prix_encouragement.html",
      type: "page",
      keywords: ["bourses", "excellence", "mérite", "étudiants", "aide financière", "récompense"],
      criteria: "Basé sur les résultats scolaires exceptionnels et le mérite académique"
    },
    {
      id: 7,
      title: "Prix d'Encouragement",
      description: "Récompenses pour les résultats scolaires exceptionnels des enfants des adhérents. Motivation et reconnaissance.",
      category: "education",
      url: "https://www.fos-agri.ma/prestations/fas/prix_encouragement.html",
      type: "page",
      keywords: ["prix", "encouragement", "récompense", "résultats", "scolaire", "motivation"]
    },
    {
      id: 8,
      title: "Primes de Rentrée Scolaire",
      description: "Aide financière accordée aux adhérents pour la rentrée scolaire de leurs enfants. Soutien pour fournitures et frais de scolarité.",
      category: "education",
      url: "https://www.fos-agri.ma/prestations/fas/primes_de_rentree_scolaire.html",
      type: "page",
      keywords: ["prime", "rentrée", "scolaire", "aide", "fournitures", "frais", "inscription"]
    },
    {
      id: 9,
      title: "Conventions Éducatives",
      description: "Partenariats avec établissements d'enseignement offrant des réductions aux adhérents. Écoles privées, universités, centres de formation.",
      category: "education",
      url: "https://www.fos-agri.ma/prestations/fas/conventions_educatives.html",
      type: "page",
      keywords: ["conventions", "éducatives", "écoles", "réductions", "partenariat", "privé", "formation"]
    },
    {
      id: 10,
      title: "Accès au Logement",
      description: "Accompagner nos adhérents dans la réalisation de leurs projets immobiliers. Terrains, appartements, villas avec facilités de paiement.",
      category: "logement",
      url: "https://www.fos-agri.ma/prestations/prest-aul.html",
      type: "page",
      keywords: ["logement", "immobilier", "achat", "maison", "appartement", "terrain", "villa", "crédit", "financement"]
    },
    {
      id: 11,
      title: "Autres Partenariats",
      description: "Faire bénéficier nos adhérents de différents partenariats et conventions avec divers organismes. Réductions exclusives.",
      category: "partenaires",
      url: "https://www.fos-agri.ma/prestations/prest-ap.html",
      type: "page",
      keywords: ["partenariats", "conventions", "avantages", "réductions", "offres", "exclusif"]
    },
    
    // CLUB DE L'AGRICULTURE
    {
      id: 12,
      title: "Club de l'Agriculture - Inscription",
      description: "Plateforme d'inscription au Club de l'Agriculture. Accès à la piscine couverte, salle fitness, et autres activités sportives et de loisirs.",
      category: "club",
      url: "https://fos-agri.ma/club.html",
      type: "page",
      keywords: ["club", "inscription", "piscine", "fitness", "sport", "gym", "adhésion", "carte", "natation"],
      rules: [
        "Enfant < 5 ans : exonéré des frais (doit être déclaré)",
        "Enfant ≥ 26 ans : considéré comme adulte",
        "Enfant < 16 ans : pas accès au GYM",
        "Paiement après validation du reçu d'inscription"
      ],
      facilities: ["Piscine couverte", "Salle de fitness", "Salle de sport", "Espaces détente"]
    },
    {
      id: 13,
      title: "Procédure d'adhésion au Club",
      description: "Guide complet pour adhérer au Club de l'Agriculture. Étapes d'inscription, documents requis, et modalités de paiement.",
      category: "club",
      url: "https://fos-agri.ma/club.html",
      type: "page",
      keywords: ["procédure", "adhésion", "club", "inscription", "carte", "membre", "étapes", "documents"]
    },
    
    // DOCUMENTS PDF
    {
      id: 14,
      title: "Guide d'Inscription - Club de l'Agriculture",
      description: "Guide d'utilisation de la plateforme d'inscription. Étapes détaillées pour renseigner les données adhérents, conjoints et enfants.",
      category: "documents",
      url: "https://fos-agri.ma/assets/club/guide.pdf",
      type: "pdf",
      keywords: ["guide", "inscription", "plateforme", "club", "pack", "formulaire", "photo", "CIN", "tutoriel"],
      steps: ["Connexion plateforme", "Renseignement données", "Ajout membres famille", "Choix du pack", "Paiement", "Génération carte"]
    },
    {
      id: 15,
      title: "Procédure Activation INWI FOS-Agri",
      description: "Procédure complète pour bénéficier de l'offre mobile INWI. Forfaits avantageux pour les adhérents.",
      category: "documents",
      url: "https://www.fos-agri.ma/assets/news/docs/ProcedureactivationFOSAGRIINWI.pdf",
      type: "pdf",
      keywords: ["INWI", "mobile", "téléphone", "abonnement", "forfait", "activation", "procédure", "4G", "internet"],
      contacts: {
        emails: ["nabil.daoui@inwi.ma", "Anas.Cherkaoui@inwi.ma"],
        address: "Agence INWI, Angle Avenue des Nations Unies et Avenue Omar ibn al khattab, AGDAL, RABAT"
      },
      processingTime: "20 jours après envoi du dossier complet",
      commitment: "Engagement 24 mois"
    },
    {
      id: 16,
      title: "Contrat Mobile INWI FOS-Agri",
      description: "Bon de commande et contrat pour l'offre mobile INWI Business destinée aux adhérents FOS-Agri.",
      category: "documents",
      url: "https://fos-agri.ma/assets/news/docs/ContratMobileINWIFOSAgri.pdf",
      type: "pdf",
      keywords: ["contrat", "INWI", "mobile", "business", "bon de commande", "formulaire"]
    },
    {
      id: 17,
      title: "Liste des Magasins Biougnach",
      description: "Liste des magasins Biougnach partenaires dans plusieurs villes du Maroc.",
      category: "documents",
      url: "https://www.fos-agri.ma/assets/news/docs/ListemagasinsBiougnach.pdf",
      type: "pdf",
      keywords: ["Biougnach", "magasins", "partenaire", "électroménager", "meubles"],
      locations: ["Meknès", "El Jadida", "Oujda", "Agadir - Label Gallery"]
    },
    {
      id: 18,
      title: "Assurance Voyage - SNTL Assurances",
      description: "Tarif spécial Fondations: 199 DHS seulement pour les assurés auprès de SNTL Assurances. Couverture voyage complète.",
      category: "documents",
      url: "https://www.fos-agri.ma/assets/news/docs/AfficheAssuranceVoyagepourlesFondations.pdf",
      type: "pdf",
      keywords: ["assurance", "voyage", "SNTL", "tarif spécial", "couverture", "étranger"],
      price: "199 DHS",
      coverage: "Assistance médicale, rapatriement, bagages"
    },
    {
      id: 19,
      title: "Offre Alliance Darna - Immobilier 2023",
      description: "Offre commerciale immobilière complète: Logements et terrains dans plusieurs villes marocaines avec remises exclusives.",
      category: "logement",
      url: "https://www.fos-agri.ma/assets/documents/conventions_et-offres/PROMOTEURS_IMMOBILIERS/OFFRE-ALLIANCE-DARNA.pdf",
      type: "pdf",
      keywords: ["Alliance Darna", "immobilier", "logement", "terrain", "villa", "appartement", "remise", "promoteur"],
      offers: [
        { ville: "Mdiq", type: "Logements", prix: "250.000 DHS HT", remise: "5%" },
        { ville: "Kenitra - Mehdia", type: "Logements", remise: "5%" },
        { ville: "Ain El Aouda - Rabat", type: "Riad El Kheir", prix: "225.000-250.000 DHS HT", remise: "5%" },
        { ville: "Mohammedia", type: "Terrains R+3", prix: "À partir de 4.720 DHS/m²", remise: "5-10%" },
        { ville: "Marrakech", type: "Terrains villas", prix: "À partir de 1.100 DHS/m²", remise: "5%" },
        { ville: "Beni Mellal", type: "Logements", remise: "5%" }
      ]
    },
    {
      id: 20,
      title: "Liste des Bénéficiaires Estivage 2022",
      description: "Liste officielle des bénéficiaires du programme d'estivage des familles pour l'année 2022.",
      category: "documents",
      url: "https://www.fos-agri.ma/assets/news/docs/LISTE_DES_BENEFICIAIRES_ESTIVAGE_2022.pdf",
      type: "pdf",
      keywords: ["estivage", "bénéficiaires", "vacances", "été", "2022", "liste", "familles"]
    },
    
    // PARTENAIRES
    {
      id: 21,
      title: "Partenaire BMCI",
      description: "Banque BMCI - Partenaire bancaire offrant des crédits et services financiers avantageux aux adhérents FOS-Agri.",
      category: "partenaires",
      url: "https://www.fos-agri.ma",
      type: "partner",
      keywords: ["BMCI", "banque", "crédit", "compte", "partenaire", "financement"],
      services: ["Comptes bancaires", "Crédits immobiliers", "Crédits consommation", "Épargne"]
    },
    {
      id: 22,
      title: "Partenaire KITEA",
      description: "KITEA - Partenaire ameublement et décoration avec réductions exclusives pour adhérents.",
      category: "partenaires",
      url: "https://www.fos-agri.ma",
      type: "partner",
      keywords: ["KITEA", "meubles", "ameublement", "décoration", "maison", "mobilier"]
    },
    {
      id: 23,
      title: "Partenaire SNTL",
      description: "SNTL - Partenaire logistique, transport et assurances.",
      category: "partenaires",
      url: "https://www.fos-agri.ma",
      type: "partner",
      keywords: ["SNTL", "transport", "logistique", "assurance"]
    },
    {
      id: 24,
      title: "Partenaire Wafa Assurance",
      description: "Wafa Assurance - Partenaire assurances avec offres préférentielles pour auto, habitation, santé.",
      category: "partenaires",
      url: "https://www.fos-agri.ma",
      type: "partner",
      keywords: ["Wafa", "assurance", "couverture", "protection", "auto", "habitation"]
    },
    {
      id: 25,
      title: "Partenaire Salafin",
      description: "Salafin - Partenaire crédit à la consommation avec taux préférentiels et facilités de paiement.",
      category: "partenaires",
      url: "https://www.fos-agri.ma",
      type: "partner",
      keywords: ["Salafin", "crédit", "financement", "consommation", "prêt"]
    },
    {
      id: 26,
      title: "Partenaire Banque Populaire",
      description: "Banque Populaire - Partenaire bancaire majeur avec services dédiés aux fonctionnaires.",
      category: "partenaires",
      url: "https://www.fos-agri.ma",
      type: "partner",
      keywords: ["Banque Populaire", "banque", "compte", "crédit", "épargne", "fonctionnaire"]
    },
    {
      id: 27,
      title: "Partenaire Wafa Immobilier",
      description: "Wafa Immobilier - Partenaire crédit immobilier avec taux préférentiels pour l'achat de logement.",
      category: "partenaires",
      url: "https://www.fos-agri.ma",
      type: "partner",
      keywords: ["Wafa Immobilier", "crédit", "logement", "immobilier", "achat", "maison"]
    },
    
    // SERVICES EN LIGNE
    {
      id: 28,
      title: "E-Services FOS-Agri",
      description: "Portail des services en ligne de la FOS-Agri. Accès aux demandes, suivi des dossiers, et informations.",
      category: "prestations",
      url: "https://fos-agri.ma/e-services/",
      type: "page",
      keywords: ["e-services", "en ligne", "portail", "digital", "services", "demande", "suivi"]
    },
    {
      id: 29,
      title: "Organismes d'Assurances Conventionnés",
      description: "Liste des organismes d'assurances partenaires. Présentation de la carte d'adhésion requise.",
      category: "partenaires",
      url: "https://fos-agri.ma/prestations/ap/organismes_assurances.html",
      type: "page",
      keywords: ["assurance", "organismes", "conventionnés", "carte adhésion", "couverture"]
    },
    {
      id: 30,
      title: "Conventions et Partenariats Médicaux",
      description: "Liste des conventions médicales au niveau central et régional. Cliniques, laboratoires, centres de soins.",
      category: "prestations",
      url: "https://www.fos-agri.ma/prestations/pms/conventions_et_partenariats_medicaux.html",
      type: "page",
      keywords: ["conventions", "médicaux", "partenariats", "soins", "cliniques", "hôpitaux", "laboratoires"]
    }
  ],

  // FAQ pour l'IA
  faq: [
    {
      question: "Comment adhérer à FOS-Agri?",
      answer: "L'adhésion à FOS-Agri est réservée au personnel du Ministère de l'Agriculture. Contactez le service adhésion au 05 37 66 55 40 ou par email à fos-agri@fos-agri.ma."
    },
    {
      question: "Quelles sont les prestations disponibles?",
      answer: "FOS-Agri offre: Prévoyance médico-sociale, Culture/Loisirs/Voyages, Formation et scolarisation, Accès au logement, et divers partenariats."
    },
    {
      question: "Comment s'inscrire au Club de l'Agriculture?",
      answer: "Visitez fos-agri.ma/club.html, suivez le guide d'inscription, renseignez vos données et celles de votre famille, choisissez un pack, et effectuez le paiement après validation."
    },
    {
      question: "Quels sont les partenaires bancaires?",
      answer: "Les partenaires bancaires incluent: BMCI, Banque Populaire, Wafa Immobilier, et Salafin pour les crédits."
    }
  ]
};

// ============================================
// AI SEARCH ENGINE COMPONENT
// ============================================
const AISearchEngine = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [expandedItems, setExpandedItems] = useState(new Set());
  const [typeFilter, setTypeFilter] = useState('all');
  const [aiResponse, setAiResponse] = useState(null);
  const [isAiLoading, setIsAiLoading] = useState(false);
  const [showAiChat, setShowAiChat] = useState(false);
  const [chatMessages, setChatMessages] = useState([]);
  const [chatInput, setChatInput] = useState('');
  const [aiMode, setAiMode] = useState('search'); // 'search' or 'chat'
  const [webSearchResults, setWebSearchResults] = useState(null);
  
  const iconMap = { Home, Users, GraduationCap, Heart, Building, Handshake, FileText };

  // Normalize text for search
  const normalizeText = (text) => {
    return text?.toLowerCase()
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/[^\w\s]/g, " ") || "";
  };

  // Calculate relevance score
  const calculateRelevance = (item, searchTerms) => {
    let score = 0;
    const titleNorm = normalizeText(item.title);
    const descNorm = normalizeText(item.description);
    const keywordsNorm = item.keywords?.map(k => normalizeText(k)) || [];
    
    searchTerms.forEach(term => {
      const termNorm = normalizeText(term);
      if (termNorm.length < 2) return;
      
      if (titleNorm.includes(termNorm)) {
        score += 100;
        if (titleNorm.startsWith(termNorm)) score += 50;
      }
      if (keywordsNorm.some(k => k === termNorm)) score += 80;
      if (keywordsNorm.some(k => k.includes(termNorm))) score += 40;
      if (descNorm.includes(termNorm)) score += 20;
    });
    
    return score;
  };

  // Build context for AI
  const buildAIContext = useCallback(() => {
    return `
Tu es l'assistant intelligent de FOS-Agri (Fondation pour la Promotion des Œuvres Sociales du Personnel du Ministère de l'Agriculture et de la Pêche Maritime - Département de l'Agriculture, Maroc).

INFORMATIONS ORGANISATION:
- Site web: ${fosAgriKnowledgeBase.organization.website}
- Email: ${fosAgriKnowledgeBase.organization.email}
- Téléphone: ${fosAgriKnowledgeBase.organization.phone}
- Mission: ${fosAgriKnowledgeBase.organization.mission}

PRESTATIONS DISPONIBLES:
${fosAgriKnowledgeBase.content.map(item => 
  `- ${item.title}: ${item.description} (${item.type === 'pdf' ? 'Document PDF: ' + item.url : 'Page: ' + item.url})`
).join('\n')}

FAQ:
${fosAgriKnowledgeBase.faq.map(f => `Q: ${f.question}\nR: ${f.answer}`).join('\n\n')}

PARTENAIRES:
- Banques: BMCI, Banque Populaire
- Crédit: Salafin, Wafa Immobilier
- Assurances: Wafa Assurance, SNTL Assurances
- Commerce: KITEA, Biougnach
- Télécom: INWI

RÈGLES CLUB AGRICULTURE:
- Enfant < 5 ans: exonéré des frais
- Enfant ≥ 26 ans: considéré comme adulte
- Enfant < 16 ans: pas d'accès au GYM
- Inscription via plateforme fos-agri.ma/club.html

Réponds de manière précise, professionnelle et en français. Fournis les liens pertinents quand disponibles. Si tu ne connais pas une information, dis-le clairement et suggère de contacter FOS-Agri directement.
`;
  }, []);

  // Call Claude API for intelligent response
  const callClaudeAPI = async (userQuery, conversationHistory = []) => {
    try {
      const messages = [
        ...conversationHistory,
        { role: "user", content: userQuery }
      ];

      const response = await fetch("https://api.anthropic.com/v1/messages", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          model: "claude-sonnet-4-20250514",
          max_tokens: 1000,
          system: buildAIContext(),
          messages: messages
        })
      });

      const data = await response.json();
      return data.content?.map(item => item.text || "").join("\n") || "Désolé, je n'ai pas pu traiter votre demande.";
    } catch (error) {
      console.error("AI API Error:", error);
      return "Erreur de connexion à l'assistant IA. Veuillez réessayer.";
    }
  };

  // Call Claude API with web search
  const callClaudeWithWebSearch = async (userQuery) => {
    try {
      const response = await fetch("https://api.anthropic.com/v1/messages", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          model: "claude-sonnet-4-20250514",
          max_tokens: 1500,
          system: buildAIContext() + "\n\nUtilise la recherche web pour trouver des informations actualisées sur FOS-Agri si nécessaire.",
          messages: [{ role: "user", content: userQuery }],
          tools: [{
            type: "web_search_20250305",
            name: "web_search"
          }]
        })
      });

      const data = await response.json();
      
      // Process response with potential web search results
      let fullResponse = "";
      let searchInfo = null;
      
      data.content?.forEach(item => {
        if (item.type === "text") {
          fullResponse += item.text;
        }
        if (item.type === "web_search_tool_result") {
          searchInfo = item;
        }
      });

      return { response: fullResponse || "Pas de réponse disponible.", webSearch: searchInfo };
    } catch (error) {
      console.error("AI API Error:", error);
      return { response: "Erreur de connexion. Veuillez réessayer.", webSearch: null };
    }
  };

  // Handle AI search
  const handleAISearch = async () => {
    if (!searchQuery.trim()) return;
    
    setIsAiLoading(true);
    setAiResponse(null);
    setWebSearchResults(null);
    
    const result = await callClaudeWithWebSearch(
      `Recherche FOS-Agri: "${searchQuery}". Donne une réponse complète avec les liens pertinents et les informations pratiques.`
    );
    
    setAiResponse(result.response);
    setWebSearchResults(result.webSearch);
    setIsAiLoading(false);
  };

  // Handle chat message
  const handleChatSubmit = async () => {
    if (!chatInput.trim()) return;
    
    const userMessage = { role: "user", content: chatInput };
    setChatMessages(prev => [...prev, userMessage]);
    setChatInput('');
    setIsAiLoading(true);
    
    const history = chatMessages.map(m => ({
      role: m.role,
      content: m.content
    }));
    
    const response = await callClaudeAPI(chatInput, history);
    
    setChatMessages(prev => [...prev, { role: "assistant", content: response }]);
    setIsAiLoading(false);
  };

  // Filter results
  const searchResults = useMemo(() => {
    let results = fosAgriKnowledgeBase.content;
    
    if (selectedCategory !== 'all') {
      results = results.filter(item => item.category === selectedCategory);
    }
    
    if (typeFilter !== 'all') {
      results = results.filter(item => item.type === typeFilter);
    }
    
    if (searchQuery.trim()) {
      const searchTerms = searchQuery.trim().split(/\s+/);
      results = results
        .map(item => ({ ...item, relevance: calculateRelevance(item, searchTerms) }))
        .filter(item => item.relevance > 0)
        .sort((a, b) => b.relevance - a.relevance);
    }
    
    return results;
  }, [searchQuery, selectedCategory, typeFilter]);

  const toggleExpand = (id) => {
    const newExpanded = new Set(expandedItems);
    if (newExpanded.has(id)) newExpanded.delete(id);
    else newExpanded.add(id);
    setExpandedItems(newExpanded);
  };

  const getTypeIcon = (type) => {
    switch(type) {
      case 'pdf': return <FileText className="w-4 h-4 text-red-500" />;
      case 'partner': return <Handshake className="w-4 h-4 text-teal-500" />;
      default: return <ExternalLink className="w-4 h-4 text-blue-500" />;
    }
  };

  const getTypeBadge = (type) => {
    const badges = {
      pdf: { bg: 'bg-red-100', text: 'text-red-700', label: 'PDF' },
      partner: { bg: 'bg-teal-100', text: 'text-teal-700', label: 'Partenaire' },
      page: { bg: 'bg-blue-100', text: 'text-blue-700', label: 'Page Web' }
    };
    const badge = badges[type] || badges.page;
    return <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${badge.bg} ${badge.text}`}>{badge.label}</span>;
  };

  // Suggested queries
  const suggestedQueries = [
    "Comment s'inscrire au Club?",
    "Bourses d'excellence",
    "Offres immobilières",
    "Forfait INWI",
    "Assurance voyage"
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-emerald-50 via-white to-blue-50">
      {/* Header */}
      <div className="bg-gradient-to-r from-emerald-700 via-green-600 to-teal-600 text-white shadow-xl">
        <div className="max-w-6xl mx-auto px-4 py-6">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 bg-white rounded-xl flex items-center justify-center shadow-lg">
                <span className="text-emerald-700 font-bold text-xl">FOS</span>
              </div>
              <div>
                <h1 className="text-2xl font-bold flex items-center gap-2">
                  <Sparkles className="w-6 h-6 text-yellow-300" />
                  Recherche IA FOS-Agri
                </h1>
                <p className="text-emerald-100 text-sm">Recherche intelligente alimentée par l'IA</p>
              </div>
            </div>
            
            {/* AI Mode Toggle */}
            <div className="flex items-center gap-2 bg-white/20 rounded-full p-1">
              <button
                onClick={() => setAiMode('search')}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                  aiMode === 'search' ? 'bg-white text-emerald-700' : 'text-white hover:bg-white/10'
                }`}
              >
                <Search className="w-4 h-4 inline mr-2" />
                Recherche
              </button>
              <button
                onClick={() => { setAiMode('chat'); setShowAiChat(true); }}
                className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${
                  aiMode === 'chat' ? 'bg-white text-emerald-700' : 'text-white hover:bg-white/10'
                }`}
              >
                <MessageSquare className="w-4 h-4 inline mr-2" />
                Assistant
              </button>
            </div>
          </div>
          
          {/* Search Bar with AI */}
          <div className="relative">
            <div className="absolute left-4 top-1/2 transform -translate-y-1/2 flex items-center gap-2">
              <Bot className="w-5 h-5 text-emerald-500" />
              <div className="w-px h-6 bg-gray-300"></div>
              <Search className="w-5 h-5 text-gray-400" />
            </div>
            <input
              type="text"
              placeholder="Posez votre question en langage naturel... (ex: Comment obtenir une bourse pour mon enfant?)"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              onKeyPress={(e) => e.key === 'Enter' && handleAISearch()}
              className="w-full pl-20 pr-32 py-4 rounded-xl text-gray-800 placeholder-gray-400 shadow-lg focus:ring-4 focus:ring-emerald-300 focus:outline-none text-lg"
            />
            <button
              onClick={handleAISearch}
              disabled={isAiLoading}
              className="absolute right-2 top-1/2 transform -translate-y-1/2 px-6 py-2 bg-gradient-to-r from-emerald-600 to-teal-600 text-white rounded-lg hover:from-emerald-700 hover:to-teal-700 transition-all flex items-center gap-2 disabled:opacity-50"
            >
              {isAiLoading ? (
                <Loader2 className="w-5 h-5 animate-spin" />
              ) : (
                <>
                  <Zap className="w-5 h-5" />
                  Recherche IA
                </>
              )}
            </button>
          </div>
          
          {/* Suggested Queries */}
          <div className="flex flex-wrap gap-2 mt-4">
            <span className="text-emerald-200 text-sm flex items-center gap-1">
              <Lightbulb className="w-4 h-4" /> Suggestions:
            </span>
            {suggestedQueries.map((query, idx) => (
              <button
                key={idx}
                onClick={() => { setSearchQuery(query); }}
                className="px-3 py-1 bg-white/20 hover:bg-white/30 rounded-full text-sm transition-all"
              >
                {query}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* AI Response Section */}
      {(aiResponse || isAiLoading) && (
        <div className="max-w-6xl mx-auto px-4 py-6">
          <div className="bg-gradient-to-r from-purple-50 to-blue-50 rounded-2xl p-6 shadow-lg border border-purple-100">
            <div className="flex items-center gap-3 mb-4">
              <div className="w-10 h-10 bg-gradient-to-r from-purple-500 to-blue-500 rounded-full flex items-center justify-center">
                <Bot className="w-5 h-5 text-white" />
              </div>
              <div>
                <h3 className="font-semibold text-gray-800">Réponse de l'Assistant IA</h3>
                <p className="text-sm text-gray-500">Analyse intelligente de votre requête</p>
              </div>
              {webSearchResults && (
                <span className="ml-auto flex items-center gap-1 text-sm text-blue-600 bg-blue-100 px-3 py-1 rounded-full">
                  <Globe className="w-4 h-4" />
                  Recherche web incluse
                </span>
              )}
            </div>
            
            {isAiLoading ? (
              <div className="flex items-center gap-3 text-gray-600">
                <Loader2 className="w-5 h-5 animate-spin" />
                <span>Analyse en cours avec l'IA...</span>
              </div>
            ) : (
              <div className="prose prose-sm max-w-none">
                <div className="text-gray-700 whitespace-pre-wrap leading-relaxed">
                  {aiResponse}
                </div>
              </div>
            )}
            
            <div className="mt-4 pt-4 border-t border-purple-200 flex items-center justify-between">
              <button
                onClick={() => setShowAiChat(true)}
                className="flex items-center gap-2 text-purple-600 hover:text-purple-700 text-sm font-medium"
              >
                <MessageSquare className="w-4 h-4" />
                Continuer la conversation
              </button>
              <button
                onClick={() => { setAiResponse(null); setSearchQuery(''); }}
                className="flex items-center gap-2 text-gray-500 hover:text-gray-700 text-sm"
              >
                <RefreshCw className="w-4 h-4" />
                Nouvelle recherche
              </button>
            </div>
          </div>
        </div>
      )}
      
      {/* Filters */}
      <div className="max-w-6xl mx-auto px-4 py-4">
        <div className="flex flex-wrap gap-2 mb-4">
          {fosAgriKnowledgeBase.categories.map(cat => {
            const Icon = iconMap[cat.icon];
            return (
              <button
                key={cat.id}
                onClick={() => setSelectedCategory(cat.id)}
                className={`flex items-center gap-2 px-4 py-2 rounded-full font-medium transition-all ${
                  selectedCategory === cat.id
                    ? 'bg-emerald-600 text-white shadow-md'
                    : 'bg-white text-gray-700 hover:bg-emerald-50 shadow'
                }`}
              >
                <Icon className="w-4 h-4" />
                {cat.name}
              </button>
            );
          })}
        </div>
        
        {/* Type Filter */}
        <div className="flex items-center gap-2 mb-4">
          <Filter className="w-4 h-4 text-gray-500" />
          <span className="text-sm text-gray-600">Type:</span>
          {[
            { id: 'all', label: 'Tous' },
            { id: 'page', label: 'Pages Web' },
            { id: 'pdf', label: 'Documents PDF' },
            { id: 'partner', label: 'Partenaires' }
          ].map(type => (
            <button
              key={type.id}
              onClick={() => setTypeFilter(type.id)}
              className={`px-3 py-1 rounded-lg text-sm transition-all ${
                typeFilter === type.id
                  ? 'bg-gray-800 text-white'
                  : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
              }`}
            >
              {type.label}
            </button>
          ))}
        </div>
        
        {/* Results Count */}
        <div className="text-sm text-gray-600 mb-4">
          {searchResults.length} résultat{searchResults.length > 1 ? 's' : ''} trouvé{searchResults.length > 1 ? 's' : ''}
          {searchQuery && ` pour "${searchQuery}"`}
        </div>
      </div>
      
      {/* Results */}
      <div className="max-w-6xl mx-auto px-4 pb-8">
        <div className="space-y-3">
          {searchResults.map(item => (
            <div
              key={item.id}
              className="bg-white rounded-xl shadow-md hover:shadow-lg transition-all overflow-hidden border border-gray-100"
            >
              <div className="p-4">
                <div className="flex items-start gap-4">
                  <div className="flex-shrink-0 w-12 h-12 bg-gradient-to-br from-emerald-100 to-teal-100 rounded-lg flex items-center justify-center">
                    {getTypeIcon(item.type)}
                  </div>
                  
                  <div className="flex-grow min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      {getTypeBadge(item.type)}
                      {item.relevance > 0 && (
                        <span className="text-xs text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">
                          Pertinence: {Math.round(item.relevance / 2)}%
                        </span>
                      )}
                    </div>
                    
                    <h3 className="font-semibold text-gray-800 text-lg mb-1">{item.title}</h3>
                    <p className="text-gray-600 text-sm mb-2">{item.description}</p>
                    
                    {/* Keywords */}
                    <div className="flex flex-wrap gap-1 mb-2">
                      {item.keywords?.slice(0, 5).map((keyword, idx) => (
                        <span key={idx} className="px-2 py-0.5 bg-gray-100 text-gray-600 rounded text-xs">
                          {keyword}
                        </span>
                      ))}
                    </div>
                    
                    {/* Expandable Details */}
                    {(item.rules || item.services || item.offers || item.contacts || item.steps) && (
                      <>
                        <button
                          onClick={() => toggleExpand(item.id)}
                          className="flex items-center gap-1 text-emerald-600 text-sm font-medium hover:text-emerald-700"
                        >
                          {expandedItems.has(item.id) ? (
                            <>Moins de détails <ChevronUp className="w-4 h-4" /></>
                          ) : (
                            <>Plus de détails <ChevronDown className="w-4 h-4" /></>
                          )}
                        </button>
                        
                        {expandedItems.has(item.id) && (
                          <div className="mt-3 p-4 bg-gradient-to-r from-gray-50 to-emerald-50 rounded-lg text-sm space-y-3">
                            {item.rules && (
                              <div>
                                <strong className="text-gray-700 flex items-center gap-2">
                                  <span className="w-1.5 h-1.5 bg-emerald-500 rounded-full"></span>
                                  Règles importantes:
                                </strong>
                                <ul className="list-disc list-inside mt-1 text-gray-600 ml-4">
                                  {item.rules.map((r, i) => <li key={i}>{r}</li>)}
                                </ul>
                              </div>
                            )}
                            
                            {item.services && (
                              <div>
                                <strong className="text-gray-700 flex items-center gap-2">
                                  <span className="w-1.5 h-1.5 bg-blue-500 rounded-full"></span>
                                  Services disponibles:
                                </strong>
                                <div className="flex flex-wrap gap-2 mt-2">
                                  {item.services.map((s, i) => (
                                    <span key={i} className="px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs">
                                      {s}
                                    </span>
                                  ))}
                                </div>
                              </div>
                            )}
                            
                            {item.steps && (
                              <div>
                                <strong className="text-gray-700 flex items-center gap-2">
                                  <span className="w-1.5 h-1.5 bg-purple-500 rounded-full"></span>
                                  Étapes:
                                </strong>
                                <ol className="list-decimal list-inside mt-1 text-gray-600 ml-4">
                                  {item.steps.map((s, i) => <li key={i}>{s}</li>)}
                                </ol>
                              </div>
                            )}
                            
                            {item.contacts && (
                              <div>
                                <strong className="text-gray-700 flex items-center gap-2">
                                  <span className="w-1.5 h-1.5 bg-orange-500 rounded-full"></span>
                                  Contacts:
                                </strong>
                                <div className="mt-1 text-gray-600 space-y-1">
                                  {item.contacts.emails && (
                                    <div className="flex items-center gap-2">
                                      <Mail className="w-4 h-4 text-gray-400" />
                                      {item.contacts.emails.join(', ')}
                                    </div>
                                  )}
                                  {item.contacts.address && (
                                    <div className="flex items-start gap-2">
                                      <MapPin className="w-4 h-4 text-gray-400 mt-0.5" />
                                      {item.contacts.address}
                                    </div>
                                  )}
                                </div>
                                {item.processingTime && (
                                  <p className="mt-2 text-orange-600">⏱ Délai: {item.processingTime}</p>
                                )}
                              </div>
                            )}
                            
                            {item.offers && (
                              <div>
                                <strong className="text-gray-700 flex items-center gap-2">
                                  <span className="w-1.5 h-1.5 bg-green-500 rounded-full"></span>
                                  Offres immobilières:
                                </strong>
                                <div className="mt-2 overflow-x-auto">
                                  <table className="min-w-full text-xs bg-white rounded-lg overflow-hidden">
                                    <thead className="bg-emerald-100">
                                      <tr>
                                        <th className="px-3 py-2 text-left">Ville</th>
                                        <th className="px-3 py-2 text-left">Type</th>
                                        <th className="px-3 py-2 text-left">Prix</th>
                                        <th className="px-3 py-2 text-left">Remise</th>
                                      </tr>
                                    </thead>
                                    <tbody>
                                      {item.offers.map((offer, i) => (
                                        <tr key={i} className="border-t">
                                          <td className="px-3 py-2">{offer.ville}</td>
                                          <td className="px-3 py-2">{offer.type}</td>
                                          <td className="px-3 py-2">{offer.prix || '-'}</td>
                                          <td className="px-3 py-2 text-emerald-600 font-semibold">{offer.remise}</td>
                                        </tr>
                                      ))}
                                    </tbody>
                                  </table>
                                </div>
                              </div>
                            )}
                          </div>
                        )}
                      </>
                    )}
                  </div>
                  
                  <a
                    href={item.url}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="flex-shrink-0 flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-emerald-600 to-teal-600 text-white rounded-lg hover:from-emerald-700 hover:to-teal-700 transition-all"
                  >
                    {item.type === 'pdf' ? <Download className="w-4 h-4" /> : <ExternalLink className="w-4 h-4" />}
                    <span className="hidden sm:inline">{item.type === 'pdf' ? 'Télécharger' : 'Visiter'}</span>
                  </a>
                </div>
              </div>
            </div>
          ))}
          
          {searchResults.length === 0 && !aiResponse && (
            <div className="text-center py-12">
              <Bot className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 className="text-xl font-semibold text-gray-600 mb-2">Aucun résultat trouvé</h3>
              <p className="text-gray-500 mb-4">Essayez la recherche IA pour une réponse personnalisée</p>
              <button
                onClick={handleAISearch}
                className="px-6 py-3 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-xl hover:from-purple-700 hover:to-blue-700 transition-all flex items-center gap-2 mx-auto"
              >
                <Sparkles className="w-5 h-5" />
                Demander à l'Assistant IA
              </button>
            </div>
          )}
        </div>
      </div>

      {/* AI Chat Floating Panel */}
      {showAiChat && (
        <div className="fixed bottom-4 right-4 w-96 h-[500px] bg-white rounded-2xl shadow-2xl border border-gray-200 flex flex-col overflow-hidden z-50">
          {/* Chat Header */}
          <div className="bg-gradient-to-r from-purple-600 to-blue-600 text-white p-4 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center">
                <Bot className="w-5 h-5" />
              </div>
              <div>
                <h4 className="font-semibold">Assistant FOS-Agri</h4>
                <p className="text-xs text-purple-200">Propulsé par Claude IA</p>
              </div>
            </div>
            <button onClick={() => setShowAiChat(false)} className="p-1 hover:bg-white/20 rounded">
              <X className="w-5 h-5" />
            </button>
          </div>
          
          {/* Chat Messages */}
          <div className="flex-grow overflow-y-auto p-4 space-y-4 bg-gray-50">
            {chatMessages.length === 0 && (
              <div className="text-center text-gray-500 py-8">
                <Bot className="w-12 h-12 mx-auto mb-3 text-purple-300" />
                <p className="text-sm">Bonjour! Je suis l'assistant FOS-Agri.</p>
                <p className="text-xs mt-1">Posez-moi vos questions sur les prestations.</p>
              </div>
            )}
            
            {chatMessages.map((msg, idx) => (
              <div key={idx} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div className={`max-w-[80%] p-3 rounded-2xl ${
                  msg.role === 'user' 
                    ? 'bg-purple-600 text-white rounded-br-md' 
                    : 'bg-white text-gray-800 rounded-bl-md shadow'
                }`}>
                  <p className="text-sm whitespace-pre-wrap">{msg.content}</p>
                </div>
              </div>
            ))}
            
            {isAiLoading && (
              <div className="flex justify-start">
                <div className="bg-white p-3 rounded-2xl rounded-bl-md shadow">
                  <Loader2 className="w-5 h-5 animate-spin text-purple-600" />
                </div>
              </div>
            )}
          </div>
          
          {/* Chat Input */}
          <div className="p-4 border-t bg-white">
            <div className="flex gap-2">
              <input
                type="text"
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                onKeyPress={(e) => e.key === 'Enter' && handleChatSubmit()}
                placeholder="Tapez votre message..."
                className="flex-grow px-4 py-2 border rounded-full focus:outline-none focus:ring-2 focus:ring-purple-500 text-sm"
              />
              <button
                onClick={handleChatSubmit}
                disabled={isAiLoading || !chatInput.trim()}
                className="p-2 bg-purple-600 text-white rounded-full hover:bg-purple-700 disabled:opacity-50 transition-all"
              >
                <Send className="w-5 h-5" />
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Floating Chat Button */}
      {!showAiChat && (
        <button
          onClick={() => setShowAiChat(true)}
          className="fixed bottom-6 right-6 w-14 h-14 bg-gradient-to-r from-purple-600 to-blue-600 text-white rounded-full shadow-lg hover:shadow-xl transition-all flex items-center justify-center z-50"
        >
          <MessageSquare className="w-6 h-6" />
        </button>
      )}
      
      {/* Footer */}
      <div className="bg-gray-800 text-white py-6">
        <div className="max-w-6xl mx-auto px-4">
          <div className="flex flex-wrap items-center justify-between gap-4">
            <div>
              <h4 className="font-semibold flex items-center gap-2">
                <Sparkles className="w-4 h-4 text-yellow-400" />
                {fosAgriKnowledgeBase.organization.name} - Recherche IA
              </h4>
              <p className="text-gray-400 text-sm">{fosAgriKnowledgeBase.organization.fullName}</p>
            </div>
            <div className="flex items-center gap-6 text-sm">
              <a href={`mailto:${fosAgriKnowledgeBase.organization.email}`} className="flex items-center gap-2 hover:text-emerald-400">
                <Mail className="w-4 h-4" />
                {fosAgriKnowledgeBase.organization.email}
              </a>
              <span className="flex items-center gap-2">
                <Phone className="w-4 h-4" />
                {fosAgriKnowledgeBase.organization.phone}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AISearchEngine;