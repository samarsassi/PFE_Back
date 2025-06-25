package com.example.recrutement.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TestCase extends BaseEntity {

    @Lob
    private String entree;

    @Lob
    private String sortieAttendue;

    private boolean estCache;

    private int points;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    @JsonIgnoreProperties("testCases")
    private Challenge challenge;


    public String getEntree() {
        return entree;
    }

    public void setEntree(String entree) {
        this.entree = entree;
    }

    public String getSortieAttendue() {
        return sortieAttendue;
    }

    public void setSortieAttendue(String sortieAttendue) {
        this.sortieAttendue = sortieAttendue;
    }

    public boolean isEstCache() {
        return estCache;
    }

    public void setEstCache(boolean estCache) {
        this.estCache = estCache;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }
}
