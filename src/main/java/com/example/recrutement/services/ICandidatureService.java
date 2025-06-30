package com.example.recrutement.services;

import com.example.recrutement.entities.Candidature;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface ICandidatureService {
    public Candidature createCandidature(Candidature offreEmploi, Authentication connectedUser);

    public List<Candidature> getAllOffresEmploi();

    public Optional<Candidature> getCandidatureById(Integer id);

    public Candidature updateCandidature(Integer id, Candidature updatedCandidature);

    public void deleteCandidature(Integer id);

    public Candidature archiveCandidature(Integer id);

    public Candidature unarchiveCandidature(Integer id);

    public List<Candidature> getCandidaturesForCandidature(Integer id);
}
