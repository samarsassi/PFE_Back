package com.example.recrutement.entities;
import jakarta.persistence.*;
import lombok.*;

import java.io.File;
import java.util.Date;

@Getter
@Setter
@Entity
public class Candidature extends BaseEntity {

    private String statut; // EN ATTENTE, ACCEPTÉ, REJETÉ
    private Date dateCandidature;

    private File cv;

    private int scoreCV;
    private String remarquesRH;
    private String decisionFinale;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private OffreEmploi offreEmploi;
}
