package com.example.recrutement.controllers;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.Challenge;
import com.example.recrutement.entities.SoumissionDefi;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.ChallengeRepo;
import com.example.recrutement.repositories.SoumissionDefiRepo;
import com.example.recrutement.services.ChallengeService;
import com.example.recrutement.services.EmailService;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;
    private final EmailService emailService;
    private final ChallengeRepo challengeRepo;
    private final CandidatureRepo candidatureRepo;
    private final SoumissionDefiRepo soumissionDefiRepo;

    public ChallengeController(ChallengeService challengeService,
                               EmailService emailService,
                               ChallengeRepo challengeRepo,
                               CandidatureRepo candidatureRepo,
                               SoumissionDefiRepo soumissionDefiRepo) {
        this.challengeService = challengeService;
        this.emailService = emailService;
        this.challengeRepo = challengeRepo;
        this.candidatureRepo = candidatureRepo;
        this.soumissionDefiRepo = soumissionDefiRepo;
    }


    @GetMapping
    public List<Challenge> getAllChallenges() {
        return challengeService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Challenge> getChallengeById(@PathVariable Integer id) {
        return challengeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Challenge> createChallenge(@RequestBody Challenge challenge) {
        if (challenge.getTestCases() != null) {
            challenge.getTestCases().forEach(tc -> tc.setChallenge(challenge));
        }

        Challenge savedChallenge = challengeRepo.save(challenge);
        return ResponseEntity.ok(savedChallenge);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Challenge> updateChallenge(@PathVariable Integer id, @RequestBody Challenge updatedChallenge) {
        return challengeService.findById(id)
                .map(challenge -> {
                    updatedChallenge.setId(challenge.getId());
                    Challenge saved = challengeService.save(updatedChallenge);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChallenge(@PathVariable Integer id) {
        if (challengeService.findById(id).isPresent()) {
            challengeService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{candidatureId}/send-challenge/{challengeId}")
    public ResponseEntity<Map<String, String>> sendChallengeToCandidate(@PathVariable Integer candidatureId, @PathVariable Integer challengeId) {

        Candidature candidature = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));
        Challenge challenge = challengeRepo.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));

        //Create SoumissionDefi entry
        SoumissionDefi soumission = new SoumissionDefi();
        soumission.setChallenge(challenge);
        soumission.setCandidature(candidature);
        soumission.setStatut(SoumissionDefi.StatutSoumission.En_evaluation);
        soumission.setSoumisLe(LocalDateTime.now());
        soumissionDefiRepo.save(soumission);

        //Update  lel Candidature with challenge info
        candidature.setDefi(challenge);
        candidature.setDefiEnvoyeLe(LocalDateTime.now());
        candidature.setStatutDefi(Candidature.StatutDefi.ENVOYE);
        candidatureRepo.save(candidature);

        //email
        String emailBody = emailService.buildChallengeAssignmentEmail(
                candidature.getNom(),
                challenge.getTitre(),
                "http://localhost:4200/main"
        );

        try {
            emailService.sendHtmlEmail(candidature.getEmail(), "Nouveau défi technique attribué", emailBody);
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        System.out.println(">>> Assigning challenge " + challengeId + " to candidature " + candidatureId);
        System.out.println(">>> Candidature updated with status: " + candidature.getStatutDefi());

        return ResponseEntity.ok(Map.of("message", "Challenge assigned and notification sent"));
    }


}
