package com.example.recrutement.controllers;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.services.CandidatureService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/candidatures")
public class CandidatureController {

    private final CandidatureService candidatureService;

    @Autowired
    public CandidatureController(CandidatureService candidatureService) {
        this.candidatureService = candidatureService;
    }

    // Create a new Candidature (Job application)
    @PostMapping
    public ResponseEntity<Candidature> createCandidature(@Valid @RequestBody Candidature candidature,
                                                         @RequestParam Integer offreEmploiId,
                                                         Authentication connectedUser) {
        return ResponseEntity.ok(candidatureService.createCandidature(candidature, offreEmploiId, connectedUser));
    }

    // Get all candidatures
    @GetMapping
    public List<Candidature> getAllCandidatures() {
        return candidatureService.getAllCandidatures();
    }

    // Get a candidature by id
    @GetMapping("/{id}")
    public Candidature getCandidatureById(@PathVariable Integer id) {
        return candidatureService.getCandidatureById(id).orElseThrow(() -> new RuntimeException("Candidature not found"));
    }

    // Update a candidature by id
    @PutMapping("/{id}")
    public Candidature updateCandidature(@PathVariable Integer id, @RequestBody Candidature updatedCandidature) {
        return candidatureService.updateCandidature(id, updatedCandidature);
    }

    // Delete a candidature by id
    @DeleteMapping("/{id}")
    public void deleteCandidature(@PathVariable Integer id) {
        candidatureService.deleteCandidature(id);
    }

    // Get all candidatures for a specific job offer (OffreEmploi)
    @GetMapping("/{offreEmploiId}")
    public List<Candidature> getCandidaturesForOffreEmploi(@PathVariable Integer offreEmploiId) {
        return candidatureService.getCandidaturesForOffreEmploi(offreEmploiId);
    }
}
