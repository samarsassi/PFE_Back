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
        try {
            Integer candidatureId = (Integer) execution.getVariable("candidatureId");
            Integer offreEmploiId = (Integer) execution.getVariable("offreEmploiId");

            // Validate required variables
            if (candidatureId == null) {
                throw new RuntimeException(
                        "Required process variable 'candidatureId' is null. Please provide a valid candidature ID when starting the process.");
            }

            if (offreEmploiId == null) {
                throw new RuntimeException(
                        "Required process variable 'offreEmploiId' is null. Please provide a valid offre emploi ID when starting the process.");
            }

            System.out.println("Processing CV analysis for candidature ID: " + candidatureId + " and offre emploi ID: "
                    + offreEmploiId);

            // Load entities
            Candidature candidature = candidatureService.getCandidatureById(candidatureId)
                    .orElseThrow(() -> new RuntimeException("Candidature not found with ID: " + candidatureId));

            OffreEmploi offre = offreEmploiService.getOffreEmploiById(offreEmploiId)
                    .orElseThrow(() -> new RuntimeException("OffreEmploi not found with ID: " + offreEmploiId));

            // Call your scoring method which updates and saves the candidature
            scoringService.scoreCandidature(candidature, offre);

            // Optionally set scoreCV as process variable if you want to use it in workflow
            // decisions
            if (candidature.getScoreCV() != null) {
                execution.setVariable("cvScore", candidature.getScoreCV());
                System.out.println("CV analysis completed. Score: " + candidature.getScoreCV());
            } else {
                System.out.println("CV analysis completed but no score was generated.");
            }

        } catch (Exception e) {
            System.err.println("Error in CV Analysis Delegate: " + e.getMessage());
            throw new RuntimeException("CV Analysis failed: " + e.getMessage(), e);
        }
    }
}
