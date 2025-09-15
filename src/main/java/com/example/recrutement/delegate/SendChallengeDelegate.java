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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional
    public void execute(DelegateExecution execution) {
        System.out.println("[FLOWABLE] SendChallengeDelegate started");

        Integer candidatureId = (Integer) execution.getVariable("candidatureId");
        String candidateEmail = (String) execution.getVariable("candidateEmail");

        // Load candidature
        Candidature candidature = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));

        if (candidateEmail == null || candidateEmail.isBlank()) {
            candidateEmail = candidature.getEmail();
        }

        // Pick a challenge (first available)
        Challenge challenge = challengeRepo.findAll().stream().findFirst()
                .orElseThrow(() -> new RuntimeException("No challenge available"));

        // Assign challenge to candidature
        candidature.setDefi(challenge);
        candidature.setDefiEnvoyeLe(LocalDateTime.now());
        candidature.setStatutDefi(Candidature.StatutDefi.ENVOYE);

        // Create SoumissionDefi
        SoumissionDefi soumission = new SoumissionDefi();
        soumission.setChallenge(challenge);
        soumission.setCandidature(candidature);
        soumission.setStatut(SoumissionDefi.StatutSoumission.En_evaluation);
        soumission.setSoumisLe(LocalDateTime.now());
        soumissionDefiRepo.save(soumission);

        // Link submission to candidature
        candidature.setSoumissionDefi(soumission);

        // Persist the updated candidature (with challenge and submission)
        candidatureRepo.save(candidature);

        // Set Flowable process variable
        execution.setVariable("statutDefi", "ENVOYE");
        System.out.println("[FLOWABLE] Set statutDefi to ENVOYE, waiting for challenge completion");

        // Send email notification
        try {
            String emailBody = emailService.buildChallengeAssignmentEmail(
                    candidature.getNom(),
                    challenge.getTitre(),
                    "http://localhost:4200/main"
            );
            emailService.sendHtmlEmail(candidateEmail, "Nouveau défi technique attribué", emailBody);
            System.out.println("[FLOWABLE] Challenge email sent successfully");
        } catch (MessagingException e) {
            System.err.println("[FLOWABLE] Failed to send email: " + e.getMessage());
            throw new RuntimeException("Failed to send challenge email", e);
        }

        System.out.println("[FLOWABLE] SendChallengeDelegate completed");
    }
}
