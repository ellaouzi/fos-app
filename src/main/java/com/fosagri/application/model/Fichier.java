package com.fosagri.application.model;

import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "document")
public class Fichier {
    private static final long serialVersionUID = 267310711517424273L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    public String cod_ag;
    public String idadh;
    public Date created;
    public String fileName;
    public String designation;
    public String extention;
    public String document;
}