package com.example.recrutement.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class OffreEmploi extends BaseEntity {


    private String titre;
    @Size(max = 10000)
    private String description;
    private String contrat;
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "categories", joinColumns = @JoinColumn(name = "offre_id"))
    @Column(name = "category" , nullable = false)
    private List<String> categories = new ArrayList<>();

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "exigences", joinColumns = @JoinColumn(name = "offre_id"))
    @Column(name = "exigence", nullable = false)
    private List<String> exigences = new ArrayList<>();

    private String niveauExperience;
    private String salaire;
    private String localisation;
    private Date dateDebut;
    private boolean archive;

    //@ManyToOne
    //@JoinColumn(name = "rh_id")
    // private User rh; // (RH)
    @OneToMany(mappedBy = "offreEmploi", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Candidature> candidatures;
//


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

    public String getContrat() {
        return contrat;
    }

    public void setContrat(String contrat) {
        this.contrat = contrat;
    }



    public List<String> getExigences() {
        return exigences;
    }

    public void setExigences(List<String> exigences) {
        this.exigences = exigences;
    }

    public String getNiveauExperience() {
        return niveauExperience;
    }

    public void setNiveauExperience(String niveauExperience) {
        this.niveauExperience = niveauExperience;
    }

    public String getSalaire() {
        return salaire;
    }

    public void setSalaire(String salaire) {
        this.salaire = salaire;
    }

    public String getLocalisation() {
        return localisation;
    }

    public void setLocalisation(String localisation) {
        this.localisation = localisation;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public List<Candidature> getCandidatures() {
        return candidatures;
    }

    public void setCandidatures(List<Candidature> candidatures) {
        this.candidatures = candidatures;
    }
}
