package com.example.recrutement.controllers;


import com.example.recrutement.entities.Entretien;
import com.example.recrutement.repositories.EntretienRepo;
import com.example.recrutement.services.EntretienService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/entretiens")
public class EntretienController {

    private final EntretienService EntretienService;
    private final EntretienRepo EntretienRepo;

    public EntretienController(EntretienService EntretienService,
                               EntretienRepo EntretienRepo) {
        this.EntretienService = EntretienService;
        this.EntretienRepo = EntretienRepo;
    }

    @GetMapping
    public List<Entretien> getAllEntretiens() {
        List<Entretien> list = EntretienRepo.findAll();
        System.out.println("Entretiens returned: " + list.size());
        return list;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Entretien> getEntretienById(@PathVariable Integer id) {
        return EntretienService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Entretien createEntretien(@RequestBody Entretien Entretien) {
        return EntretienService.save(Entretien);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Entretien> updateEntretien(@PathVariable Integer id, @RequestBody Entretien updatedEntretien) {
        return EntretienService.findById(id)
                .map(Entretien -> {
                    updatedEntretien.setId(Entretien.getId());
                    Entretien saved = EntretienService.save(updatedEntretien);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntretien(@PathVariable Integer id) {
        if (EntretienService.findById(id).isPresent()) {
            EntretienService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
