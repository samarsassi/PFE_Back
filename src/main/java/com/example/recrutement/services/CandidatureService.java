package com.example.recrutement.services;

import com.example.recrutement.controllers.CandidatureController;
import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.OffreEmploiRepo;
import com.example.recrutement.repositories.SoumissionDefiRepo;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class CandidatureService {
    private static final Logger log = LoggerFactory.getLogger(CandidatureController.class);
    private final CandidatureRepo candidatureRepository;
    private final OffreEmploiRepo offreEmploiRepository;
    private final SoumissionDefiRepo soumissionDefiRepo;

    @Autowired
    public CandidatureService(CandidatureRepo candidatureRepository,
                              OffreEmploiRepo offreEmploiRepository,
                              SoumissionDefiRepo soumissionDefiRepo) {
        this.candidatureRepository = candidatureRepository;
        this.offreEmploiRepository = offreEmploiRepository;
        this.soumissionDefiRepo = soumissionDefiRepo;
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

    public Page<Candidature> getAllCandidatures(Pageable pageable) {

        Page<Candidature> statusdefi = candidatureRepository.findAllSimple(pageable);

        for (Candidature candidature : statusdefi) {
            updateExpiredDefiIfNeeded(candidature);
        }
        try {
            Page<Candidature> result = candidatureRepository.findAllSimple(pageable);
            return result;

        } catch (Exception e) {
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("Service error: " + e.getMessage(), e);
        }
    }

    // Get all candidatures
    public List<Candidature> getAllCandidaturesList() {

        List<Candidature> result = candidatureRepository.findAllCandidature();

        for (Candidature candidature : result) {
            updateExpiredDefiIfNeeded(candidature);
        }
        return candidatureRepository.findAllCandidature();
    }

    // Get a candidature by id
    public Optional<Candidature> getCandidatureById(Integer id) {
        return candidatureRepository.findById(id);
    }


    public void updateExpiredDefiIfNeeded(Candidature candidature) {
        if (candidature.getStatutDefi() == Candidature.StatutDefi.ENVOYE && candidature.getDefiEnvoyeLe() != null) {
            long hoursElapsed = ChronoUnit.HOURS.between(candidature.getDefiEnvoyeLe(), LocalDateTime.now());
            if (hoursElapsed > 48) {
                candidature.setStatutDefi(Candidature.StatutDefi.EXPIRE);
                candidature.setDefiTermineLe(LocalDateTime.now());
                candidatureRepository.save(candidature);
                log.info("Candidature {} challenge expired after {} hours", candidature.getId(), hoursElapsed);
            }
        }
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
        Candidature candidature = candidatureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));

        candidatureRepository.delete(candidature);
    }

    @Transactional
    public void deleteById(Integer id) {
        candidatureRepository.deleteById(id);
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
