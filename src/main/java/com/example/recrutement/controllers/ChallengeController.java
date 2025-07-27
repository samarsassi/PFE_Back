package com.example.recrutement.controllers;

import com.example.recrutement.entities.*;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.ChallengeRepo;
import com.example.recrutement.repositories.SoumissionDefiRepo;
import com.example.recrutement.repositories.TestCaseRepo;
import com.example.recrutement.services.ChallengeService;
import com.example.recrutement.services.EmailService;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/challenges")
public class ChallengeController {

    private static final Logger log = LoggerFactory.getLogger(ChallengeController.class);

    private final ChallengeService challengeService;
    private final EmailService emailService;
    private final ChallengeRepo challengeRepo;
    private final CandidatureRepo candidatureRepo;
    private final SoumissionDefiRepo soumissionDefiRepo;
    private final TestCaseRepo testCaseRepo;

    public ChallengeController(ChallengeService challengeService,
                               EmailService emailService,
                               ChallengeRepo challengeRepo,
                               CandidatureRepo candidatureRepo,
                               SoumissionDefiRepo soumissionDefiRepo,
                               TestCaseRepo testCaseRepo) {
        this.challengeService = challengeService;
        this.emailService = emailService;
        this.challengeRepo = challengeRepo;
        this.candidatureRepo = candidatureRepo;
        this.soumissionDefiRepo = soumissionDefiRepo;
        this.testCaseRepo = testCaseRepo;
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
                .map(existingChallenge -> {
                    // Detach old test cases
                    existingChallenge.getTestCases().clear();

                    // Set the relationship on each new test case
                    for (TestCase tc : updatedChallenge.getTestCases()) {
                        tc.setChallenge(existingChallenge);
                    }

                    // Add new test cases
                    existingChallenge.getTestCases().addAll(updatedChallenge.getTestCases());

                    // Update other fields
                    existingChallenge.setTitre(updatedChallenge.getTitre());
                    existingChallenge.setDescription(updatedChallenge.getDescription());
                    existingChallenge.setLanguageId(updatedChallenge.getLanguageId());
                    existingChallenge.setLanguageName(updatedChallenge.getLanguageName());
                    existingChallenge.setDifficulte(updatedChallenge.getDifficulte());
                    existingChallenge.setTempslimite(updatedChallenge.getTempslimite());
                    existingChallenge.setMemoirelimite(updatedChallenge.getMemoirelimite());
                    existingChallenge.setCodeDepart(updatedChallenge.getCodeDepart());
                    existingChallenge.setStatut(updatedChallenge.getStatut());

                    Challenge saved = challengeService.save(existingChallenge);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteChallenge(@PathVariable Integer id) {
        log.info("-------- {}", id);

     Challenge challenge = challengeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));


        List<Candidature> linkedCandidatures = candidatureRepo.findByDefi(challenge);
        for (Candidature c : linkedCandidatures) {
            c.setDefi(null);
            c.setStatutDefi(Candidature.StatutDefi.AUCUN);
        }
        candidatureRepo.saveAll(linkedCandidatures);

        List<SoumissionDefi> DefiSoumis = soumissionDefiRepo.findByChallenge(challenge);
        for (SoumissionDefi s : DefiSoumis) {
            soumissionDefiRepo.delete(s);
        }

        List<TestCase> testCases = testCaseRepo.findByChallenge(challenge);
        for (TestCase t : testCases) {
            testCaseRepo.delete(t);
        }

        log.info("challenge is .......... {}",challenge);

        // Step 2: Now delete the challenge safely
        challengeRepo.delete(challenge);

        return ResponseEntity.ok("Challenge deleted successfully");
    }

    @PostMapping("/{candidatureId}/send-challenge/{challengeId}")
    public ResponseEntity<Map<String, String>> sendChallengeToCandidate(@PathVariable Integer candidatureId, @PathVariable Integer challengeId) {

        Candidature candidature = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));
        Challenge challenge = challengeRepo.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found"));


        //Update  lel Candidature with challenge info
        candidature.setDefi(challenge);
        candidature.setDefiEnvoyeLe(LocalDateTime.now());
        candidature.setStatutDefi(Candidature.StatutDefi.ENVOYE);
        candidatureRepo.save(candidature);

        //Create SoumissionDefi entry
        SoumissionDefi soumission = new SoumissionDefi();
        soumission.setChallenge(challenge);
        soumission.setCandidature(candidature);
        soumission.setStatut(SoumissionDefi.StatutSoumission.En_evaluation);
        soumission.setSoumisLe(LocalDateTime.now());
        candidature.setSoumissionDefi(soumission);
        soumissionDefiRepo.save(soumission);

        //email
         String emailBody = emailService.buildChallengeAssignmentEmail(
            candidature.getNom(),
          challenge.getTitre(),
          "http://localhost:4200/main"
         );

         try {
            emailService.sendHtmlEmail(candidature.getEmail(), "Nouveau défi technique attribué", emailBody); }
         catch (MessagingException e) { e.printStackTrace();}

        return ResponseEntity.ok(Map.of("message", "Challenge assigned and notification sent"));
    }


}
