package com.example.recrutement.controllers;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.Challenge;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.SoumissionDefiRepo;
import com.example.recrutement.services.CandidatureService;
import com.example.recrutement.services.OffreEmploiService;
import com.example.recrutement.services.ScoringService;
import jakarta.transaction.Transactional;
import org.flowable.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

@RestController
@RequestMapping("/candidatures")
public class CandidatureController {
    @Autowired
    private RuntimeService runtimeService;

    private static final Logger log = LoggerFactory.getLogger(CandidatureController.class);
    private final CandidatureService candidatureService;
    private final CandidatureRepo candidatureRepo;
    private final SoumissionDefiRepo soumissionDefiRepo;

    private final ScoringService scoringService;
    private final OffreEmploiService offreEmploiService;
    private static final Logger logger = LoggerFactory.getLogger(CandidatureController.class);

    public CandidatureController(CandidatureService candidatureService,
            CandidatureRepo candidatureRepo,
            SoumissionDefiRepo soumissionDefiRepo,
            ScoringService scoringService,
            OffreEmploiService offreEmploiService) {
        this.candidatureService = candidatureService;
        this.candidatureRepo = candidatureRepo;
        this.soumissionDefiRepo = soumissionDefiRepo;
        this.scoringService = scoringService;
        this.offreEmploiService = offreEmploiService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Candidature> createCandidature(
            @RequestParam("nom") String nom,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("experience") String experience,
            @RequestParam("linkedInProfile") String linkedin,
            @RequestParam("portfolioURL") String portfolio,
            @RequestParam(value = "coverLetter", required = false) String coverLetter,
            @RequestParam(value = "statut", defaultValue = "EN ATTENTE") String statut,
            @RequestParam(value = "statutDefi", defaultValue = "AUCUN") String statutDefi,
            @RequestParam(value = "statusEntretien", defaultValue = "AUCUN") String statusEntretien,
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("cvUrl") String cvUrl,
            @RequestParam("offreEmploiId") Integer offreEmploiId,
            Authentication connectedUser) {

        try {
            Candidature candidature = new Candidature();
            candidature.setNom(nom);
            candidature.setEmail(email);
            candidature.setTelephone(phone);
            candidature.setExperience(experience);
            candidature.setLinkedInProfile(linkedin);
            candidature.setPortfolioURL(portfolio);
            candidature.setCoverLetter(coverLetter);
            candidature.setStatut(statut);
            candidature.setStatutDefi(Candidature.StatutDefi.valueOf(statutDefi));
            candidature.setStatutEntretien(Candidature.StatutEnt.AUCUN);
            candidature.setCvUrl(cvUrl);

            String fileName = saveFile(cv);
            candidature.setCv(fileName);

            OffreEmploi offre = offreEmploiService.getOffreEmploiById(offreEmploiId)
                    .orElseThrow(() -> new RuntimeException("OffreEmploi not found with ID: " + offreEmploiId));

            // Create candidature first
            Candidature savedCandidature = candidatureService.createCandidature(candidature, offreEmploiId,
                    connectedUser);

            Map<String, Object> variables = new HashMap<>();
            variables.put("candidatureId", savedCandidature.getId());
            variables.put("offreEmploiId", offreEmploiId);

            // runtimeService.startProcessInstanceByKey("recruitmentProcess", variables);

            return ResponseEntity.ok(savedCandidature);

        } catch (Exception e) {
            log.error("Error creating candidature: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private String saveFile(MultipartFile file) {
        // Set the path to save the file
        String uploadDirectory = System.getProperty("user.dir") + "/uploads/";
        File uploadDir = new File(uploadDirectory);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs(); // Create the directory if it does not exist
        }

        // Create a unique filename for the CV
        String fileName = file.getOriginalFilename();
        String filePath = uploadDirectory + fileName;

        try {
            // Save the file to the specified path
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(file.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            // You could throw an exception or return a response indicating failure
        }

        return fileName; // Return the saved file path
    }

    // Get all candidatures en liste
    @GetMapping("/ListCandidature")
    public List<Candidature> getAllCandidaturesList() {
        return candidatureService.getAllCandidaturesList();
    }

    @GetMapping
    public ResponseEntity<?> getAllCandidatures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            System.out.println("=== CONTROLLER START ===");
            System.out.println("Received request - page: " + page + ", size: " + size);

            // Create simple pageable without sorting first
            Pageable pageable = PageRequest.of(page, size);
            Page<Candidature> result = candidatureService.getAllCandidatures(pageable);

            // Create a simple response to avoid serialization issues
            Map<String, Object> response = new HashMap<>();
            response.put("content", result.getContent());
            response.put("totalElements", result.getTotalElements());
            response.put("totalPages", result.getTotalPages());
            response.put("size", result.getSize());
            response.put("number", result.getNumber());

            System.out.println("=== CONTROLLER SUCCESS ===");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("=== CONTROLLER ERROR ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("Error class: " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.err.println("Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal Server Error");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("timestamp", new Date().toString());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Get a candidature by id
    @GetMapping("/{id}")
    public Candidature getCandidatureById(@PathVariable Integer id) {
        logger.info("Fetching candidature with ID: {}", id);

        return candidatureService.getCandidatureById(id)
                .map(candidature -> {
                    logger.info("Candidature found: {}", candidature.getNom());
                    return candidature;
                })
                .orElseThrow(() -> {
                    logger.error("Candidature with ID {} not found", id);
                    return new RuntimeException("Candidature not found");
                });
    }

    // Update a candidature by id
    @PutMapping("/{id}")
    public Candidature updateCandidature(@RequestBody Candidature updatedCandidature) {
        Integer id = updatedCandidature.getId();
        System.out.println("Updating candidature with ID: " + id + ", statut: " + updatedCandidature.getStatut());

        return candidatureService.updateCandidature(id, updatedCandidature);
    }

    // Delete a candidature by id
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<String> deleteCandidature(@PathVariable Integer id) {
        try {
            log.info("Before delete - Candidature ID: {}", id);

            // Delete connected SoumissionDefi if mawjouda
            soumissionDefiRepo.deleteByCandidatureId(id);
            soumissionDefiRepo.flush();

            // Delete candidature itself
            candidatureRepo.deleteByIdNative(id);
            candidatureRepo.flush();

            log.info("After delete - should be gone now");

            return ResponseEntity.ok("Candidature deleted successfully");
        } catch (Exception e) {
            log.error("Error deleting candidature: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete candidature: " + e.getMessage());
        }
    }

    // Get all candidatures for a specific job offer (OffreEmploi)

    @GetMapping("/{id}/challenge")
    public Challenge getAssignedChallenge(@PathVariable Integer id) {
        Candidature candidature = candidatureRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));
        return candidature.getDefi();
    }

    @PostMapping("/{id}/submit-challenge")
    public ResponseEntity<?> submitChallenge(@PathVariable Integer id,
            @RequestBody com.example.recrutement.dto.ChallengeSubmissionDTO payload) {
        var candidatureOpt = candidatureRepo.findById(id);
        if (candidatureOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var candidature = candidatureOpt.get();
        if (candidature.getDefi() == null) {
            return ResponseEntity.badRequest().body("No challenge assigned to this candidature");
        }
        var soumission = candidature.getSoumissionDefi();
        if (soumission == null) {
            soumission = new com.example.recrutement.entities.SoumissionDefi();
            soumission.setCandidature(candidature);
            soumission.setChallenge(candidature.getDefi());
        }
        soumission.setCode(payload.getCode());
        soumission.setLangage(payload.getLangage());
        soumission.setResultatsExecution(payload.getResultatsExecution());
        soumission.setSoumisLe(java.time.LocalDateTime.now());
        soumission.setPointsTotal(payload.getPointsTotal() != null ? payload.getPointsTotal() : 0);
        soumission.setScore(payload.getScore() != null ? payload.getScore() : 0);
        soumission.setStatut(com.example.recrutement.entities.SoumissionDefi.StatutSoumission.Termine);
        soumissionDefiRepo.save(soumission);
        candidature.setSoumissionDefi(soumission);

        candidature.setDefiTermineLe(java.time.LocalDateTime.now());
        candidature.setScoreDefi(payload.getScore());
        candidature.setStatutDefi(com.example.recrutement.entities.Candidature.StatutDefi.TERMINE);
        candidatureRepo.save(candidature);

        return ResponseEntity.ok(java.util.Map.of(
                "message", "Submission recorded",
                "score", payload.getScore(),
                "pointsTotal", payload.getPointsTotal()));
    }
}
