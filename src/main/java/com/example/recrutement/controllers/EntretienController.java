package com.example.recrutement.controllers;


import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.Entretien;
import com.example.recrutement.entities.dto.EntretienDTO;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.EntretienRepo;
import com.example.recrutement.services.CandidatureService;
import com.example.recrutement.services.EntretienService;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/entretiens")
public class EntretienController {

    private final EntretienService EntretienService;
    private final CandidatureService CandidatureService;
    private final EntretienRepo EntretienRepo;
    private final CandidatureRepo CandidatureRepo;

    public EntretienController(EntretienService entretienService,
                               CandidatureService candidatureService,
                               EntretienRepo entretienRepo,
                               CandidatureRepo candidatureRepo) {
        EntretienService = entretienService;
        CandidatureService = candidatureService;
        EntretienRepo = entretienRepo;
        CandidatureRepo = candidatureRepo;
    }


    @GetMapping
    public List<EntretienDTO> getAllEntretiens() {
        List<Entretien> entretiens = EntretienRepo.findAll();
        List<EntretienDTO> dtoList = entretiens.stream()
                .map(EntretienDTO::new)
                .toList();

        System.out.println("Entretiens returned: " + dtoList.size());
        return dtoList;
    }



    @GetMapping("/{id}")
    public ResponseEntity<Entretien> getEntretienById(@PathVariable Integer id) {
        return EntretienService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{candidatureId}/create")
    @Transactional
    public Entretien createEntretien(@PathVariable Integer candidatureId, @RequestBody Entretien entretien) {
        Candidature candidature = CandidatureService.getCandidatureById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature non trouvée"));

        entretien.setCandidature(candidature);
        candidature.setEntretien(entretien);
        candidature.setStatutEntretien(Candidature.StatutEnt.ENVOYE);

        return EntretienService.save(entretien);
    }




    @PutMapping("/{id}")
    public ResponseEntity<Entretien> updateEntretien(@PathVariable Integer id, @RequestBody Entretien updatedEntretien) {
        System.out.println("Updating entretien with ID: " + id + ", dateEntretien: " + updatedEntretien.getDateEntretien());
        Entretien updated = EntretienService.updateEntretien(id, updatedEntretien);
        return ResponseEntity.ok(updated);
    }





    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntretien(@PathVariable Integer id) {
        Optional<Entretien> entretienOpt = EntretienService.findById(id);
        if (entretienOpt.isPresent()) {
            Entretien entretien = entretienOpt.get();

            // Break the association from the candidature
            Candidature candidature = entretien.getCandidature();
            if (candidature != null) {
                candidature.setEntretien(null);
                candidature.setStatutEntretien(Candidature.StatutEnt.AUCUN);
                CandidatureRepo.save(candidature); // Save the update to remove the link
            }

            EntretienService.deleteById(id); // Now delete the entretien
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

}
