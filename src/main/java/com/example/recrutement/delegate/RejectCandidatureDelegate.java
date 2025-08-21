package com.example.recrutement.delegate;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.repositories.CandidatureRepo;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("rejectCandidatureDelegate")
public class RejectCandidatureDelegate implements JavaDelegate {
    @Autowired
    private CandidatureRepo candidatureRepo;

    @Override
    public void execute(DelegateExecution execution) {
        Integer candidatureId = (Integer) execution.getVariable("candidatureId");
        Candidature candidature = candidatureRepo.findById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));
        candidature.setStatut("REFUSEE");
        candidatureRepo.save(candidature);
    }
}