package com.example.recrutement.controllers;

import com.example.recrutement.entities.Candidature;
import com.example.recrutement.services.CandidatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

@RestController
@RequestMapping("/candidatures")
public class CandidatureController {

    private final CandidatureService candidatureService;

    @Autowired
    public CandidatureController(CandidatureService candidatureService) {
        this.candidatureService = candidatureService;
    }

    // Create a new Candidature (Job application)

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Candidature> createCandidature(
            @RequestParam("nom") String nom,
            @RequestParam("email") String email,
            @RequestParam("phone") String phone,
            @RequestParam("experience") String experience,
            @RequestParam(value = "coverLetter", required = false) String coverLetter,
            @RequestParam(value = "statut", defaultValue = "EN ATTENTE") String statut,
            @RequestParam(value = "statutDefi", defaultValue = "AUCUN") String statutDefi,
            @RequestParam("cv") MultipartFile cv,
            @RequestParam("offreEmploiId") Integer offreEmploiId,
            Authentication connectedUser) {

        // Build Candidature object
        Candidature candidature = new Candidature();
        candidature.setNom(nom);
        candidature.setEmail(email);
        candidature.setTelephone(phone);
        candidature.setExperience(experience);
        candidature.setCoverLetter(coverLetter);
        candidature.setStatut(statut);
        candidature.setStatutDefi(Candidature.StatutDefi.AUCUN);

        // Save the file
        String fileName = saveFile(cv);
        candidature.setCv(fileName);

        return ResponseEntity.ok(candidatureService.createCandidature(candidature, offreEmploiId, connectedUser));
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

    // Get all candidatures
    @GetMapping
    public List<Candidature> getAllCandidatures() {
        return candidatureService.getAllCandidatures();
    }

    // Get a candidature by id
    @GetMapping("/{id}")
    public Candidature getCandidatureById(@PathVariable Integer id) {
        return candidatureService.getCandidatureById(id).orElseThrow(() -> new RuntimeException("Candidature not found"));
    }

    // Update a candidature by id
    @PutMapping("/{id}")
    public Candidature updateCandidature(@PathVariable Integer id, @RequestBody Candidature updatedCandidature) {
        return candidatureService.updateCandidature(id, updatedCandidature);
    }

    // Delete a candidature by id
    @DeleteMapping("/{id}")
    public void deleteCandidature(@PathVariable Integer id) {
        candidatureService.deleteCandidature(id);
    }

    // Get all candidatures for a specific job offer (OffreEmploi)
    @GetMapping("/{offreEmploiId}")
    public List<Candidature> getCandidaturesForOffreEmploi(@PathVariable Integer offreEmploiId) {
        return candidatureService.getCandidaturesForOffreEmploi(offreEmploiId);
    }
}
