package com.fosagri.application.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

import jakarta.persistence.*;

@Data
@Entity

public class Authority {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String username;

    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    private Utilisateur utilisateur;

    public Authority() {

    }

    public Authority(String username, String role, Utilisateur utilisateur) {
        this.username = username;
        this.role = role;
        this.utilisateur = utilisateur;
    }    public Authority(Long id, String username, String role, Utilisateur utilisateur) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.utilisateur = utilisateur;
    }

}//end Role