package com.fosagri.application.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "prestation_ref_id", "colonne" }) })

public class PrestationField {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String label;
    private boolean active;
    private String colonne;
    private String vlauetype;
    private int ordre;
    private String fieldtype;
    @Length(max = 600)
    private String valeurs;
    private String inputvalue;
    private String condition;
    private boolean required;
    @Transient
    private Long prestation_ref_id;
    @Transient
    private Long prestat_ref_id;

    public PrestationField() {
    }

    public PrestationField(boolean active, String colonne, String vlauetype, String label, boolean required) {
        this.colonne = colonne;
        this.active = active;
        this.label = label;
        this.vlauetype = vlauetype;
        this.required = required;
    }

    @ManyToOne
    @JsonIgnore
    PrestationRef prestationRef;


    //------------------------- Secure Logs---
    private Date created;
    private Date updated;
    private Long createdBy;
    private Long updatedBy;
    //------------------------- Secure Logs---
}