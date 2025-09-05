package com.fosagri.application.model;

import lombok.Data;

import java.util.Date;

@Data
public class Conjoint {
    public Integer adhconjointid;
    public String codAg;
     public String c_USER;
     public String pr_CONJ;
     public String nom_CONJ;
    public String email;
    public boolean valide;
    public String sit_CJ;
    public String sex_CONJ;
    public String cin_CONJ;
    public String nom_CONJ_A;
    public String pr_CONJ_A;
    public String nom_PAC_A;
    public String pr_PAC_A;
    public String nationalite;
    public String tele;
    public String adrs_POSTALE;
    public Date dat_N_CONJ;
    public Date updated;
    public int cnjt_ORDRE;





    public Conjoint() {
    }


    @Override
    public String toString() {
        return String.format("%s, %s ", email, codAg);
    }
}