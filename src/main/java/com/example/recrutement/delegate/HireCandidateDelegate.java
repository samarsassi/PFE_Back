package com.example.recrutement.delegate;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.repositories.CandidatureRepo;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component("hireCandidateDelegate")
public class HireCandidateDelegate implements JavaDelegate {

    @Autowired
    private CandidatureRepo candidatureRepo;

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("[FLOWABLE] HireCandidateDelegate started");

        Integer candidatureId = (Integer) execution.getVariable("candidatureId");
        if (candidatureId == null) {
            throw new RuntimeException("Missing process variable 'candidatureId'");
        }

        System.out.println("[FLOWABLE] Hiring candidature: " + candidatureId);

        Candidature candidature = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));

        candidature.setStatut("ACCEPTÉ");
        candidature.setDecisionFinale("EMBAUCHE");
        candidatureRepo.save(candidature);

        execution.setVariable("finalStatus", "EMBAUCHE");
        execution.setVariable("hireDate", LocalDateTime.now());

        System.out.println("[FLOWABLE] Candidate hired successfully");
    }
}
