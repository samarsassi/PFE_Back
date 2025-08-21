package com.example.recrutement.entities.dto;

import com.example.recrutement.entities.Entretien;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.util.Date;

public class EntretienDTO {
    private Integer id;
    private Date dateEntretien;
    private String commentaireRH;
    @Enumerated(EnumType.STRING)
    private ResultatEntretien resultat;
    private String lien;
    private Integer candidatureId;  // only ID here
    public enum ResultatEntretien {
        ACCEPTE, REFUSEE, EN_ATTENTE
    }

    public EntretienDTO(Entretien entretien) {
        this.id = entretien.getId();
        this.dateEntretien = entretien.getDateEntretien();
        this.commentaireRH = entretien.getCommentaireRH();
        this.resultat = EntretienDTO.ResultatEntretien.valueOf(entretien.getResultat().name());
        this.lien = entretien.getLien();
        this.candidatureId = entretien.getCandidature() != null ? entretien.getCandidature().getId() : null;
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getLien() {
        return lien;
    }

    public void setLien(String lien) {
        this.lien = lien;
    }

    public Integer getCandidatureId() {
        return candidatureId;
    }

    public void setCandidatureId(Integer candidatureId) {
        this.candidatureId = candidatureId;
    }
}

