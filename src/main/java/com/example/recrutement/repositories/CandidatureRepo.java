package com.example.recrutement.repositories;

import com.example.recrutement.entities.Candidature;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CandidatureRepo extends JpaRepository<Candidature, Integer>, JpaSpecificationExecutor<Candidature> {

    //@Query("SELECT c FROM Candidature c JOIN FETCH c.offreEmploi WHERE c.id = :id")
    @Query("""
    SELECT c FROM Candidature c
    JOIN FETCH c.offreEmploi
""")

    List<Candidature> findAllCandidature();
}
