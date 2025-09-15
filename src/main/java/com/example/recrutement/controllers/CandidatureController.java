package com.example.recrutement.controllers;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.entities.Challenge;
import com.example.recrutement.entities.OffreEmploi;
import com.example.recrutement.entities.SoumissionDefi;
import com.example.recrutement.repositories.CandidatureRepo;
import com.example.recrutement.repositories.SoumissionDefiRepo;
import com.example.recrutement.services.CandidatureService;
import com.example.recrutement.services.OffreEmploiService;
import com.example.recrutement.services.ScoringService;
import jakarta.transaction.Transactional;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
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
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("cvUrl") String cvUrl,
            @RequestParam("offreEmploiId") Integer offreEmploiId,
            Authentication connectedUser) {

        try {
            // 1️⃣ Create candidature object
            Candidature candidature = new Candidature();
            candidature.setNom(nom);
            candidature.setEmail(email);
            candidature.setTelephone(phone);
            candidature.setExperience(experience);
            candidature.setLinkedInProfile(linkedin);
            candidature.setPortfolioURL(portfolio);
            candidature.setCoverLetter(coverLetter);
            candidature.setStatut("EN ATTENTE");
            candidature.setStatutDefi(Candidature.StatutDefi.AUCUN);
            candidature.setStatutEntretien(Candidature.StatutEnt.AUCUN);
            candidature.setCvUrl(cvUrl);

            // Save CV file
            String fileName = saveFile(cv);
            candidature.setCv(fileName);

            // 2️⃣ Get related job offer
            OffreEmploi offre = offreEmploiService.getOffreEmploiById(offreEmploiId)
                    .orElseThrow(() -> new RuntimeException("OffreEmploi not found with ID: " + offreEmploiId));

            // 3️⃣ Save candidature first
            Candidature savedCandidature = candidatureService.createCandidature(candidature, offreEmploiId, connectedUser);

            // 4️⃣ Start workflow asynchronously and save process instance ID
            try {
                boolean workflowAlreadyStarted = runtimeService.createProcessInstanceQuery()
                        .processDefinitionKey("Process_1_1757601711956")
                        .variableValueEquals("candidatureId", savedCandidature.getId())
                        .count() > 0;

                if (!workflowAlreadyStarted) {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("candidatureId", savedCandidature.getId());
                    variables.put("offreEmploiId", offreEmploiId);
                    variables.put("candidateEmail", savedCandidature.getEmail());
                    variables.put("statutDefi", "AUCUN");

                    // Start process instance
                    ProcessInstance instance = runtimeService.startProcessInstanceByKey(
                            "Process_1_1757601711956",
                            variables
                    );

                    // Save the runtime process instance ID in candidature
                    savedCandidature.setProcessInstanceId(instance.getId());
                    candidatureRepo.save(savedCandidature);

                    log.info("Workflow started for candidature {}, instanceId={}", savedCandidature.getId(), instance.getId());
                }
            } catch (Exception e) {
                log.error("Workflow failed to start, but candidature was saved: {}", e.getMessage());
            }

            // 5️⃣ Return saved candidature immediately
            return ResponseEntity.ok(savedCandidature);

        } catch (Exception e) {
            log.error("Error creating candidature: {}", e.getMessage(), e);
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

            return ResponseEntity.ok(response);

        } catch (Exception e) {
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
            // Delete connected SoumissionDefi if mawjouda
            soumissionDefiRepo.deleteByCandidatureId(id);
            soumissionDefiRepo.flush();

            // Delete candidature itself
            candidatureRepo.deleteByIdNative(id);
            candidatureRepo.flush();
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
    public ResponseEntity<?> submitChallenge(
            @PathVariable Integer id,
            @RequestBody com.example.recrutement.dto.ChallengeSubmissionDTO payload) {

        // 1️⃣ Fetch the candidature
        var candidatureOpt = candidatureRepo.findById(id);
        if (candidatureOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var candidature = candidatureOpt.get();

        // 2️⃣ Check if the challenge has expired
        candidatureService.updateExpiredDefiIfNeeded(candidature);
        if (candidature.getStatutDefi() == Candidature.StatutDefi.EXPIRE) {
            return ResponseEntity.badRequest().body("Challenge has expired and cannot be submitted.");
        }

        // 3️⃣ Ensure there is a challenge assigned
        if (candidature.getDefi() == null) {
            return ResponseEntity.badRequest().body("No challenge assigned to this candidature.");
        }

        // 4️⃣ Get or create the submission
        var soumission = candidature.getSoumissionDefi();
        if (soumission == null) {
            soumission = new com.example.recrutement.entities.SoumissionDefi();
            soumission.setCandidature(candidature);
            soumission.setChallenge(candidature.getDefi());
        }

        // 5️⃣ Update the submission
        soumission.setCode(payload.getCode());
        soumission.setLangage(payload.getLangage());
        soumission.setResultatsExecution(payload.getResultatsExecution());
        soumission.setSoumisLe(java.time.LocalDateTime.now());
        soumission.setPointsTotal(payload.getPointsTotal() != null ? payload.getPointsTotal() : 0);
        soumission.setScore(payload.getScore() != null ? payload.getScore() : 0);
        soumission.setStatut(com.example.recrutement.entities.SoumissionDefi.StatutSoumission.Termine);
        soumissionDefiRepo.save(soumission);
        candidature.setSoumissionDefi(soumission);

        // 6️⃣ Update the candidature
        candidature.setDefiTermineLe(java.time.LocalDateTime.now());
        candidature.setScoreDefi(payload.getScore());
        candidature.setStatutDefi(com.example.recrutement.entities.Candidature.StatutDefi.TERMINE);
        candidatureRepo.save(candidature);

        // 7️⃣ Trigger the BPMN signal to continue the process
        if (candidature.getProcessInstanceId() != null) {
            Execution execution = runtimeService.createExecutionQuery()
                    .processInstanceId(candidature.getProcessInstanceId())
                    .signalEventSubscriptionName("ChallengeSubmittedSignal")
                    .singleResult();

            if (execution != null) {
                runtimeService.signalEventReceived(
                        "ChallengeSubmittedSignal",
                        execution.getId(),
                        Map.of("statutDefi", "TERMINE")
                );
                log.info("Signal sent to execution {} of process instance {}",
                        execution.getId(), candidature.getProcessInstanceId());
            } else {
                log.warn("No active execution found for candidature {}, signal not sent", candidature.getId());
            }
        }

        // 8️⃣ Return response
        return ResponseEntity.ok(Map.of(
                "message", "Submission recorded and workflow notified",
                "score", payload.getScore(),
                "pointsTotal", payload.getPointsTotal()
        ));
    }



    @PostMapping("/{id}/reanalyze")
    public ResponseEntity<?> reanalyzeCandidature(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "scoreOnly") String mode) {

        log.info("Reanalyzing candidature {} with mode {}", id, mode);

        Candidature candidature = candidatureRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Candidature not found"));

        OffreEmploi offre = candidature.getOffreEmploi();
        if (offre == null) {
            return ResponseEntity.badRequest().body("No job offer linked to candidature.");
        }

        if ("fullProcess".equalsIgnoreCase(mode)) {
            // Reset candidature
            candidature.resetForReanalysis();
            candidatureRepo.save(candidature);

            // Delete existing workflow
            runtimeService.createProcessInstanceQuery()
                    .variableValueEquals("candidatureId", candidature.getId())
                    .list()
                    .forEach(instance -> runtimeService.deleteProcessInstance(instance.getId(), "Reanalyzed by admin"));

            // Handle SoumissionDefi safely (Option 1)
            soumissionDefiRepo.deleteByCandidatureId(candidature.getId());

            // Start workflow again
            Map<String, Object> variables = new HashMap<>();
            variables.put("candidatureId", candidature.getId());
            variables.put("offreEmploiId", offre.getId());
            variables.put("candidateEmail", candidature.getEmail());
            variables.put("statutDefi", "AUCUN"); // Initialize with default value

            runtimeService.startProcessInstanceByKey("Process_1", variables);
            log.info("Workflow restarted for candidature {}", candidature.getId());

        } else if ("scoreOnly".equalsIgnoreCase(mode)) {
            // Only recalculate the score
            scoringService.scoreCandidature(candidature, offre);
            log.info("Score recalculated for candidature {}", candidature.getId());
        }

        return ResponseEntity.ok(candidatureRepo.findById(id).get());
    }
}
