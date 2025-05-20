package com.example.recrutement.entities;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.File;


@Getter
@Setter
@Entity
public class Candidature extends BaseEntity {

    private String nom;
    private String email;
    private String telephone;
    private String statut; // EN ATTENTE, ACCEPTÉ, REJETÉ
    @Lob
    private String cv;

    @Lob
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    private int scoreCV;
    private String remarquesRH;
    private String decisionFinale;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offre_id")
    @JsonIgnoreProperties("candidatures") // instead of @JsonBackReference
    private OffreEmploi offreEmploi;

    //
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

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getCv() {
        return cv;
    }

    public void setCv(String cv) {
        this.cv = cv;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
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

