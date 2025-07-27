package com.example.recrutement.controllers;
import com.example.recrutement.services.OffreEmploiService;
import com.example.recrutement.services.OllamaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class OllamaController {

    private final OllamaService ollamaService;
    private final OffreEmploiService offreEmploiService;

    public OllamaController(OllamaService ollamaService, OffreEmploiService offreEmploiService) {
        this.ollamaService = ollamaService;
        this.offreEmploiService = offreEmploiService;
    }


    @PostMapping("/analyze-cv")
    public String analyzeCV(@RequestBody String cvText) {
        String prompt = "Analyze this CV and rate the candidate from 1 to 10 for a backend Java developer position. Provide short reasoning.\nCV: " + cvText;
        return ollamaService.generateFromPhi(prompt);
    }

    @PostMapping("/admin/chat")
    public String adminChat(@RequestBody String message, @RequestHeader("Authorization") String token) {
        // Optionally parse token to get user role

        if (message.toLowerCase().contains("analyze cv")) {
            // Run CV analysis logic
            return analyzeCV(message);
        }
        else if (message.toLowerCase().contains("how many offers") || message.toLowerCase().contains("show stats")) {
            // Query DB and return stats
            return getStatsSummary();
        }
        else {
            // Default: regular AI chat response
            return ollamaService.generateFromPhi(message);
        }
    }


    @PostMapping("/chat")
    public String chat(@RequestBody String userMessage) {
        return ollamaService.generateFromPhi(userMessage);
    }




    private String getStatsSummary() {
        // For example: count offers, average salary, etc.
        long offerCount = offreEmploiService.countOffers();
        double averageSalary = offreEmploiService.getAverageSalary();

        return String.format("There are currently %d job offers available. The average salary is %.2f.", offerCount, averageSalary);
    }
}
