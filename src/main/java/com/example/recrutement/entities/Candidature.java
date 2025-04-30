package com.example.recrutement.entities;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.io.File;


@Getter
@Setter
@Entity
public class Candidature extends BaseEntity {

    private String nom;
    private String email;
    private String statut; // EN ATTENTE, ACCEPTÉ, REJETÉ
    private File cv;
    private int scoreCV;
    private String remarquesRH;
    private String decisionFinale;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offre_id")
    @JsonManagedReference //endless loop
    private OffreEmploi offreEmploi;

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public File getCv() {
        return cv;
    }

    public void setCv(File cv) {
        this.cv = cv;
    }

    public int getScoreCV() {
        return scoreCV;
    }

    public void setScoreCV(int scoreCV) {
        this.scoreCV = scoreCV;
    }

    public String getRemarquesRH() {
        return remarquesRH;
    }

    public void setRemarquesRH(String remarquesRH) {
        this.remarquesRH = remarquesRH;
    }

    public String getDecisionFinale() {
        return decisionFinale;
    }

    public void setDecisionFinale(String decisionFinale) {
        this.decisionFinale = decisionFinale;
    }

    public OffreEmploi getOffreEmploi() {
        return offreEmploi;
    }

    public void setOffreEmploi(OffreEmploi offreEmploi) {
        this.offreEmploi = offreEmploi;
    }
}

