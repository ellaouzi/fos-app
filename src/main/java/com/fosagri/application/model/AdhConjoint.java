package com.fosagri.application.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "adhconjoint")
public class AdhConjoint implements Serializable {
	private static final long serialVersionUID = 267310711517424273L;
	@Id
	  @GeneratedValue(strategy = GenerationType.AUTO)
	// @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "adhconjointid")

	private int adhConjointId;
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
	private String NUM_CONJ;
	private String PR_CONJ;
	private String NOM_CONJ;
	private String NOMP_CONJ_A;
	private String CIN_CONJ;
	private Date dat_N_CONJ;
	private int cnjt_ordre;
	private Date dat_MAR;
	private String sit_CJ;
	private Date dat_SIT_CJ;
	private String doti_CONJ;
	private Date updated;
	private String sex_CONJ;
	private String c_USER;
	private String observation;
 	private String email;
	private String civilite;
	private String nom_CONJ_A;
	private String pr_CONJ_A;
 	private String adrs_POSTALE;
	private String ville;
  	private String tele;
 	private String fonctionnaire;
    private boolean valide;
    @Transient
	private int currentId;
	
	// Photo and document fields
	@Lob
	private byte[] conjoint_photo;
	private String conjoint_photo_filename;
	private String conjoint_photo_contentType;
	
	@Lob
	private byte[] cin_image; // For conjoints > 18 years
	private String cin_image_filename;
	private String cin_image_contentType;
	
	@Lob
	private byte[] acte_mariage_photo;
	private String acte_mariage_photo_filename;
	private String acte_mariage_photo_contentType;

	/*private Date DAT_MAR;
	private String SIT_CJ;
	private Date DAT_SIT_CJ;
	private String COD_PRF;
	private String COD_DEP;
	private String DOTI_CONJ;
	private String TEL_CONJ;
	private String MAIL_CONJ;
	private Date UPDATED;
	private String SEX_CONJ;
	private String C_USER;
	private String OBSERVATION;
	private String IDCONJOINT;
	private String EMAIL;
	private String CIVILITE;
	private String TYPEADHERENT;
	private String TYPEIDENTITE;
	private int CNJT_ORDRE;
	private String NOM_PAC_A;
	private String PR_PAC_A;
	private String NATIONALITE;
	private String TYPEIDT;
	private String TYPADH;
	private String ADRS_POSTALE;
	private String VILLE;
	private String CODE_POST;
	private String PAYE;
	private String TELE;
*/


}
