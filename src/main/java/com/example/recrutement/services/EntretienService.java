package com.example.recrutement.services;

import com.example.recrutement.entities.Entretien;
import com.example.recrutement.repositories.EntretienRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EntretienService {


    private final EntretienRepo EntretienRepository;

    public EntretienService(EntretienRepo EntretienRepository) {
        this.EntretienRepository = EntretienRepository;
    }

    public List<Entretien> findAll() {
        return EntretienRepository.findAll();
    }

    public Optional<Entretien> findById(Integer id) {
        return EntretienRepository.findById(id);
    }
    @Transactional
    public Entretien save(Entretien Entretien) {
        return EntretienRepository.save(Entretien);
    }

    public void deleteById(Integer id) {
        EntretienRepository.deleteById(id);
    }

    @Transactional
    public Entretien updateEntretien(Integer id, Entretien updatedEntretien) {
        Optional<Entretien> existingEntretien = EntretienRepository.findById(id);

        if (existingEntretien.isPresent()) {
            Entretien entretien = existingEntretien.get();
            // Update properties explicitly
            entretien.setDateEntretien(updatedEntretien.getDateEntretien());
            entretien.setCommentaireRH(updatedEntretien.getCommentaireRH());
            entretien.setResultat(updatedEntretien.getResultat());
            entretien.setLien(updatedEntretien.getLien());
            // Save and return updated entity
            return EntretienRepository.save(entretien);
        } else {
            throw new RuntimeException("Entretien not found with id: " + id);
        }
    }

}
