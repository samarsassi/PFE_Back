package com.example.recrutement.repositories;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepo extends JpaRepository<Challenge, Integer> {

}
