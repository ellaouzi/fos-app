package com.fosagri.application.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by fekra on 12/01/2017.
 */
@Data
@Entity
@Table(name = "adhenfant")

public class AdhEnfant implements Serializable {

 	private static final long serialVersionUID = 6469017838580397275L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	 // @GeneratedValue(strategy=GenerationType.IDENTITY)
 	@Column(name = "adhenfantid")
	private Integer adhEnfantId;

	// ==============================
	// Many Enfant to One Agent
	// ==============================

	@ManyToOne
	@JoinColumn(name = "adhagentid")
	@JsonIgnore
	@JsonBackReference
	private AdhAgent adhAgent;

	@Column(name = "cod_ag")
	private String codAg;
	private String nom_pac;
	private String pr_pac;
    private Date dat_n_pac;
	private String sex_pac;
	private int enft_ordre;
    @Transient
	MultipartFile photo;
	private String nom_PAC_A;
	private String pr_PAC_A;
	// -------------------------
	private Date updated;
	private Date created;

	private String niv_INSTRUCTION;
	private String COD_LIEN_PAR;
	private String infirmite;
 	private String DER_CERTIF;
	private String c_USER;
 	private String etatSante;
	private String email;
	private String tele;
	private String lien_PAR;
	private String cin_PAC;
	private boolean valide;
	public String num_CONJ;
	public String nationalite;

	public String adrs_postale;
	
	// Photo and document fields
	@Lob
	private byte[] enfant_photo;
	private String enfant_photo_filename;
	private String enfant_photo_contentType;
	
	@Lob
	private byte[] cin_image; // For enfants > 18 years
	private String cin_image_filename;
	private String cin_image_contentType;
	
	@Lob
	private byte[] attestation_scolarite_photo;
	private String attestation_scolarite_photo_filename;
	private String attestation_scolarite_photo_contentType;



}

