package com.example.recrutement.services;

import com.example.recrutement.entities.Entretien;
import com.example.recrutement.repositories.EntretienRepo;
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

    public Entretien save(Entretien Entretien) {
        return EntretienRepository.save(Entretien);
    }

    public void deleteById(Integer id) {
        EntretienRepository.deleteById(id);
    }
}
