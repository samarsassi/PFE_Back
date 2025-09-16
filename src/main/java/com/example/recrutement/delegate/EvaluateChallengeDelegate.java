package com.example.recrutement.delegate;

import com.example.recrutement.entities.SoumissionDefi;
import com.example.recrutement.repositories.SoumissionDefiRepo;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("evaluateChallengeDelegate")
public class EvaluateChallengeDelegate implements JavaDelegate {

    @Autowired
    private SoumissionDefiRepo soumissionDefiRepo;

    @Override
    public void execute(DelegateExecution execution) {
        Integer candidatureId = (Integer) execution.getVariable("candidatureId");

        if (candidatureId == null) {
            throw new RuntimeException("Missing process variable 'candidatureId'");
        }

        // In absence of a direct repository method, find by all and filter
        Optional<SoumissionDefi> maybe = soumissionDefiRepo.findAll().stream()
                .filter(s -> s.getCandidature() != null && s.getCandidature().getId().equals(candidatureId))
                .findFirst();

        boolean passed = false;
        double score = 0.0;

        if (maybe.isPresent()) {
            SoumissionDefi submission = maybe.get();
            String code = submission.getCode();
            // naive evaluation: basic non-empty + minimal length
            if (code != null && code.trim().length() >= 10) {
                passed = true;
                score = 85.0;
            } else {
                passed = false;
                score = 45.0;
            }
            submission.setScore(score);
            submission.setStatut(SoumissionDefi.StatutSoumission.Termine);
            soumissionDefiRepo.save(submission);
        }

        execution.setVariable("challengePassed", passed);
        execution.setVariable("challengeScore", score);
    }
}


