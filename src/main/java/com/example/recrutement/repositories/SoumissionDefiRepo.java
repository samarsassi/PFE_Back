package com.example.recrutement.repositories;

import com.example.recrutement.entities.SoumissionDefi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface SoumissionDefiRepo extends JpaRepository<SoumissionDefi, Integer> {
    List<SoumissionDefi> findByCandidatureEmail(String email);
}
