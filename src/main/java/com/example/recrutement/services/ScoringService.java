package com.example.recrutement.services;

import com.example.recrutement.controllers.CandidatureController;
import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.OffreEmploiRepo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ScoringService {

    private final CandidatureRepo candidatureRepo;
    private final OffreEmploiRepo offreEmploiRepo;
    private final OllamaService ollamaService;
    private static final Logger log = LoggerFactory.getLogger(CandidatureController.class);

    // Constants for better maintainability
    private static final int MAX_CV_TEXT_LENGTH = 2000;
    private static final String DEFAULT_SCORING_ERROR = "Error during CV analysis";

    public ScoringService(CandidatureRepo candidatureRepo,
                          OffreEmploiRepo offreEmploiRepo,
                          OllamaService ollamaService) {
        this.candidatureRepo = candidatureRepo;
        this.offreEmploiRepo = offreEmploiRepo;
        this.ollamaService = ollamaService;
    }

    @Transactional
    public void scoreAllCvs() {
        List<Candidature> candidatures = candidatureRepo.findAll();
        log.info("Starting to score {} candidatures", candidatures.size());

        int successCount = 0;
        int errorCount = 0;

        for (Candidature c : candidatures) {
            try {
                if (c.getCvUrl() == null || c.getOffreEmploi() == null) {
                    log.warn("Skipping candidature ID {} - missing CV URL or job offer", c.getId());
                    continue;
                }

                scoreCandidature(c, c.getOffreEmploi());
                successCount++;

            } catch (Exception e) {
                errorCount++;
                log.error("Failed to score CV for candidature ID {}: {}", c.getId(), e.getMessage());

                // Set error state
                c.setScoreCV(null);
                c.setScoringComment(DEFAULT_SCORING_ERROR + ": " + e.getMessage());
                candidatureRepo.save(c);
            }
        }

        log.info("Scoring completed. Success: {}, Errors: {}", successCount, errorCount);
    }

    @Transactional
    public void scoreCandidature(Candidature candidature, OffreEmploi offreEmploi) {
        log.info("Starting to score candidature ID: {}", candidature.getId());

        String cvText = extractCvText(candidature);
        if (cvText == null) {
            return;
        }

        String prompt = buildOptimizedPrompt(offreEmploi, cvText);

        try {
            String result = ollamaService.generateFromPhi(prompt, 0.0);
            log.debug("AI Response for candidature {}: {}", candidature.getId(), result);

            Double score = extractScoreFromText(result);

            candidature.setScoreCV(score);
            candidature.setScoringComment(result);
            candidatureRepo.save(candidature);

            log.info("Successfully scored candidature {} with score: {}", candidature.getId(), score);

        } catch (Exception e) {
            log.error("Error during AI analysis for candidature {}: {}", candidature.getId(), e.getMessage());
            candidature.setScoreCV(null);
            candidature.setScoringComment(DEFAULT_SCORING_ERROR + ": " + e.getMessage());
            candidatureRepo.save(candidature);
        }
    }

    private String extractCvText(Candidature candidature) {
        try {
            String cvText = ollamaService.extractTextFromPdf(candidature.getCvUrl());
            log.debug("Extracted CV text for candidature {} (length: {})", candidature.getId(), cvText.length());

            // Truncate if too long for better model performance
            if (cvText.length() > MAX_CV_TEXT_LENGTH) {
                cvText = cvText.substring(0, MAX_CV_TEXT_LENGTH) + "...";
                log.debug("CV text truncated to {} characters", MAX_CV_TEXT_LENGTH);
            }

            return cvText;

        } catch (IOException e) {
            log.error("Failed to extract CV text for candidature {}: {}", candidature.getId(), e.getMessage());
            candidature.setScoreCV(null);
            candidature.setScoringComment("Error: Could not extract CV text - " + e.getMessage());
            candidatureRepo.save(candidature);
            return null;
        }
    }

    private String buildOptimizedPrompt(OffreEmploi offreEmploi, String cvText) {
        // Aggressively truncate CV text. Phi struggles with long inputs.
        String truncatedCvText = cvText.length() > 500 ? cvText.substring(0, 500) + "..." : cvText;

        return String.format("""
     INSTRUCTION:
        You are a professional recruiter AI analyzing a candidate's resume **strictly** based on the provided job offer.
        Do not invent or assume information beyond what's provided.

        --- JOB OFFER DETAILS ---
        Title: %s
        Description: %s
        Required Experience: %s years

        --- CANDIDATE RESUME (TRUNCATED) ---
        %s

        --- EVALUATION TASK ---
        1. Provide a numeric **RATING** from 1 to 10 indicating how well the candidate fits the job.
        2. List 2 key **STRENGTHS** from the resume relevant to the job.
        3. Write 1 sentence of notable **POSITIVE** attributes.
        4. List 1-2 **CONCERNS** or gaps in skills or experience.
        5. Provide an **OVERALL ASSESSMENT** choosing one: Well-qualified / Moderately qualified / Less qualified.

        Format your response exactly as follows:

        RATING: [number]
        STRENGTHS:
        - [bullet 1]
        - [bullet 2]
        POSITIVE:
        - [sentence]
        CONCERNS:
        - [sentence]
        OVERALL ASSESSMENT: [one of the three options above]""",
                offreEmploi.getTitre(),
                offreEmploi.getDescription(),
                offreEmploi.getNiveauExperience(),
                truncatedCvText);
    }
    private Double extractScoreFromText(String response) {
        if (response == null || response.isEmpty()) return null;

        // Try to find "RATING: X", "RATING: X/10", "RATING X", "RATING X/10" (case-insensitive)
        Pattern ratingPattern = Pattern.compile("(?i)rating[:\\s]*([0-9]+(?:\\.[0-9]+)?)(?:/10)?");
        Matcher matcher = ratingPattern.matcher(response);
        if (matcher.find()) {
            try {
                double score = Double.parseDouble(matcher.group(1));
                // If score seems over 10 (like 80 or 100), normalize it to 0-10 scale
                if (score > 10) score = score / 10;
                if (score < 0 || score > 10) return null;
                return score;
            } catch (NumberFormatException e) {
                // fallback to alternative extraction below
            }
        }

        // Fallback: look for a standalone number between 0 and 10 in the text
        Pattern numberPattern = Pattern.compile("\\b([0-9](?:\\.[0-9]+)?|10)\\b");
        matcher = numberPattern.matcher(response);
        if (matcher.find()) {
            try {
                double score = Double.parseDouble(matcher.group(1));
                if (score < 0 || score > 10) return null;
                return score;
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null; // no score found
    }


    private Double extractScore(String response) {
        // Look for "SCORE: X/10" or "SCORE: X" pattern first
        Pattern scorePattern = Pattern.compile("(?i)score[:\\s]+(\\d+(?:\\.\\d+)?)(?:/10)?");
        Matcher matcher = scorePattern.matcher(response);

        if (matcher.find()) {
            double score = Double.parseDouble(matcher.group(1));
            return score > 10 ? score / 10 : score; // Convert if out of 100
        }

        // Fallback to your original pattern
        Pattern pattern = Pattern.compile("\\b([0-9](?:\\.\\d+)?|10)\\b");
        Matcher fallbackMatcher = pattern.matcher(response);
        if (fallbackMatcher.find()) {
            return Double.parseDouble(fallbackMatcher.group(1));
        }

        return null;
    }


    // Async version for better performance in controller
    @Async
    public CompletableFuture<Void> scoreCandidatureAsync(Candidature candidature, OffreEmploi offreEmploi) {
        scoreCandidature(candidature, offreEmploi);
        return CompletableFuture.completedFuture(null);
    }
}