package com.example.recrutement.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Entretien extends BaseEntity {


    private Date dateEntretien;
    private String commentaireRH;
    public enum ResultatEntretien {
        ACCEPTE, REJETE, EN_ATTENTE
    }
    @Enumerated(EnumType.STRING)
    private ResultatEntretien resultat;

    @Column(length = 500)
    private String lien;


    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "candidature_id", foreignKey = @ForeignKey(name = "FK_ENTRETIEN_CANDIDATURE", foreignKeyDefinition = "FOREIGN KEY (candidature_id) REFERENCES candidature(id) ON DELETE CASCADE"))
    @JsonIgnore
    private Candidature candidature;


    //@ManyToOne
   // @JoinColumn(name = "rh_id")
    //private User menePar; // Led by RH

    public Date getDateEntretien() {
        return dateEntretien;
    }

    public void setDateEntretien(Date dateEntretien) {
        this.dateEntretien = dateEntretien;
    }

    public String getCommentaireRH() {
        return commentaireRH;
    }

    public void setCommentaireRH(String commentaireRH) {
        this.commentaireRH = commentaireRH;
    }

    public ResultatEntretien getResultat() {
        return resultat;
    }

    public void setResultat(ResultatEntretien resultat) {
        this.resultat = resultat;
    }

    public Candidature getCandidature() {
        return candidature;
    }

    public void setCandidature(Candidature candidature) {
        this.candidature = candidature;
    }

    public String getLien() {
        return lien;
    }

    public void setLien(String lien) {
        lien = lien;
    }
}
