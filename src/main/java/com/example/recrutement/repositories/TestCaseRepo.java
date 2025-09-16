package com.example.recrutement.repositories;

import com.example.recrutement.entities.Challenge;
import com.example.recrutement.entities.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestCaseRepo extends JpaRepository<TestCase, Integer> {

    List<TestCase> findByChallenge(Challenge c);

    @Modifying
    @Query("DELETE FROM TestCase t WHERE t.challenge.id = :challengeId")
    void deleteByChallenge(@Param("challengeId") Integer challengeId);
}
