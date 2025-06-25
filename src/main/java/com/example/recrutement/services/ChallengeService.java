package com.example.recrutement.services;

import com.example.recrutement.entities.Challenge;
import com.example.recrutement.repositories.ChallengeRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ChallengeService {


    private final ChallengeRepo challengeRepository;

    public ChallengeService(ChallengeRepo challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    public List<Challenge> findAll() {
        return challengeRepository.findAll();
    }

    public Optional<Challenge> findById(Integer id) {
        return challengeRepository.findById(id);
    }

    public Challenge save(Challenge challenge) {
        return challengeRepository.save(challenge);
    }

    public void deleteById(Integer id) {
        challengeRepository.deleteById(id);
    }
}
