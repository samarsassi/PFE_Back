package com.example.recrutement.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {

    private final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private final RestTemplate restTemplate = new RestTemplate();

    public String generateFromPhi(String prompt) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", "phi");
        request.put("prompt", prompt);
        request.put("stream", false);

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
}
