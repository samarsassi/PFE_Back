package com.example.recrutement.services;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.repositories.OffreEmploiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OffreEmploiService implements IOffreEmploiService{

    private final OffreEmploiRepo offreEmploiRepository;

    @Autowired
    public OffreEmploiService(OffreEmploiRepo offreEmploiRepository) {
        this.offreEmploiRepository = offreEmploiRepository;
    }

    // Create a new OffreEmploi
    @Transactional
    public OffreEmploi createOffreEmploi(OffreEmploi offreEmploi, Authentication connectedUser) {
       // offreEmploi.setCreePar(connectedUser.getName());
        return offreEmploiRepository.save(offreEmploi);
    }
    // Get all offres d'emploi
    public List<OffreEmploi> getAllOffresEmploi() {
        return offreEmploiRepository.findAllOffres();
    }

    // Get an offre emploi by id
    public Optional<OffreEmploi> getOffreEmploiById(Integer id) {
        return offreEmploiRepository.findById(id);
    }

    // Update an existing offre emploi
    @Transactional
    public OffreEmploi updateOffreEmploi(Integer id, OffreEmploi updatedOffreEmploi) {
        Optional<OffreEmploi> existingOffreEmploi = offreEmploiRepository.findById(id);

        if (existingOffreEmploi.isPresent()) {
            OffreEmploi offreEmploi = existingOffreEmploi.get();
            // Update properties
            offreEmploi.setTitre(updatedOffreEmploi.getTitre());
            offreEmploi.setDescription(updatedOffreEmploi.getDescription());
            offreEmploi.setDateCreation(updatedOffreEmploi.getDateCreation());
            offreEmploi.setArchive(updatedOffreEmploi.isArchive());
            offreEmploi.setLocalisation(updatedOffreEmploi.getLocalisation());
            offreEmploi.setDateDebut(updatedOffreEmploi.getDateDebut());

            // If you need to update the candidatures, you can handle that as well (cascade type is set)
           // offreEmploi.setCandidatures(updatedOffreEmploi.getCandidatures());

            return offreEmploiRepository.save(offreEmploi);
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + id);
        }
    }

    // Delete an offre emploi by id
    @Transactional
    public void deleteOffreEmploi(Integer id) {
        Optional<OffreEmploi> existingOffreEmploi = offreEmploiRepository.findById(id);

        if (existingOffreEmploi.isPresent()) {
            offreEmploiRepository.deleteById(id);
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + id);
        }
    }

    // Archive an offre emploi (set archive flag to true)
    @Transactional
    public OffreEmploi archiveOffreEmploi(Integer id) {
        Optional<OffreEmploi> existingOffreEmploi = offreEmploiRepository.findById(id);

        if (existingOffreEmploi.isPresent()) {
            OffreEmploi offreEmploi = existingOffreEmploi.get();
            offreEmploi.setArchive(true);
            return offreEmploiRepository.save(offreEmploi);
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + id);
        }
    }

    // Unarchive an offre emploi (set archive flag to false)
    @Transactional
    public OffreEmploi unarchiveOffreEmploi(Integer id) {
        Optional<OffreEmploi> existingOffreEmploi = offreEmploiRepository.findById(id);

        if (existingOffreEmploi.isPresent()) {
            OffreEmploi offreEmploi = existingOffreEmploi.get();
            offreEmploi.setArchive(false);
            return offreEmploiRepository.save(offreEmploi);
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + id);
        }
    }

    // Get all candidatures related to a specific offre emploi
    public List<Candidature> getCandidaturesForOffreEmploi(Integer id) {
        Optional<OffreEmploi> offreEmploi = offreEmploiRepository.findById(id);
        if (offreEmploi.isPresent()) {
            return offreEmploi.get().getCandidatures();
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + id);
        }
    }
}
