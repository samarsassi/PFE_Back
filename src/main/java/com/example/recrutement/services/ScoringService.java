package com.example.recrutement.services;

import com.example.recrutement.controllers.CandidatureController;
import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.OffreEmploiRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ScoringService {

    private final CandidatureRepo candidatureRepo;
    private final OffreEmploiRepo offreEmploiRepo;
    private final OllamaService ollamaService;
    private static final Logger log = LoggerFactory.getLogger(CandidatureController.class);

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

        int successCount = 0, errorCount = 0;

        for (Candidature c : candidatures) {
            if (c.getCvUrl() == null || c.getOffreEmploi() == null) {
                log.warn("Skipping candidature ID {} - missing CV URL or job offer", c.getId());
                continue;
            }
            try {
                scoreCandidature(c, c.getOffreEmploi());
                successCount++;
            } catch (Exception e) {
                errorCount++;
                log.error("Failed to score CV for candidature ID {}: {}", c.getId(), e.getMessage(), e);
                c.setScoreCV(0.0);
                c.setScoringComment(DEFAULT_SCORING_ERROR + ": " + e.getMessage());
                candidatureRepo.save(c);
            }
        }

        log.info("Scoring completed. Success: {}, Errors: {}", successCount, errorCount);
    }

    @Transactional
    public void scoreCandidature(Candidature candidature, OffreEmploi offreEmploi) {
        log.info("Scoring candidature ID: {}", candidature.getId());

        String cvText = extractCvText(candidature);
        if (cvText == null) return;

        String jobTitle = offreEmploi.getTitre();
        String jobDescription = offreEmploi.getDescription();
        if (jobDescription == null || jobDescription.isBlank()) {
            log.error("Job description is empty for candidature {}", candidature.getId());
            candidature.setScoreCV(0.0);
            candidature.setScoringComment("Error: Job description is missing");
            candidatureRepo.save(candidature);
            return;
        }

        log.debug("Job Title: {}, Job Description length: {}", jobTitle, jobDescription.length());

        String prompt = buildOptimizedPrompt(jobTitle, jobDescription, cvText);

        String aiResponse = null;
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                aiResponse = ollamaService.generateFromPhi(prompt, 0.1);
                log.info("AI response attempt {} for candidature {}: {}", attempt, candidature.getId(), aiResponse);
                break;
            } catch (Exception e) {
                log.warn("AI call attempt {} failed for candidature {}: {}", attempt, candidature.getId(), e.getMessage());
                if (attempt == maxRetries) {
                    log.error("All AI attempts failed for candidature {}", candidature.getId(), e);
                    candidature.setScoreCV(0.0);
                    candidature.setScoringComment(DEFAULT_SCORING_ERROR + ": " + e.getMessage());
                    candidatureRepo.save(candidature);
                    return;
                }
                try {
                    Thread.sleep(1000 * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if (aiResponse == null || aiResponse.isBlank()) {
            log.error("Empty AI response for candidature {}", candidature.getId());
            candidature.setScoreCV(0.0);
            candidature.setScoringComment("Error: AI service returned empty response");
            candidatureRepo.save(candidature);
            return;
        }

        ScoringResult result = extractScoringResult(aiResponse);

        String finalComment = result.comment != null && !result.comment.isBlank()
                ? result.comment
                : generateFallbackComment(cvText, jobDescription, jobTitle);

        candidature.setScoreCV(result.score);
        candidature.setScoringComment(finalComment);
        candidatureRepo.save(candidature);

        log.info("Successfully scored candidature {} with score: {} and comment: {}",
                candidature.getId(), result.score, finalComment);
    }

    private String extractCvText(Candidature candidature) {
        try {
            String cvText = ollamaService.extractTextFromPdf(candidature.getCvUrl());
            if (cvText.length() > MAX_CV_TEXT_LENGTH) {
                cvText = cvText.substring(0, MAX_CV_TEXT_LENGTH) + "...";
            }
            return cvText;
        } catch (IOException e) {
            log.error("CV text extraction failed for candidature {}: {}", candidature.getId(), e.getMessage());
            candidature.setScoreCV(0.0);
            candidature.setScoringComment("Error: Could not extract CV text - " + e.getMessage());
            candidatureRepo.save(candidature);
            return null;
        }
    }

    public String buildOptimizedPrompt(String jobTitle, String jobDescription, String cvText) {
        return """
            You are an expert recruiter. Compare the candidate CV with the job offer.
            STRICTLY output ONLY a JSON object with this structure:
            {
              "cvScore": <number from 0 to 10>,
              "comment": "<short concise sentence>"
            }
            
            Rules:
            - Use double quotes for all keys and string values.
            - Do not include markdown, code fences, or explanations.
            - Do not include text before or after the JSON.
            
            --- JOB OFFER ---\n
            Title: %s\n
            Description: %s\n
            
            --- CANDIDATE CV ---\n
            %s
            """.formatted(jobTitle, jobDescription, cvText);
    }

    private ScoringResult extractScoringResult(String response) {
        if (response == null || response.isBlank()) {
            return new ScoringResult(0.0, null);
        }

        try {
            String cleaned = response.trim();
            log.debug("Processing AI response: {}", cleaned);

            // 1. Extract JSON inside \`\`\`json ... \`\`\` if present
            Pattern fencePattern = Pattern.compile("```json\\s*(\\{.*?\\})\\s*```", Pattern.DOTALL);
            Matcher fenceMatcher = fencePattern.matcher(cleaned);
            if (fenceMatcher.find()) {
                cleaned = fenceMatcher.group(1);
                log.debug("Extracted from code fence: {}", cleaned);
            }

            // 2. If AI returned Python dict with single quotes → convert to JSON
            if (cleaned.startsWith("{") && cleaned.contains("'")) {
                cleaned = cleaned.replaceAll("'", "\"");
                log.debug("Converted quotes: {}", cleaned);
            }

            // 3. Extract first {...}
            int start = cleaned.indexOf("{");
            int end = cleaned.lastIndexOf("}");
            if (start < 0 || end <= start) {
                log.warn("No JSON object found, trying regex fallback");
                return extractWithRegexFallback(response);
            }
            String jsonPart = cleaned.substring(start, end + 1);
            log.debug("Extracted JSON part: {}", jsonPart);

            // Parse JSON
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(jsonPart);

            Double score = 0.0;
            String comment = null;

            if (node.has("cvScore")) {
                score = clampScore(node.get("cvScore").asDouble());
            } else if (node.has("score")) {
                score = clampScore(node.get("score").asDouble());
            } else if (node.has("rating")) {
                score = clampScore(node.get("rating").asDouble());
            }

            if (node.has("comment")) {
                comment = node.get("comment").asText();
            } else if (node.has("feedback")) {
                comment = node.get("feedback").asText();
            } else if (node.has("analysis")) {
                comment = node.get("analysis").asText();
            }

            log.info("Successfully extracted - Score: {}, Comment: {}", score, comment);
            return new ScoringResult(score, comment);

        } catch (Exception e) {
            log.error("JSON parsing failed, trying regex fallback: {}", e.getMessage());
            return extractWithRegexFallback(response);
        }
    }

    private ScoringResult extractWithRegexFallback(String response) {
        Double score = 0.0;
        String comment = null;

        try {
            // Try to find score patterns
            List<Pattern> scorePatterns = Arrays.asList(
                    Pattern.compile("(?:score|rating|cvScore)[\"']?\\s*:?\\s*([0-9]+(?:\\.[0-9]+)?)", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*/\\s*10", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*out\\s*of\\s*10", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("\\b([0-9]+(?:\\.[0-9]+)?)\\b")
            );

            for (Pattern pattern : scorePatterns) {
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    double foundScore = Double.parseDouble(matcher.group(1));
                    score = clampScore(foundScore);
                    log.info("Extracted score {} using regex pattern", score);
                    break;
                }
            }

            // Try to find comment patterns
            List<Pattern> commentPatterns = Arrays.asList(
                    Pattern.compile("(?:comment|feedback|analysis)[\"']?\\s*:?\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE),
                    Pattern.compile("(?:comment|feedback|analysis)[\"']?\\s*:?\\s*([^,}]+)", Pattern.CASE_INSENSITIVE)
            );

            for (Pattern pattern : commentPatterns) {
                Matcher matcher = pattern.matcher(response);
                if (matcher.find()) {
                    comment = matcher.group(1).trim();
                    log.info("Extracted comment using regex: {}", comment);
                    break;
                }
            }

        } catch (Exception e) {
            log.error("Regex fallback also failed: {}", e.getMessage());
        }

        log.info("Regex fallback result - Score: {}, Comment: {}", score, comment);
        return new ScoringResult(score, comment);
    }

    private double clampScore(double score) {
        return Math.max(0.0, Math.min(10.0, score));
    }

    private String generateFallbackComment(String cvText, String jobDescription, String jobTitle) {
        List<String> requiredSkills = extractSkillsFromJobDescription(jobDescription);
        if (requiredSkills.isEmpty()) requiredSkills = Collections.singletonList("relevant skills");

        List<String> matchedSkills = requiredSkills.stream()
                .filter(skill -> cvText.toLowerCase().contains(skill.toLowerCase()))
                .toList();

        List<String> missingSkills = requiredSkills.stream()
                .filter(skill -> !matchedSkills.contains(skill))
                .toList();

        String strengths = matchedSkills.isEmpty() ? "no specific skills" : String.join(", ", matchedSkills);
        String gaps = missingSkills.isEmpty() ? "no major gaps" : String.join(", ", missingSkills);

        return String.format("The candidate has %s experience but lacks %s required for the %s role.",
                strengths, gaps, jobTitle);
    }

    private List<String> extractSkillsFromJobDescription(String jobDescription) {
        List<String> commonSkills = Arrays.asList(
                "SQL", "Power BI", "Python", "ETL", "Agile", "Scrum", "Project Management",
                "Leadership", "Communication", "Data Visualization", "Statistical Analysis",
                "Java", "JavaScript", "Spring Boot", "AWS", "Cloud", "DevOps",
                "Marketing", "Sales", "Design", "UX/UI", "C++", "Docker", "Kubernetes",
                "TypeScript", "Angular", "React", "Vue.js", "Node.js", "MySQL", "PostgreSQL", "NoSQL",
                "RESTful", "CI/CD", "Jenkins", "GitLab CI"
        );
        return commonSkills.stream()
                .filter(skill -> jobDescription.toLowerCase().contains(skill.toLowerCase()))
                .toList();
    }

    @Async
    public CompletableFuture<Void> scoreCandidatureAsync(Candidature candidature, OffreEmploi offreEmploi) {
        scoreCandidature(candidature, offreEmploi);
        return CompletableFuture.completedFuture(null);
    }

    private static class ScoringResult {
        final Double score;
        final String comment;

        ScoringResult(Double score, String comment) {
            this.score = score;
            this.comment = comment;
        }
    }
}
