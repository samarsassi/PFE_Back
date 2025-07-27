package com.example.recrutement.repositories;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.Entretien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface EntretienRepo extends JpaRepository<Entretien, Integer>, JpaSpecificationExecutor<Entretien> {


}
