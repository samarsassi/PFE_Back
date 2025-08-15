package com.example.recrutement.delegate;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.services.CandidatureService;
import com.example.recrutement.services.OffreEmploiService;
import com.example.recrutement.services.ScoringService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("cvAnalysisDelegate")
public class CvAnalysisDelegate implements JavaDelegate {

    @Autowired
    private CandidatureService candidatureService;

    @Autowired
    private OffreEmploiService offreEmploiService;

    @Autowired
    private ScoringService scoringService;

    public CvAnalysisDelegate() {
        // Default constructor - Spring will inject dependencies via @Autowired fields
    }

    public CvAnalysisDelegate(CandidatureService candidatureService,
                              OffreEmploiService offreEmploiService,
                              ScoringService scoringService) {
        this.candidatureService = candidatureService;
        this.offreEmploiService = offreEmploiService;
        this.scoringService = scoringService;
    }

    @Override
    public void execute(DelegateExecution execution) {
        Integer candidatureId = (Integer) execution.getVariable("candidatureId");
        Integer offreEmploiId = (Integer) execution.getVariable("offreEmploiId");

        // Load entities
        Candidature candidature = candidatureService.getCandidatureById(candidatureId)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));
        OffreEmploi offre = offreEmploiService.getOffreEmploiById(offreEmploiId)
                .orElseThrow(() -> new RuntimeException("OffreEmploi not found"));

        // Call your scoring method which updates and saves the candidature
        scoringService.scoreCandidature(candidature, offre);

        // Optionally set scoreCV as process variable if you want to use it in workflow decisions
        if (candidature.getScoreCV() != null) {
            execution.setVariable("cvScore", candidature.getScoreCV());
        }
    }
}
