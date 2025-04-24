package com.example.recrutement.controllers;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.services.OffreEmploiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/offres-emploi")
public class OffreEmploiController {

    private final OffreEmploiService offreEmploiService;

    @Autowired
    public OffreEmploiController(OffreEmploiService offreEmploiService) {
        this.offreEmploiService = offreEmploiService;
    }

    //@PreAuthorize("hasRole('admin')")
    @PostMapping
    public ResponseEntity<OffreEmploi> createOffreEmploi(@Valid @RequestBody OffreEmploi offreEmploi, Authentication connectedUser) {
        System.out.println("Authenticated User: " + (connectedUser != null ? connectedUser.getName() : "Not authenticated"));
        return ResponseEntity.ok(offreEmploiService.createOffreEmploi(offreEmploi, connectedUser));
    }

    @GetMapping("OffresEmplois")
    //@PreAuthorize("hasRole('User') or hasRole('ADMIN')")
    public List<OffreEmploi> getAllOffresEmploi() {
        return offreEmploiService.getAllOffresEmploi();
    }

    @GetMapping("/{id}")
    public OffreEmploi getOffreEmploiById(@PathVariable Integer id) {
        return offreEmploiService.getOffreEmploiById(id).orElseThrow(() -> new RuntimeException("OffreEmploi not found"));
    }

    @PutMapping("/{id}")
    public OffreEmploi updateOffreEmploi(@PathVariable Integer id, @RequestBody OffreEmploi updatedOffreEmploi) {
        return offreEmploiService.updateOffreEmploi(id, updatedOffreEmploi);
    }

    @DeleteMapping("/{id}")
    public void deleteOffreEmploi(@PathVariable Integer id) {
        offreEmploiService.deleteOffreEmploi(id);
    }

    @PutMapping("/{id}/archive")
    public OffreEmploi archiveOffreEmploi(@PathVariable Integer id) {
        return offreEmploiService.archiveOffreEmploi(id);
    }

    @PutMapping("/{id}/unarchive")
    public OffreEmploi unarchiveOffreEmploi(@PathVariable Integer id) {
        return offreEmploiService.unarchiveOffreEmploi(id);
    }

    @GetMapping("/{id}/candidatures")
    public List<Candidature> getCandidaturesForOffreEmploi(@PathVariable Integer id) {
        return offreEmploiService.getCandidaturesForOffreEmploi(id);
    }
}