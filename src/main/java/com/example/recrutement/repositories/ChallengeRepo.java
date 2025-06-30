package com.example.recrutement.repositories;

import com.example.recrutement.entities.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeRepo extends JpaRepository<Challenge, Integer> {
}
