package com.fosagri.application.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeEvent type;

    @Enumerated(EnumType.STRING)
    private CategorieEvent categorie;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    private LocalTime heureDebut;

    private LocalTime heureFin;

    private boolean journeeEntiere = false;

    @Column(length = 300)
    private String lieu;

    @Column(length = 100)
    private String organisateur;

    @Column(length = 500)
    private String lienInscription;

    @Column(length = 7)
    private String couleur = "#3b6b35"; // Default FOS-Agri green

    private Integer nombrePlaces;

    private Integer nombreInscrits = 0;

    private boolean actif = true;

    private boolean publique = true; // Visible to all users

    // Audit fields
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    private Date updated;

    private Long createdBy;
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        created = new Date();
        if (dateFin == null) {
            dateFin = dateDebut;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }

    // Event types
    public enum TypeEvent {
        REUNION("Réunion"),
        FORMATION("Formation"),
        CONFERENCE("Conférence"),
        SEMINAIRE("Séminaire"),
        ATELIER("Atelier"),
        VOYAGE("Voyage"),
        EXCURSION("Excursion"),
        CELEBRATION("Célébration"),
        DEADLINE("Date limite"),
        FERMETURE("Fermeture"),
        AUTRE("Autre");

        private final String label;

        TypeEvent(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // Event categories
    public enum CategorieEvent {
        PRESTATIONS("Prestations"),
        EDUCATION("Éducation"),
        LOISIRS("Loisirs & Voyages"),
        CLUB("Club Agriculture"),
        ADMINISTRATIF("Administratif"),
        SOCIAL("Social"),
        SANTE("Santé"),
        AUTRE("Autre");

        private final String label;

        CategorieEvent(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    // Helper methods
    public boolean isMultiDay() {
        return dateDebut != null && dateFin != null && !dateDebut.equals(dateFin);
    }

    public boolean isPast() {
        return dateFin != null && dateFin.isBefore(LocalDate.now());
    }

    public boolean isToday() {
        LocalDate today = LocalDate.now();
        return dateDebut != null && dateFin != null &&
               !today.isBefore(dateDebut) && !today.isAfter(dateFin);
    }

    public boolean isFuture() {
        return dateDebut != null && dateDebut.isAfter(LocalDate.now());
    }

    public boolean hasAvailablePlaces() {
        return nombrePlaces == null || nombrePlaces <= 0 || nombreInscrits < nombrePlaces;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(id, event.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
