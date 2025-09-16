package com.example.recrutement.repositories;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.Challenge;
import com.example.recrutement.entities.SoumissionDefi;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface SoumissionDefiRepo extends JpaRepository<SoumissionDefi, Integer> {
    List<SoumissionDefi> findByCandidatureEmail(String email);
    List<SoumissionDefi> findByChallenge(Challenge challenge);
List<SoumissionDefi> findByCandidature(Candidature candidature);
    @Modifying
    @Query("DELETE FROM SoumissionDefi s WHERE s.challenge.id = :challengeId")
    void deleteByChallenge(@Param("challengeId") Integer challengeId);

    @Modifying
    @Transactional
    @Query("DELETE FROM SoumissionDefi s WHERE s.candidature.id = :candidatureId")
    void deleteByCandidatureId(@Param("candidatureId") Integer candidatureId);
}
