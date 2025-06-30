package com.example.recrutement.services;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.OffreEmploiRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CandidatureService {

    private final CandidatureRepo candidatureRepository;
    private final OffreEmploiRepo offreEmploiRepository;

    @Autowired
    public CandidatureService(CandidatureRepo candidatureRepository, OffreEmploiRepo offreEmploiRepository) {
        this.candidatureRepository = candidatureRepository;
        this.offreEmploiRepository = offreEmploiRepository;
    }

    // Create a new Candidature (Application)
    @Transactional
    public Candidature createCandidature(Candidature candidature, Integer offreEmploiId, Authentication connectedUser) {
        Optional<OffreEmploi> offreEmploi = offreEmploiRepository.findById(offreEmploiId);
        if (offreEmploi.isPresent()) {
            candidature.setOffreEmploi(offreEmploi.get());
            return candidatureRepository.save(candidature);
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + offreEmploiId);
        }
    }

    // Get all candidatures
    public List<Candidature> getAllCandidatures() {
        return candidatureRepository.findAllCandidature();
    }

    // Get a candidature by id
    public Optional<Candidature> getCandidatureById(Integer id) {
        return candidatureRepository.findById(id);
    }

    // Update an existing candidature
    @Transactional
    public Candidature updateCandidature(Integer id, Candidature updatedCandidature) {
        Optional<Candidature> existingCandidature = candidatureRepository.findById(id);

        if (existingCandidature.isPresent()) {
            Candidature candidature = existingCandidature.get();
            // Update properties of the candidature
            candidature.setStatut(updatedCandidature.getStatut());
            candidature.setCv(updatedCandidature.getCv());
            candidature.setScoreCV(updatedCandidature.getScoreCV());
            candidature.setRemarquesRH(updatedCandidature.getRemarquesRH());
            candidature.setDecisionFinale(updatedCandidature.getDecisionFinale());
            candidature.setCoverLetter(updatedCandidature.getCoverLetter());
            // Ensure the offer is not changed (it's linked already)
            return candidatureRepository.save(candidature);
        } else {
            throw new RuntimeException("Candidature not found with id: " + id);
        }
    }

    // Delete a candidature by id
    @Transactional
    public void deleteCandidature(Integer id) {
        Optional<Candidature> existingCandidature = candidatureRepository.findById(id);

        if (existingCandidature.isPresent()) {
            candidatureRepository.deleteById(id);
        } else {
            throw new RuntimeException("Candidature not found with id: " + id);
        }
    }

    // Get all candidatures for a specific OffreEmploi (Job Offer)
    public List<Candidature> getCandidaturesForOffreEmploi(Integer offreEmploiId) {
        Optional<OffreEmploi> offreEmploi = offreEmploiRepository.findById(offreEmploiId);
        if (offreEmploi.isPresent()) {
            return offreEmploi.get().getCandidatures();
        } else {
            throw new RuntimeException("OffreEmploi not found with id: " + offreEmploiId);
        }
    }
}
