package com.example.recrutement.delegate;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.Challenge;
import com.example.recrutement.entities.SoumissionDefi;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.ChallengeRepo;
import com.example.recrutement.repositories.SoumissionDefiRepo;
import com.example.recrutement.services.EmailService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.mail.MessagingException;
import java.time.LocalDateTime;

@Component("sendChallengeDelegate")
public class SendChallengeDelegate implements JavaDelegate {

    @Autowired
    private CandidatureRepo candidatureRepo;

    @Autowired
    private ChallengeRepo challengeRepo;

    @Autowired
    private SoumissionDefiRepo soumissionDefiRepo;

    @Autowired
    private EmailService emailService;

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("[FLOWABLE] SendChallengeDelegate started");

        Integer candidatureId = (Integer) execution.getVariable("candidatureId");
        Integer challengeId = (Integer) execution.getVariable("challengeId");
        String candidateEmail = (String) execution.getVariable("candidateEmail");

        System.out.println("[FLOWABLE] Processing candidature: " + candidatureId);

        // Load candidature
        Candidature candidature = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));

        if (candidateEmail == null || candidateEmail.isBlank()) {
            candidateEmail = candidature.getEmail();
        }

        // Load or fallback challenge
        Challenge challenge;
        if (challengeId != null) {
            challenge = challengeRepo.findById(challengeId)
                    .orElseThrow(() -> new RuntimeException("Challenge not found"));
        } else {
            challenge = challengeRepo.findAll().stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("No challenge available to assign"));
        }

        // Update candidature with challenge
        candidature.setDefi(challenge);
        candidature.setDefiEnvoyeLe(LocalDateTime.now());
        candidature.setStatutDefi(Candidature.StatutDefi.ENVOYE);
        candidatureRepo.save(candidature);

        // The gateway will wait for this to be updated to TERMINE or EXPIRE
        execution.setVariable("statutDefi", "EN_ATTENTE");
        System.out.println("[FLOWABLE] Set statutDefi to EN_ATTENTE, waiting for challenge completion");

        // Create SoumissionDefi
        SoumissionDefi soumission = new SoumissionDefi();
        soumission.setChallenge(challenge);
        soumission.setCandidature(candidature);
        soumission.setStatut(SoumissionDefi.StatutSoumission.En_evaluation);
        soumission.setSoumisLe(LocalDateTime.now());
        String createdBy = (String) execution.getVariable("createdBy");
        soumission.setCreePar(createdBy); // assuming creePar is a String

        candidature.setSoumissionDefi(soumission);
        soumissionDefiRepo.save(soumission);

        // Send email
        String emailBody = emailService.buildChallengeAssignmentEmail(
                candidature.getNom(),
                challenge.getTitre(),
                "http://localhost:4200/main"
        );

        try {
            emailService.sendHtmlEmail(candidateEmail, "Nouveau défi technique attribué", emailBody);
            System.out.println("[FLOWABLE] Challenge email sent successfully");
        } catch (MessagingException e) {
            System.err.println("[FLOWABLE] Failed to send email: " + e.getMessage());
            throw new RuntimeException("Failed to send challenge email: " + e.getMessage());
        }

        System.out.println("[FLOWABLE] SendChallengeDelegate completed");
    }
}
