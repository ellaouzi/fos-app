package com.fosagri.application.entities;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;
import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nom; // Contact name or department name

    @Column(length = 200)
    private String fonction; // Function/Role

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeContact type;

    @Column(length = 100)
    private String telephone;

    @Column(length = 100)
    private String telephoneMobile;

    @Column(length = 100)
    private String fax;

    @Column(length = 200)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String adresse;

    @Column(length = 100)
    private String ville;

    @Column(length = 100)
    private String region;

    @Column(length = 20)
    private String codePostal;

    @Column(length = 200)
    private String siteWeb;

    @Column(columnDefinition = "TEXT")
    private String horaires; // Opening hours

    @Column(columnDefinition = "TEXT")
    private String description;

    private boolean actif = true;

    private Integer ordre; // Display order

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
        if (actif == false) {
            actif = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updated = new Date();
    }

    // Contact types
    public enum TypeContact {
        SIEGE("Siège Social"),
        DIRECTION_REGIONALE("Direction Régionale"),
        ANTENNE("Antenne"),
        PARTENAIRE_MEDICAL("Partenaire Médical"),
        PARTENAIRE_BANCAIRE("Partenaire Bancaire"),
        PARTENAIRE_ASSURANCE("Partenaire Assurance"),
        PARTENAIRE_COMMERCIAL("Partenaire Commercial"),
        SERVICE("Service"),
        URGENCE("Urgence"),
        AUTRE("Autre");

        private final String label;

        TypeContact(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(id, contact.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
