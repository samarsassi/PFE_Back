package com.example.recrutement.entities;
import com.example.recrutement.controllers.CandidatureController;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
public class Candidature extends BaseEntity {
    private static final Logger log = LoggerFactory.getLogger(CandidatureController.class);

    private String nom;
    private String email;
    private String telephone;
    private String experience;
    private String linkedInProfile;
    private String portfolioURL ;
    private String statut; // EN ATTENTE, ACCEPTÉ, REFUSEE
    @Lob
    private String cv;
    private String cvUrl;

    @Lob
    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    private Double scoreCV;
    @Column(length = 10000)
    private String scoringComment;

    private String remarquesRH;
    private String decisionFinale; //  EMBAUCHE , REFUSEE   manaher part

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "offre_id")
    @JsonIgnoreProperties("candidatures") // instead of @JsonBackReference
    private OffreEmploi offreEmploi;

    //challenge

    @ManyToOne
    @JoinColumn(name = "defi_id")
    @JsonIgnoreProperties("candidatures")
    private Challenge defi;

    @OneToOne(mappedBy = "candidature", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private SoumissionDefi soumissionDefi;

    private LocalDateTime defiEnvoyeLe;
    private LocalDateTime defiTermineLe;
    private Double scoreDefi;

    @Enumerated(EnumType.STRING)
    private StatutDefi statutDefi;
    public enum StatutDefi{
        AUCUN, ENVOYE, TERMINE, EVALUE, EXPIRE
    }

    @OneToOne(mappedBy = "candidature", cascade = CascadeType.ALL, orphanRemoval = true, optional = true)
    @JsonManagedReference
    private Entretien entretien;

    @Enumerated(EnumType.STRING)
    private StatutEnt statutEntretien;
    public enum StatutEnt {
        AUCUN, ENVOYE, TERMINE, NON_CONCLUANT
    }

    private String ProcessInstanceId;

    /**
     * Reset candidature for a full reanalysis.
     * Clears workflow, scores, and challenge/interview statuses.
     */
    public void resetForReanalysis() {
        // Reset general status
        this.statut = "EN ATTENTE";

        // Reset challenge status
        this.statutDefi = StatutDefi.AUCUN;
        this.defiEnvoyeLe = null;
        this.defiTermineLe = null;
        this.scoreDefi = null;

        // Reset SoumissionDefi if exists
        if (this.soumissionDefi != null) {
            this.soumissionDefi.setScore(0);
            this.soumissionDefi.setPointsTotal(0);
            this.soumissionDefi.setCode(null);
            this.soumissionDefi.setResultatsExecution(null);
            this.soumissionDefi.setSoumisLe(null);
            this.soumissionDefi.setStatut(SoumissionDefi.StatutSoumission.Aucun);
        }

        // Reset interview status
        this.statutEntretien = StatutEnt.AUCUN;
        if (this.entretien != null) {
            this.entretien.setDateEntretien(null);
            this.entretien.setCommentaireRH(null);
            this.entretien.setResultat(null);
        }

        // Reset CV scoring
        this.scoreCV = null;
        this.scoringComment = null;

        // Reset HR remarks and final decision
        this.remarquesRH = null;
        this.decisionFinale = null;

        log.info("Candidature {} reset for full reanalysis", this.getId());
    }

    public String getProcessInstanceId() {
        return ProcessInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        ProcessInstanceId = processInstanceId;
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

    public Double getScoreCV() {
        return scoreCV;
    }

    public void setScoreCV(Double scoreCV) {
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

    public SoumissionDefi getSoumissionDefi() {
        return soumissionDefi;
    }

    public void setSoumissionDefi(SoumissionDefi soumissionDefi) {
        this.soumissionDefi = soumissionDefi;
    }

    public Entretien getEntretien() {
        return entretien;
    }

    public void setEntretien(Entretien entretien) {
        this.entretien = entretien;
    }

    public String getCvUrl() {
        return cvUrl;
    }

    public void setCvUrl(String cvUrl) {
        this.cvUrl = cvUrl;
    }

    public StatutEnt getStatutEntretien() {
        return statutEntretien;
    }

    public void setStatutEntretien(StatutEnt statutEntretien) {
        this.statutEntretien = statutEntretien;
    }

    public String getScoringComment() {
        return scoringComment;
    }

    public void setScoringComment(String scoringComment) {
        this.scoringComment = scoringComment;
    }
}
