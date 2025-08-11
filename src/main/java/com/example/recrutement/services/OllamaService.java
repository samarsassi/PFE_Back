package com.example.recrutement.services;

import com.example.recrutement.entities.OffreEmploi;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {


    private final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateFromPhi(String prompt, double temperature) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", "phi");
        request.put("prompt", prompt);
        request.put("stream", false);
        request.put("temperature", temperature); // <-- Add this line

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Map> response = restTemplate.exchange(OLLAMA_URL, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody().get("response").toString();
        } else {
            throw new RuntimeException("Failed to call Ollama API: " + response.getStatusCode());
        }
    }

    public String extractTextFromPdf(String url) throws IOException {
        try (InputStream inputStream = new URL(url).openStream()) {
            PDDocument document = PDDocument.load(inputStream);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();
            return text;
        }
    }


    public String buildPrompt(String resumeText, OffreEmploi offer) {
        return String.format("""
            Act as an expert recruiter. Analyze this candidate-job match:
            
            JOB: %s
            REQUIREMENTS: %s
            EXPERIENCE NEEDED: %s years
            
            RESUME: %s
            
            Provide:
            1. SCORE: X/10
            2. KEY MATCHES: (2-3 bullet points)
            3. GAPS: (1-2 main concerns)
            4. RECOMMENDATION: Hire/Interview/Reject
            
            Keep response under 150 words.
            """,
                offer.getTitre(),
                offer.getDescription(),
                offer.getNiveauExperience(),
                resumeText);
    }

}
