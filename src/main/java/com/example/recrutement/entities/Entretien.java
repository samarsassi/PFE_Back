package com.example.recrutement.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Entretien implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date dateEntretien;
    private String commentaireRH;
    private String resultat; // ACCEPTÉ, REJETÉ

    @OneToOne
    @JoinColumn(name = "candidature_id")
    private Candidature candidature;

    //@ManyToOne
   // @JoinColumn(name = "rh_id")
    //private User menePar; // Led by RH
}
