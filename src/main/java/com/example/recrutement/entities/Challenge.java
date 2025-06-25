package com.example.recrutement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Challenge extends BaseEntity {

    private String titre;
    private String description;

    private Integer languageId;
    private String languageName;

    @Enumerated(EnumType.STRING)
    private Difficulte difficulte;

    private Integer tempslimite;     // en minutes
    private Integer memoirelimite;   // en KB

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.EAGER)
    @JsonIgnoreProperties("challenge")
    private List<TestCase> testCases;

    @Lob
    private String codeDepart;

    @Enumerated(EnumType.STRING)
    private Statut statut;

    // Getters and Setters

    public enum Difficulte {
        Facile, Moyen, Difficile
    }

    public enum Statut {
        Brouillon, Actif, Archive
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getLanguageId() {
        return languageId;
    }

    public void setLanguageId(Integer languageId) {
        this.languageId = languageId;
    }

    public String getLanguageName() {
        return languageName;
    }

    public void setLanguageName(String languageName) {
        this.languageName = languageName;
    }

    public Difficulte getDifficulte() {
        return difficulte;
    }

    public void setDifficulte(Difficulte difficulte) {
        this.difficulte = difficulte;
    }

    public Integer getTempslimite() {
        return tempslimite;
    }

    public void setTempslimite(Integer tempslimite) {
        this.tempslimite = tempslimite;
    }

    public Integer getMemoirelimite() {
        return memoirelimite;
    }

    public void setMemoirelimite(Integer memoirelimite) {
        this.memoirelimite = memoirelimite;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public String getCodeDepart() {
        return codeDepart;
    }

    public void setCodeDepart(String codeDepart) {
        this.codeDepart = codeDepart;
    }

    public Statut getStatut() {
        return statut;
    }

    public void setStatut(Statut statut) {
        this.statut = statut;
    }
}
