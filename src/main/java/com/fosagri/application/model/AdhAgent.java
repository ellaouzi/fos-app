package com.fosagri.application.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by fekra on 12/01/2017.
 */
@Entity
@Data
@Table(name = "adhagent")
public class AdhAgent implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 4328645878271674354L;
	@Id
	 @GeneratedValue(strategy = GenerationType.AUTO)
	// @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "adhagentid")
	private int adhAgentId;

	@OneToMany(mappedBy = "adhAgent", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<AdhConjoint> adhConjoints;

	@OneToMany(mappedBy = "adhAgent", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<AdhEnfant> adhEnfants;

	// -------------------------
	private Date updated;

	@Column(unique=true, name = "id_ADH")
		private String idAdh;
	private String IS_ADH;
	@Column(name = "cod_ag")
	private String codAg;
	private String NOM_AG;
	private String PR_AG;
	private String CIN_AG;
	private String sex_AG;
	private Date Naissance;
	private String mail;
	private String num_Tel;
	private String ville;
	private String code_POSTE;
	private String adresse;
	private String situation_familiale;
	
	// Photo and document fields
	@Lob
	private byte[] agent_photo;
	private String agent_photo_filename;
	private String agent_photo_contentType;
	
	@Lob
	private byte[] cin_image;
	private String cin_image_filename;
	private String cin_image_contentType;
	
	@Lob
	private byte[] rib;
	private String rib_filename;
	private String rib_contentType;
	
	@Lob
	private byte[] rib_photo;
	private String rib_photo_filename;
	private String rib_photo_contentType;

	/*private String ADRESSE;
	private String Situation_familiale;
	private String Grade;
	private String CodeGrade;
	private Date Date_effet_du_grade;
	private Date Anciennete_Administrative;
	private Date Anciennete_Grade;
	private Date Date_de_recrutement;
	private String Echelle;
	private String COD_ECH_A;
	private String Echelon;
	private Date Date_effet_Echelon;
	private Date Anciennete_Echelon;
	private String Mode_acces_grade;
	private String Situation_statutaire;
	private Date Date_de_statut;
	private String Fonction;
	private Date Date_Fonction;
	private String COD_Serv;
	private String Service;
	private String COD_Div;
	private String Division;
	private String Cod_Direc;
	private String Direction;
	private Date Date_affectation;
	private String LIB_LOC;
	private String COD_LOC;
	private String LIB_PROVINCE;
	private String COD_PROVINCE;
	private String Position;
	private Date Date_mouvement_Position;
	private String LIB_TYP_MVT;
	private String COD_ORG;
	private String LIB_ORG;
	private String MAIL;
	private String NUM_Tel;
	private String VILLE;
	private String CODE_POSTE;
	private String nouvelleAffectation;
	private String choix;*/
}
