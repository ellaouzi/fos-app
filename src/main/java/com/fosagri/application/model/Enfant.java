package com.fosagri.application.model;

import lombok.Data;

import java.util.Date;

@Data
public class Enfant {
    public String codAg;
    public String nom_PAC;
    public String pr_PAC;
    public String adrs_POSTALE;
    public String tele;
    public boolean valide;
    public String num_CONJ;
    public String c_USER;
    public String email;
    public String nom_PAC_A;
    public String pr_PAC_A;
    public String nationalite;
    public String sex_PAC;
    public Date dat_N_PAC;
    public String cin_PAC;
    public String niv_INSTRUCTION;
    public int enft_ORDRE;



    public Enfant() {
    }


    @Override
    public String toString() {
        return String.format("%s, %s ", nom_PAC, pr_PAC);
    }
}