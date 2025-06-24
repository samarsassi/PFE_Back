package com.example.recrutement.entities;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
public class Candidature extends BaseEntity {

    private String nom;
    private String email;
    private String telephone;
    private String experience;
    private String linkedInProfile;
    private String portfolioURL ;
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

    //challenge

    @ManyToOne
    @JoinColumn(name = "defi_id")
    @JsonIgnoreProperties("candidatures")
    private Challenge defi;

    private LocalDateTime defiEnvoyeLe;
    private LocalDateTime defiTermineLe;
    private Double scoreDefi;


    private StatutDefi statutDefi;
    public enum StatutDefi {
        AUCUN, ENVOYE, TERMINE, EVALUE
    }


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

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getLinkedInProfile() {
        return linkedInProfile;
    }

    public void setLinkedInProfile(String linkedInProfile) {
        this.linkedInProfile = linkedInProfile;
    }

    public String getPortfolioURL() {
        return portfolioURL;
    }

    public void setPortfolioURL(String portfolioURL) {
        this.portfolioURL = portfolioURL;
    }

    public Challenge getDefi() {
        return defi;
    }

    public void setDefi(Challenge defi) {
        this.defi = defi;
    }

    public LocalDateTime getDefiEnvoyeLe() {
        return defiEnvoyeLe;
    }

    public void setDefiEnvoyeLe(LocalDateTime defiEnvoyeLe) {
        this.defiEnvoyeLe = defiEnvoyeLe;
    }

    public LocalDateTime getDefiTermineLe() {
        return defiTermineLe;
    }

    public void setDefiTermineLe(LocalDateTime defiTermineLe) {
        this.defiTermineLe = defiTermineLe;
    }

    public Double getScoreDefi() {
        return scoreDefi;
    }

    public void setScoreDefi(Double scoreDefi) {
        this.scoreDefi = scoreDefi;
    }

    public StatutDefi getStatutDefi() {
        return statutDefi;
    }

    public void setStatutDefi(StatutDefi statutDefi) {
        this.statutDefi = statutDefi;
    }
}

