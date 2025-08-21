package com.example.recrutement.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class SoumissionDefi extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    @JsonIgnoreProperties("soumissions")
    private Challenge challenge;

    @OneToOne
    @JoinColumn(name = "candidature_id", unique = true)
    @JsonBackReference
    private Candidature candidature;




    @Lob
    private String code;

    private String langage;

    private LocalDateTime soumisLe;

    @Lob
    private String resultatsExecution;

    private double score;
    private int pointsTotal;

    @Enumerated(EnumType.STRING)
    private StatutSoumission statut;

    public enum StatutSoumission {
        Aucun, Soumis, En_evaluation, Termine
    }


    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public Candidature getCandidature() {
        return candidature;
    }

    public void setCandidature(Candidature candidature) {
        this.candidature = candidature;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLangage() {
        return langage;
    }

    public void setLangage(String langage) {
        this.langage = langage;
    }

    public LocalDateTime getSoumisLe() {
        return soumisLe;
    }

    public void setSoumisLe(LocalDateTime soumisLe) {
        this.soumisLe = soumisLe;
    }

    public String getResultatsExecution() {
        return resultatsExecution;
    }

    public void setResultatsExecution(String resultatsExecution) {
        this.resultatsExecution = resultatsExecution;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getPointsTotal() {
        return pointsTotal;
    }

    public void setPointsTotal(int pointsTotal) {
        this.pointsTotal = pointsTotal;
    }

    public StatutSoumission getStatut() {
        return statut;
    }

    public void setStatut(StatutSoumission statut) {
        this.statut = statut;
    }
}