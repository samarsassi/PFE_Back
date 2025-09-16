package com.example.recrutement.controllers;

import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.ChallengeRepo;
import com.example.recrutement.repositories.OffreEmploiRepo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final OffreEmploiRepo offreEmploiRepo;
    private final CandidatureRepo candidatureRepository;
    private final ChallengeRepo challengeRepository;

    public StatsController(OffreEmploiRepo offreEmploiRepo,
                           CandidatureRepo candidatureRepository,
                           ChallengeRepo challengeRepository) {
        this.offreEmploiRepo = offreEmploiRepo;
        this.candidatureRepository = candidatureRepository;
        this.challengeRepository = challengeRepository;
    }

    @GetMapping
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("offers", offreEmploiRepo.count());
        stats.put("candidatures", candidatureRepository.count());
        stats.put("challenges", challengeRepository.count());
        return stats;
    }
}
