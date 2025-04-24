package com.example.recrutement.services;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.OffreEmploi;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface IOffreEmploiService {
    public OffreEmploi createOffreEmploi(OffreEmploi offreEmploi, Authentication connectedUser);

    public List<OffreEmploi> getAllOffresEmploi();

    public Optional<OffreEmploi> getOffreEmploiById(Integer id);

    public OffreEmploi updateOffreEmploi(Integer id, OffreEmploi updatedOffreEmploi);

    public void deleteOffreEmploi(Integer id);

    public OffreEmploi archiveOffreEmploi(Integer id);

    public OffreEmploi unarchiveOffreEmploi(Integer id);

    public List<Candidature> getCandidaturesForOffreEmploi(Integer id);

}
