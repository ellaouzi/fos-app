package com.fosagri.application.model;



import lombok.Data;

import jakarta.persistence.*;
import java.util.Date;
import java.util.List;

@Data
@Entity
public class Utilisateur {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	@Column(unique = true)
	private String username;

	private String password;

	@Transient
	private String password2;

	//@Column(unique = true)
	private String email;

	private String fullname;

	private String adhid;

	@Column(unique = true)
	private String cin;

	//@Column(unique = true)
	private String pension;

	@Column(unique = true)
	private String ppr;

	private String nom;

	private String prenom;

	private boolean enabled;

	private Date created;

	private Date updated;

	private String isadh;

// cod_ag	NOM_AG	PR_AG	CIN_AG	ID_ADH	IS_ADH	grade	Direction	ORG
	private String createdBy;

	private String updatedBy;

	@OneToMany(fetch = FetchType.LAZY,mappedBy="utilisateur", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Authority> authorities;

	public Utilisateur(){

	}
	public Utilisateur( String username, String password, String nom, String prenom, boolean enabled, Date updated,
						String ppr, String idadh, String cin, String isadh) {
 		this.username = username;
		this.password = password;
		this.nom = nom;
		this.prenom = prenom;
		this.enabled = enabled;
		this.updated = updated;
		this.ppr = ppr;
		this.adhid = idadh;
		this.isadh = isadh;
		this.cin = cin;
	}
}//end Role